<script setup lang="ts">
import { computed, nextTick, onMounted, provide, reactive, ref, useTemplateRef, watch } from "vue";
import { AxiosError } from "axios";
import * as jsonpatch from "fast-json-patch";

// Carbon
import "@carbon/web-components/es/components/breadcrumb/index";
import "@carbon/web-components/es/components/notification/index";
import "@carbon/web-components/es/components/button/index";
import "@carbon/web-components/es/components/tabs/index";
import "@carbon/web-components/es/components/tag/index";
import "@carbon/web-components/es/components/accordion/index";
import "@carbon/web-components/es/components/skeleton-text/index";
import summit from "@carbon/pictograms/es/summit";
import tools from "@carbon/pictograms/es/tools";

// Composables
import { useFetchTo, useJsonPatch } from "@/composables/useFetch";
import { useFocus } from "@/composables/useFocus";
import useSvg from "@/composables/useSvg";
import { useRouter } from "vue-router";
import { useEventBus } from "@vueuse/core";

import Location16 from "@carbon/icons-vue/es/location/16";
import User16 from "@carbon/icons-vue/es/user/16";
import NetworkEnterprise16 from "@carbon/icons-vue/es/network--enterprise/16";
import RecentlyViewed16 from "@carbon/icons-vue/es/recently-viewed/16";
import LocationStar20 from "@carbon/icons-vue/es/location--star/20";
import Location20 from "@carbon/icons-vue/es/location/20";
import User20 from "@carbon/icons-vue/es/user/20";
import NetworkEnterprise20 from "@carbon/icons-vue/es/network--enterprise/20";
import Launch16 from "@carbon/icons-vue/es/launch/16";
import Add16 from "@carbon/icons-vue/es/add/16";
import Save16 from "@carbon/icons-vue/es/save/16";

import { greenDomain } from "@/CoreConstants";
import {
  adminEmail,
  extractReasonFields,
  getObfuscatedEmailLink,
  includesAnyOf,
  toTitleCase,
  getActionLabel,
  updateSelectedReason,
  formatLocation,
  formatAddress,
  compareAny,
  buildRelatedClientIndex,
  buildRelatedClientCombination,
} from "@/services/ForestClientService";
import ForestClientUserSession from "@/helpers/ForestClientUserSession";

import {
  type ClientDetails,
  type ClientLocation,
  type ModalNotification,
  type FieldReason,
  type UserRole,
  type SaveEvent,
  createClientLocation,
  type ClientContact,
  createClientContact,
  type ValidationMessageType,
  type ClientInformation,
  type RelatedClientList,
  type RelatedClientEntry,
  createIndexedRelatedClientEntry,
  type IndexedRelatedClient,
} from "@/dto/CommonTypesDto";

// Page components
import SummaryView from "@/pages/client-details/SummaryView.vue";
import LocationView from "@/pages/client-details/LocationView.vue";
import ContactView from "@/pages/client-details/ContactView.vue";
import LocationRelationshipsView from "@/pages/client-details/LocationRelationshipsView.vue";
import ClientRelationshipForm from "@/pages/client-details/ClientRelationshipForm.vue";
import HistoryView from "@/pages/client-details/HistoryView.vue";
import { isNotEmpty, isUniqueDescriptive } from "@/helpers/validators/GlobalValidators";
import { getRelationshipRefName, type OperationOptions, type SaveableComponent } from "./client-details/shared";

// Route related
const router = useRouter();
const clientNumber = router.currentRoute.value.params.id as string;

const selectedTab = ref<string>();
watch(router.currentRoute, ({ hash }) => {
  selectedTab.value = (hash || "#locations").substring(1);
}, { immediate: true });

const toastBus = useEventBus<ModalNotification>("toast-notification");

const revalidateBus = useEventBus<string[] | undefined>("revalidate-bus");

/**
 * Event bus for submission error notifications.
 */
 const errorBus = useEventBus<ValidationMessageType[]>("submission-error-notification");

const { setFocusedComponent, setScrollPoint } = useFocus();

const data = ref<ClientDetails>(undefined);

const userRoles = ForestClientUserSession.authorities as UserRole[];

const userHasAuthority = includesAnyOf(userRoles, [
  "CLIENT_ADMIN",
  "CLIENT_SUSPEND",
  "CLIENT_EDITOR",
]);

const { error: fetchError, fetch: fetchClientData } = useFetchTo(
  `/api/clients/details/${clientNumber}`,
  data,
);

watch(fetchError, (value) => {
  globalError.value = value;
});

const buildFullName = (clientInfo: ClientInformation) => {
  if (clientInfo) {
    const { legalFirstName, legalMiddleName, clientName } = clientInfo;
    const rawParts = [legalFirstName, legalMiddleName, clientName];
    const populatedParts = [];
    for (const part of rawParts) {
      if (part) {
        populatedParts.push(part);
      }
    }
    const fullClientName = populatedParts.join(" ");
    return fullClientName;
  }
  return "";
};

const clientFullName = computed(() => {
  if (data.value) {
    return buildFullName(data.value.client);
  }
  return "";
});

const formatCount = (count = 0) => {
  return String(count).padStart(2, "0");
};

const pluralize = (word: string, count = 0) => {
  if (count === 1) {
    return word;
  }
  return `${word}s`;
};

const newLocation = ref<ClientLocation>();

const sortedLocations = computed(() => {
  const result = data.value?.addresses?.toSorted((a, b) =>
    compareAny(a.clientLocnCode, b.clientLocnCode),
  );
  if (newLocation.value) {
    result.push(newLocation.value);
  }
  return result;
});

interface CollapsibleState {
  isReloading?: boolean;
  name: string;
  startOpen?: boolean;
}

const createCollapsibleState = (initialState?: Partial<CollapsibleState>): CollapsibleState => ({
  isReloading: false,
  name: "",
  startOpen: false,
  ...initialState,
});

const locationsState = reactive<Record<string, CollapsibleState>>({});

watch(sortedLocations, () => {
  sortedLocations.value?.forEach((location) => {
    const locationCode = location.clientLocnCode;
    if (!locationsState[locationCode]) {
      locationsState[locationCode] = createCollapsibleState();
    }
    // reset location name
    locationsState[locationCode].name = location.clientLocnName;
  });
});

const uniqueLocations = isUniqueDescriptive();

watch(sortedLocations, (value) => {
  if (value?.length) {
    value.forEach((location) => {
      const index = location.clientLocnCode;
      uniqueLocations.add("Names", index)(location.clientLocnName ?? "");
    });
  }
});

const newContact = ref<ClientContact>();

const sortedContacts = computed(() => {
  const result = data.value?.contacts?.toSorted((a, b) =>
    compareAny(a.contactName, b.contactName),
  );
  if (newContact.value) {
    result.push(newContact.value);
  }
  return result;
});

const uniqueContacts = isUniqueDescriptive();

watch(sortedContacts, (value) => {
  if (value?.length) {
    value.forEach((contact) => {
      uniqueContacts.add("Name", String(contact.contactId))(contact.contactName);
    });
  }
});

const contactsState = reactive<Record<string, CollapsibleState>>({});

watch(sortedContacts, (_value, oldValue) => {
  sortedContacts.value?.forEach((contact) => {
    const contactId = contact.contactId;
    if (!contactsState[contactId]) {
      contactsState[contactId] = createCollapsibleState();
    }
    // reset to the original value
    contactsState[contactId].name = contact.contactName;
  });

  oldValue?.forEach((oldContact) => {
    const oldContactId = oldContact.contactId;
    if (!sortedContacts.value?.find((contact) => contact.contactId === oldContactId)) {
      // remove deleted contact's state
      delete contactsState[oldContactId];
      uniqueContacts.remove("Name", String(oldContactId));
    }
  });
});

const getUpdatedLocationTitle = (location: ClientLocation) => {
  return formatLocation(location.clientLocnCode, locationsState[location.clientLocnCode].name);
};

const formatLocationsList = (
  locationCodes: string[],
  allLocations: ClientLocation[] = sortedLocations.value,
) => {
  const list: string[] = [];
  if (Array.isArray(locationCodes)) {
    for (const curLocationCode of locationCodes.toSorted()) {
      const location = allLocations.find(
        (curLocation) => curLocation.clientLocnCode === curLocationCode,
      );

      if (location) {
        const title = getUpdatedLocationTitle(location);
        list.push(title);
      }
    }
  }
  return list.join(", ");
};

const associatedLocationsRecord = computed(() => {
  const result: Record<string, string> = {};
  sortedContacts.value?.forEach((contact) => {
    result[contact.contactId] = formatLocationsList(contact.locationCodes);
  });
  return result;
});

const formatContact = (contact: ClientContact) => {
  if (contact.contactId === null && !contactsState[contact.contactId].name) {
    return "New contact";
  }
  return contactsState[contact.contactId].name;
};

const addLocation = () => {
  const codeString = null;
  newLocation.value = createClientLocation(clientNumber, codeString);
  locationsState[codeString] = createCollapsibleState({ startOpen: true });

  const index = sortedLocations.value.length - 1;
  setScrollPoint(`location-${index}-heading`, undefined, () => {
    setFocusedComponent(`location-${index}-heading`);
  });
};

const handleLocationCanceled = (location: ClientLocation) => {
  if (location.clientLocnCode === null) {
    newLocation.value = undefined;
    delete locationsState[location.clientLocnCode];
  } else {
    // reset location name
    locationsState[location.clientLocnCode].name = location.clientLocnName;
  }
};

const updateLocationName = (locationName: string, locationCode: string) => {
  locationsState[locationCode].name = locationName;
};

const addContact = () => {
  const contactId = null;
  newContact.value = createClientContact(contactId, clientNumber);
  contactsState[contactId] = createCollapsibleState({ startOpen: true });
  
  setScrollPoint(`contact-${contactId}-heading`, undefined, () => {
    setFocusedComponent(`contact-${contactId}-heading`);
  });
};

const handleContactCanceled = (contact: ClientContact) => {
  if (contact.contactId === null) {
    newContact.value = undefined;
    delete contactsState[contact.contactId];
  } else {
    // reset to the original value
    contactsState[contact.contactId].name = contact.contactName;
  }
};

const updateContactName = (contactName: string, contactId: number) => {
  contactsState[contactId].name = contactName;
};

const newIndexedRelationship = ref<IndexedRelatedClient>();

const relatedClientsLocations = computed<RelatedClientList>(() => {
  const result = {...data.value?.relatedClients};
  if (newIndexedRelationship.value) {
    result.null = [newIndexedRelationship.value];
  }
  return result;
});

const addRelationship = () => {
  newIndexedRelationship.value = createIndexedRelatedClientEntry(clientNumber);
  relatedLocationsState.null = createCollapsibleState({ startOpen: true });

  const index = "null";
  setScrollPoint(`relationships-location-${index}-heading`, undefined, () => {
    setFocusedComponent(`relationships-location-${index}-heading`);
  });
};

const handleRelationshipCanceled = (relationship: RelatedClientEntry) => {
  console.log(relationship, relationship === newIndexedRelationship.value)
  if (relationship === newIndexedRelationship.value) {
    newIndexedRelationship.value = undefined;
    delete relatedLocationsState.null;
  }
};

const openRelatedClientsLegacy = () => {
  const url = `https://${greenDomain}/int/client/client04RelatedClientListAction.do?bean.clientNumber=${clientNumber}`;
  window.open(url, "_blank", "noopener");
};

const summitSvg = useSvg(summit);
const toolsSvg = useSvg(tools);

const summaryRef = ref<InstanceType<typeof SummaryView> | null>(null);

const saveableComponentRef = ref<SaveableComponent>(null);

const reasonModalActiveInd = ref(false);

type ReasonPatch = jsonpatch.AddOperation<FieldReason> & {
  action: string;
};

type OnSuccess = (response: any) => void;
type OnFailure = (error: AxiosError) => void;

const reasonPatchData = ref<ReasonPatch[]>([]);
let originalPatchData: jsonpatch.Operation[] = [];
const finalPatchData = ref<jsonpatch.Operation[]>([]);
const onSuccessPatch = ref<(response: any) => void>();
const onFailurePatch = ref<(error: AxiosError) => void>();
const selectedReasons = ref<FieldReason[]>([]);
const saveDisabled = ref(false);
const isSaveFirstClick = ref(false);

const fieldValidations: Record<string, ((value: string) => string)[]> = {};

fieldValidations["selectedReasons.*.reason"] = [
  isNotEmpty("You must select a reason")
];

const getValidations = (key: string): ((value: any) => string)[] => {
  const match = Object.keys(fieldValidations).find((validationKey) =>
    new RegExp(`^${validationKey.replace(/\*/g, "\\d+")}$`).test(key)
  );

  if (match) {
    const validations = fieldValidations[match] ?? [];
    if (!validations || validations.length === 0) {
      return [];
    }
    return validations;
  }
  return [];
};

const checkReasonCodesValidations = () => {
  for (const valid of reasonCodesValidations.value) {
    if (!valid) {
      return false;
    }
  }
  return true;
};

const reasonCodesValidations = ref<boolean[]>([]);

watch(
  reasonCodesValidations,
  () => {
    if (isSaveFirstClick.value) {
      // Enables the Save button if not clicked for the first time yet
      saveDisabled.value = false;
      return;
    }

    // Possibly reenables the button
    saveDisabled.value = !checkReasonCodesValidations();
  },
  {
    deep: true,
  },
);

// Function to update reasons and send final PATCH request
const confirmReasons = () => {
  isSaveFirstClick.value = false;

  // Prevents double-click
  saveDisabled.value = true;

  const reasonInputIdList = reasonPatchData.value.map((_, index) => `input-reason-${index}`);
  revalidateBus.emit(reasonInputIdList);

  if (!checkReasonCodesValidations()) {
    return;
  }

  // Continue with the patch process
  reasonPatchData.value.forEach((patch, index) => {
    const reasonEntry = selectedReasons.value[index];
    if (reasonEntry) {
      patch.value.reason = reasonEntry.reason;
    }
  });

  reasonModalActiveInd.value = false;
  sendPatchRequest(reasonPatchData.value);
};

const sendPatchRequest = (reasonPatchData: ReasonPatch[]) => {
  // Stores a reference to the component that triggered the Patch request
  const patchingComponent = saveableComponentRef.value;
  
  patchingComponent.setSaving(true);

  const reasonChanges: jsonpatch.Operation[] = reasonPatchData.map((patch) => {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const { action, ...operation } = patch;

    return operation;
  });

  finalPatchData.value = [...originalPatchData, ...reasonChanges];

  // Send API request
  const {
    fetch: patch,
    response,
    error,
  } = useJsonPatch(`/api/clients/details/${clientNumber}`, finalPatchData.value, { skip: true });

  patch().then(() => {
    patchingComponent.setSaving(false);

    if (response.value.status) {
      onSuccessPatch.value(response.value);
    }
    if (error.value.status) {
      onFailurePatch.value(error.value);
    }
  });
};

const handlePatch = (
  patchData: jsonpatch.Operation[],
  onSuccess: OnSuccess,
  onFailure: OnFailure,
) => {
  // Reset values
  selectedReasons.value = [];
  saveDisabled.value = false;

  originalPatchData = [...patchData];
  const reasonFields = extractReasonFields(patchData, data.value);

  // Initializes the validations array
  reasonCodesValidations.value = Array(reasonFields.length).fill(false);

  // Store on global variables to use later
  onSuccessPatch.value = onSuccess;
  onFailurePatch.value = onFailure;

  if (reasonFields.length > 0) {
    reasonPatchData.value = reasonFields.map((item, index) => ({
      op: "add",
      path: `/reasons/${index}`,
      value: {
        field: item.field,
        reason: "", // To be selected later
      },
      action: item.action,
    }));

    // Prevents focusing input field on the modal
    setTimeout(() => {
      reasonModalActiveInd.value = true;
    }, 0);

    isSaveFirstClick.value = true;
  } else {
    reasonPatchData.value = [];
    sendPatchRequest([]); // No reasons required
  }

  resetGlobalError();
};

// Function to save
const saveSummary = (payload: SaveEvent<ClientDetails>) => {
  const { updatedData: updatedClient, patch: patchData} = payload;

  const updatedFullName = buildFullName(updatedClient.client);

  saveableComponentRef.value = summaryRef.value;

  const onSuccess: OnSuccess = () => {
    const toastNotification: ModalNotification = {
      kind: "Success",
      active: true,
      handler: () => {},
      message: `Client <span class="weight-700">“${updatedFullName}”</span> was updated`,
      toastTitle: undefined,
    };
    toastBus.emit(toastNotification);
    summaryRef.value.lockEditing();
    data.value = undefined;
    fetchClientData();
  };

  const onFailure: OnFailure = (error) => {
    const toastNotification: ModalNotification = {
      kind: "Error",
      active: true,
      handler: () => {},
      message: "Failed to update client",
      toastTitle: undefined,
    };
    toastBus.emit(toastNotification);
    globalError.value = error;
    if (error.code === AxiosError.ERR_BAD_REQUEST && Array.isArray(error.response.data)) {
      const validationMessages: ValidationMessageType[] = (error.response.data as any[]).flatMap(
        (error) => {
          /*
          For the registrationNumber error, splits the error in two so as to put the two related fields in error
          state.
          Also uses a common parent path "/client/registrationNumber" so as to identify it as a group error
          i.e. an error that concerns to the two fields combined.
          */
          const registrationNumberGroup = ["/client/registrationNumber/type", "/client/registrationNumber/number"];

          const fieldList = error?.fieldId === "/client/registrationNumber" ? registrationNumberGroup : [error?.fieldId];
          return fieldList.map((fieldId) => ({
            fieldId,
            fieldName: "",
            errorMsg: error?.errorMsg || "custom", // we need a non-empty value here to activate the error state
            custom: {
              ...error,
            },
          }));
        },
      );
      errorBus.emit(validationMessages, {
        skipNotification: true,
      });
      setScrollPoint("top-notification");
    }
  };
  handlePatch(patchData, onSuccess, onFailure);
};

const locationsRef = ref<InstanceType<typeof LocationView>[]>([]);

const setLocationRef = (index: number) => (el: InstanceType<typeof LocationView>) => {
  locationsRef.value[index] = el;
};

const adjustPatchPath = (rawPatchData: jsonpatch.Operation[], prefix: string) => {
  const patchData = rawPatchData.map((item) => ({
    ...item,
    path: `${prefix}${item.path}`,
  }));
  return patchData;
};

const createAddPatch = <T>(value: T, path: string) => {
  const patch: jsonpatch.AddOperation<T> = {
    op: "add",
    path,
    value,
  };
  return [patch];
};

const createRemovePatch = (path: string) => {
  const patch: jsonpatch.RemoveOperation = {
    op: "remove",
    path,
  };
  return [patch];
};

const saveLocation =
  (index: number) =>
  (payload: SaveEvent<ClientLocation>) => {
    const {
      patch: rawPatchData,
      updatedData: updatedLocation,
      action,
    } = payload;

    const locationCode = updatedLocation.clientLocnCode;

    const isNew = updatedLocation.clientLocnCode === null;

    // Removes the location code from the new data as it's just a pseudo id.
    const { clientLocnCode, ...newLocationData } = updatedLocation;

    const patchData = isNew
      ? createAddPatch(newLocationData, "/addresses/null")
      : adjustPatchPath(rawPatchData, `/addresses/${locationCode}`);

    const updatedTitle = getUpdatedLocationTitle(updatedLocation);

    saveableComponentRef.value = locationsRef.value[index];

    const onSuccess: OnSuccess = () => {
      const toastNotification: ModalNotification = {
        kind: "Success",
        active: true,
        handler: () => {},
        message: `Location <span class="weight-700">“${updatedTitle}”</span> was ${action.pastParticiple}`,
        toastTitle: undefined,
      };
      toastBus.emit(toastNotification);

      locationsRef.value[index].lockEditing();

      if (!locationsState[locationCode]) {
        locationsState[locationCode] = createCollapsibleState();
      }

      locationsState[locationCode].isReloading = true;

      fetchClientData().asyncResponse.then(() => {
        locationsState[locationCode].isReloading = false;

        if (isNew) {
          // Reset the newLocation variable
          newLocation.value = undefined;
        }
      });
    };

    const onFailure: OnFailure = (error) => {
      const toastNotification: ModalNotification = {
        kind: "Error",
        active: true,
        handler: () => {},
        message: `Failed to ${action.infinitive} location`,
        toastTitle: undefined,
      };
      toastBus.emit(toastNotification);
      globalError.value = error;
    };

    handlePatch(patchData, onSuccess, onFailure);
  };

const contactsRef = ref<InstanceType<typeof ContactView>[]>([]);

const setContactRef = (index: number) => (el: InstanceType<typeof ContactView>) => {
  contactsRef.value[index] = el;
};

const operateContact =
  (index: number | string) =>
  (payload: SaveEvent<ClientContact>, rawOptions?: OperationOptions) => {
    const {
      patch: rawPatchData,
      updatedData: updatedContact,
      action,
    } = payload;
    
    // Removes the contactId from the new data as it's just a pseudo id.
    const { contactId, ...newContactData } = updatedContact;

    const options: OperationOptions = rawOptions ?? {};
    const { preserveRawPatch } = options;

    const isNew = contactId === null;

    let patchData: jsonpatch.Operation[];

    if (preserveRawPatch) {
      patchData = rawPatchData;
    } else {
      patchData = isNew
        ? createAddPatch(newContactData, "/contacts/null")
        : adjustPatchPath(rawPatchData, `/contacts/${contactId}`);
    }

    const updatedTitle = updatedContact.contactName;

    saveableComponentRef.value = contactsRef.value[index];

    const onSuccess: OnSuccess = () => {
      const toastNotification: ModalNotification = {
        kind: "Success",
        active: true,
        handler: () => {},
        message: `Contact <span class="weight-700">“${updatedTitle}”</span> was ${action.pastParticiple}`,
        toastTitle: undefined,
      };
      toastBus.emit(toastNotification);

      contactsRef.value[index].lockEditing();

      if (!contactsState[contactId]) {
        contactsState[contactId] = createCollapsibleState();
      }

      contactsState[contactId].isReloading = true;

      fetchClientData().asyncResponse.then(() => {
        // Checking for existence because it might have just been deleted.
        if (contactsState[contactId]) {
          contactsState[contactId].isReloading = false;
        }
        if (isNew) {
          // Reset the newContact variable
          newContact.value = undefined;
        }
      });
    };

    const onFailure: OnFailure = (error) => {
      const toastNotification: ModalNotification = {
        kind: "Error",
        active: true,
        handler: () => {},
        message: `Failed to ${action.infinitive} contact`,
        toastTitle: undefined,
      };
      toastBus.emit(toastNotification);
      globalError.value = error;
    };

    handlePatch(patchData, onSuccess, onFailure);
  };

const deleteContact =
  (index: number | string) =>
  (contact: ClientContact) => {
    const patch = createRemovePatch(`/contacts/${contact.contactId}`);
    operateContact(index)({
      action: {
        infinitive: "delete",
        pastParticiple: "deleted",
      },
      patch,
      updatedData: contact,
      operationType: "delete",
    }, { preserveRawPatch: true });
  };

const operateRelatedClient =
  (payload: SaveEvent<IndexedRelatedClient>, rawOptions?: OperationOptions) => {
    const {
      patch: rawPatchData,
      updatedData: updatedRelatedClient,
      action,
    } = payload;

    const { index: relatedClientIndex, originalLocation, ...newRelationshipData } = updatedRelatedClient;

    const options: OperationOptions = rawOptions ?? {};
    const { preserveRawPatch } = options;

    const isNew = relatedClientIndex === null;

    let patchData: jsonpatch.Operation[];

    if (preserveRawPatch) {
      patchData = rawPatchData;
    } else {
      patchData = isNew
        ? createAddPatch(newRelationshipData, `/relatedClients/${newRelationshipData.client.location.code}/null`)
        : adjustPatchPath(rawPatchData, `/relatedClients/${originalLocation.code}/${relatedClientIndex}`);
    }

    const updatedTitle = `${updatedRelatedClient.relatedClient.client.code}, ${updatedRelatedClient.relatedClient.client.name}`;

    saveableComponentRef.value = useTemplateRef(getRelationshipRefName(originalLocation.code, relatedClientIndex)) as unknown as SaveableComponent;

    const onSuccess: OnSuccess = () => {
      const toastNotification: ModalNotification = {
        kind: "Success",
        active: true,
        handler: () => {},
        message: `Client relationship with <span class="weight-700">“${toTitleCase(updatedTitle)}”</span> was ${action.pastParticiple}`,
        toastTitle: undefined,
      };
      toastBus.emit(toastNotification);

      saveableComponentRef.value.lockEditing();

      const locationId = updatedRelatedClient.client.location.code;

      if (!relatedLocationsState[locationId]) {
        relatedLocationsState[locationId] = createCollapsibleState();
      }

      relatedLocationsState[locationId].isReloading = true;

      fetchClientData().asyncResponse.then(() => {
        // Checking for existence because it might have just been deleted.
        if (relatedLocationsState[locationId]) {
          relatedLocationsState[locationId].isReloading = false;
        }
        if (isNew) {
          // Reset the newRelationship variable
          newIndexedRelationship.value = undefined;
        }
      });
    };

    const onFailure: OnFailure = (error) => {
      const toastNotification: ModalNotification = {
        kind: "Error",
        active: true,
        handler: () => {},
        message: `Failed to ${action.infinitive} client relationship`,
        toastTitle: undefined,
      };
      toastBus.emit(toastNotification);
      globalError.value = error;
    };

    handlePatch(patchData, onSuccess, onFailure);
  };

provide("operateRelatedClient", operateRelatedClient);

const globalError = ref();

const resetGlobalError = () => {
  globalError.value = {};
};

resetGlobalError();

const isHistoryPanelVisible = ref(false);

const isRelatedClientsPanelVisible = ref(false);

onMounted(async () => {
  await nextTick();

  const tabs = document.querySelector('cds-tabs');
  if (tabs) {
    tabs.addEventListener('cds-tabs-selected', (event: any) => {
      selectedTab.value = event.detail.item.value;
      
      setTimeout(() => {
        const historyPanel = document.getElementById('panel-history');
        if (historyPanel) {
          isHistoryPanelVisible.value = !historyPanel.hasAttribute('hidden');
        }

        const relatedPanel = document.getElementById('panel-related');
        if (relatedPanel) {
          isRelatedClientsPanelVisible.value = !relatedPanel.hasAttribute('hidden');
        }
      }, 0);
    });
  }
});

const findLocation = (locationCode: string) =>
  sortedLocations.value?.find((location) => location.clientLocnCode === locationCode);

const countRelatedClients = computed(() =>
(data && data.value?.relatedClients) ?
  Object.keys(data.value?.relatedClients ?? {}).reduce(
    (previousValue, locationCode) => previousValue + data.value?.relatedClients[locationCode]?.length,
    0,
  ) : 0
);

const relatedLocationsState = reactive<Record<string, CollapsibleState>>({});

watch([() => Object.keys(relatedClientsLocations.value ?? {}), locationsState], ([relatedLocationCodeList]) => {
  relatedLocationCodeList?.forEach((relatedLocationCode) => {
    if (!relatedLocationsState[relatedLocationCode]) {
      relatedLocationsState[relatedLocationCode] = createCollapsibleState();
    }

    // reset location name
    relatedLocationsState[relatedLocationCode].name = locationsState[relatedLocationCode]?.name;
  });
});

const uniqueRelationships = isUniqueDescriptive("This combination of location, relationship type, related client and its location already exists");

watch(() => data.value?.relatedClients, (value) => {
  if (value) {
    Object.entries(value).forEach(([curLocationCode, curList]) => {
      curList.forEach((entry, index) => {
        const uniqueIndex = buildRelatedClientIndex(curLocationCode, index);
        const value = buildRelatedClientCombination(entry);
        uniqueRelationships.add("Combination", uniqueIndex)(value);
      });
    });
  }
});

const formatRelatedLocation = (locationCode: string) => {
  if (locationCode === "null") {
    return "New client relationship";
  }
  return `Under location “${formatLocation(locationCode, relatedLocationsState[locationCode].name)}”`;
};
</script>

<template>
  <div id="screen" class="client-details-screen">
    <div class="client-details-content">
      <div class="resource-header">
        <cds-breadcrumb>
          <cds-breadcrumb-item>
            <cds-breadcrumb-link href="/search">Client search</cds-breadcrumb-link>
          </cds-breadcrumb-item>
        </cds-breadcrumb>

        <template v-if="!fetchError.code">
          <h1 v-if="clientFullName" class="resource-details--title">
            <span>
              {{ toTitleCase(clientFullName) }}
            </span>
          </h1>
          <cds-skeleton-text v-else v-shadow="1" class="heading-03-skeleton" />
        </template>
        <div>
          <p class="body-02 light-theme-text-text-secondary" data-testid="subtitle">
            Check and manage this client's data
          </p>
        </div>
      </div>

      <div class="hide-when-less-than-two-children">
        <!--
          This div is necessary to avoid the div.header-offset below from interfering in the flex flow.
        -->
        <div data-scroll="top-notification" class="header-offset"></div>
        <cds-actionable-notification
          v-if="[AxiosError.ERR_BAD_RESPONSE, AxiosError.ERR_NETWORK].includes(globalError.code)"
          id="internalServerError"
          v-shadow="true"
          low-contrast="true"
          hide-close-button="true"
          open="true"
          kind="error"
          title="Something went wrong:"
        >
          <div>
            We're working to fix a problem with our network. Please try again later. If this error
            persists, please email
            <span v-dompurify-html="getObfuscatedEmailLink(adminEmail)"></span> for help.
          </div>
        </cds-actionable-notification>
        <cds-actionable-notification
          v-else-if="globalError.code === AxiosError.ERR_BAD_REQUEST"
          id="badRequestError"
          v-shadow="true"
          low-contrast="true"
          hide-close-button="true"
          open="true"
          kind="error"
          title="Something went wrong:"
        >
          <div>
            There seems to be a problem with the information you entered. Please double-check and
            try again.
          </div>
        </cds-actionable-notification>
      </div>

      <div v-if="!fetchError.code" class="grouping-14">
        <div class="grouping-05-short">
          <div>
            <h2 class="mg-tl-2 heading-05">Client summary</h2>
            <div class="grouping-10">
              <summary-view
                ref="summaryRef"
                v-if="data"
                :data="data"
                :userRoles="userRoles"
                @save="saveSummary"
              />
              <div v-else v-for="i in Array(4)" :key="i" class="grouping-11" >
                <cds-skeleton-text v-shadow="1" class="label-skeleton" />
                <cds-skeleton-text v-shadow="1" class="value-skeleton" />
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="invisible"></div>
    </div>
    <div :id="selectedTab" v-if="!fetchError.code" class="client-details-content tabs-container opaque-background">
      <cds-tabs :value="selectedTab" type="contained">
        <cds-tab
          id="tab-locations" target="panel-locations" value="locations">
          <div>
            Client locations
            <Location16 />
          </div>
        </cds-tab>
        <cds-tab id="tab-contacts" target="panel-contacts" value="contacts">
          <div>
            Client contacts
            <User16 />
          </div>
        </cds-tab>
        <cds-tab id="tab-related" target="panel-related" value="related">
          <div>
            Related clients
            <NetworkEnterprise16 />
          </div>
        </cds-tab>
        <cds-tab id="tab-history" target="panel-history" value="history">
          <div>
            History
            <RecentlyViewed16 />
          </div>
        </cds-tab>
      </cds-tabs>
    </div>
    <div v-if="!fetchError.code" class="tab-panels-container">
      <div id="panel-locations" role="tabpanel" aria-labelledby="tab-locations" hidden>
        <div class="tab-header space-between">
          <template v-if="data">
            <h3 class="padding-left-1rem">
              {{ formatCount(data.addresses?.length) }}
              {{ pluralize("location", data.addresses?.length) }}
            </h3>
            <cds-button
              v-if="userHasAuthority"
              id="addLocationBtn"
              kind="primary"
              size="md"
              @click="addLocation"
              :disabled="newLocation"
            >
              <span class="width-unset">Add location</span>
              <Add16 slot="icon" />
            </cds-button>
          </template>
          <cds-skeleton-text v-else v-shadow="1" class="heading-05-skeleton" />
        </div>
        <div class="tab-panel tab-panel--populated">
          <cds-accordion
            v-for="(location, index) in sortedLocations"
            :key="location.clientLocnCode"
            :id="`location-${location.clientLocnCode}`"
          >
            <div :data-scroll="`location-${index}-heading`" class="header-tabs-offset"></div>
            <cds-accordion-item
              size="lg"
              class="grouping-13"
              v-shadow="1"
              :open="locationsState[location.clientLocnCode]?.startOpen"
              :data-focus="`location-${index}-heading`"
            >
              <div
                slot="title"
                class="flex-column-0_25rem"
                :class="{ invisible: locationsState[location.clientLocnCode]?.isReloading }"
              >
                <span class="label-with-icon">
                  <LocationStar20 v-if="index === 0" />
                  <Location20 v-else />
                  {{ getUpdatedLocationTitle(location) }}
                  <cds-tag
                    :id="`location-${location.clientLocnCode}-deactivated`"
                    v-if="location.locnExpiredInd === 'Y'"
                    type="purple"
                    title=""
                  >
                    <span>Deactivated</span>
                  </cds-tag>
                </span>
                <span
                  :id="`location-${location.clientLocnCode}-title-address`"
                  class="hide-open body-compact-01 padding-left-1_625rem"
                >
                  {{ formatAddress(location) }}
                </span>
              </div>
              <location-view
                :ref="setLocationRef(index)"
                :data="location"
                :is-reloading="locationsState[location.clientLocnCode]?.isReloading"
                :user-roles="userRoles"
                :validations="[uniqueLocations.check]"
                keep-scroll-bottom-position
                :createMode="location.clientLocnCode === null"
                @update-location-name="updateLocationName($event, location.clientLocnCode)"
                @save="saveLocation(index)($event)"
                @canceled="handleLocationCanceled(location)"
              />
            </cds-accordion-item>
          </cds-accordion>
        </div>
      </div>
      <div id="panel-contacts" role="tabpanel" aria-labelledby="tab-contacts" hidden>
        <template v-if="!data || sortedContacts?.length">
          <div class="tab-header space-between">
            <template v-if="data">
              <h3 class="padding-left-1rem">
                {{ formatCount(data.contacts?.length) }}
                {{ pluralize("contact", data.contacts?.length) }}
              </h3>
              <cds-button
                v-if="userHasAuthority"
                id="addContactBtn"
                kind="primary"
                size="md"
                @click="addContact"
                :disabled="newContact"
              >
                <span class="width-unset">Add contact</span>
                <Add16 slot="icon" />
              </cds-button>
            </template>
            <cds-skeleton-text v-else v-shadow="1" class="heading-05-skeleton" />
          </div>
          <div class="tab-panel tab-panel--populated">
            <cds-accordion
              v-for="(contact, index) in sortedContacts"
              :key="contact.contactId"
              :id="`contact-${contact.contactId}`"
            >
              <div :data-scroll="`contact-${contact.contactId}-heading`" class="header-tabs-offset"></div>
              <cds-accordion-item
                size="lg"
                class="grouping-13"
                v-shadow="1"
                :open="contactsState[contact.contactId]?.startOpen"
                :data-focus="`contact-${contact.contactId}-heading`"
              >
                <div
                  slot="title"
                  class="flex-column-0_25rem"
                  :class="{ invisible: contactsState[contact.contactId]?.isReloading }"
                >
                  <span class="label-with-icon">
                    <User20 />
                    {{ formatContact(contact) }}
                  </span>
                  <span
                    :id="`contact-${contact.contactId}-title-locations`"
                    class="hide-open body-compact-01 padding-left-1_625rem"
                  >
                    {{ associatedLocationsRecord[contact.contactId] }}
                  </span>
                </div>
                <contact-view
                  :ref="setContactRef(index)"
                  :data="contact"
                  :index="contact.contactId"
                  :associatedLocationsString="associatedLocationsRecord[contact.contactId]"
                  :is-reloading="contactsState[contact.contactId]?.isReloading"
                  :all-locations="sortedLocations"
                  :user-roles="userRoles"
                  :validations="[uniqueContacts.check]"
                  keep-scroll-bottom-position
                  :createMode="contact.contactId === null"
                  @update-contact-name="updateContactName($event, contact.contactId)"
                  @save="operateContact(index)($event)"
                  @delete="deleteContact(index)($event)"
                  @canceled="handleContactCanceled(contact)"
                />
              </cds-accordion-item>
            </cds-accordion>
          </div>
        </template>
        <div class="tab-panel tab-panel--empty" v-else>
          <div class="empty-table-list">
            <summit-svg alt="Summit pictogram" class="standard-svg" />
            <div class="description">
              <div class="inner-description">
                <p class="heading-02">Nothing to show yet!</p>
                <p class="body-compact-01" v-if="userHasAuthority">
                  Click “Add contact” button to start
                </p>
                <p class="body-compact-01" v-else>
                  No contacts have been added to this client account
                </p>
              </div>
              <cds-button
                v-if="userHasAuthority"
                id="addContactBtn"
                kind="primary"
                size="md"
                @click="addContact"
                :disabled="newContact"
              >
                <span class="width-unset">Add contact</span>
                <Add16 slot="icon" />
              </cds-button>
            </div>
          </div>
        </div>
      </div>
      <div id="panel-related" role="tabpanel" aria-labelledby="tab-related" hidden>
        <template v-if="$features.RELATED_CLIENTS">
          <template v-if="!data || Object.entries(relatedClientsLocations).length">
            <div class="tab-header space-between">
              <h3 class="padding-left-1rem">
                Client relationships
              </h3>
              <cds-button
                v-if="userHasAuthority"
                id="addClientRelationshipBtn"
                kind="primary"
                size="md"
                @click="addRelationship"
                :disabled="newIndexedRelationship"
              >
                <span class="width-unset">Add client relationship</span>
                <Add16 slot="icon" />
              </cds-button>              
            </div>
            <div class="tab-panel tab-panel--populated">
              <cds-accordion
                v-for="([curLocationCode, curList], index) in Object.entries(relatedClientsLocations)"
                :key="curLocationCode"
                :id="`relationships-location-${curLocationCode}`"
              >
                <div :data-scroll="`relationships-location-${curLocationCode}-heading`" class="header-tabs-offset"></div>
                <cds-accordion-item
                  size="lg"
                  class="grouping-13"
                  v-shadow="1"
                  :open="relatedLocationsState[curLocationCode]?.startOpen"
                  :data-focus="`relationships-location-${curLocationCode}-heading`"
                >
                  <div
                    slot="title"
                    class="flex-column-0_25rem"
                    :class="{ invisible: relatedLocationsState[curLocationCode]?.isReloading }"
                  >
                    <span class="label-with-icon">
                      <NetworkEnterprise20 />
                      {{ formatRelatedLocation(curLocationCode) }}
                      <cds-tag
                        :id="`relationships-location-${curLocationCode}-deactivated`"
                        v-if="findLocation(curLocationCode)?.locnExpiredInd === 'Y'"
                        type="purple"
                        title=""
                      >
                        <span>Deactivated</span>
                      </cds-tag>
                    </span>
                  </div>
                  <client-relationship-form
                    v-if="newIndexedRelationship && curLocationCode === 'null'"
                    location-index="null"
                    index="null"
                    :data="newIndexedRelationship"
                    :clientData="data"
                    :validations="[uniqueRelationships.check]"
                    keep-scroll-bottom-position
                    @canceled="handleRelationshipCanceled(newIndexedRelationship)"
                    @save="operateRelatedClient($event)"
                  />
                  <location-relationships-view
                    v-else
                    :data="curList"
                    :client-data="data"
                    :location="findLocation(curLocationCode)"
                    :is-reloading="relatedLocationsState[curLocationCode]?.isReloading"
                    :user-roles="userRoles"
                    :createMode="curLocationCode === 'null'"
                  />
                </cds-accordion-item>
              </cds-accordion>
            </div>
          </template>
          <div class="tab-panel tab-panel--empty" v-else>
            <div class="empty-table-list">
              <summit-svg alt="Summit pictogram" class="standard-svg" />
              <div class="description">
                <div class="inner-description">
                  <p class="heading-02">Nothing to show yet!</p>
                  <p class="body-compact-01" v-if="userHasAuthority">
                    Click “Add client relationship” button to start
                  </p>
                  <p class="body-compact-01" v-else>
                    No relationships have been added to this client account
                  </p>
                </div>
                <cds-button
                  v-if="userHasAuthority"
                  id="addClientRelationshipBtn"
                  kind="primary"
                  size="md"
                  @click="addRelationship"
                  :disabled="newIndexedRelationship"
                >
                  <span class="width-unset">Add client relationship</span>
                  <Add16 slot="icon" />
                </cds-button>
              </div>
            </div>
          </div>
        </template>
        <div class="tab-panel tab-panel--empty" v-else>
          <div class="empty-table-list">
            <tools-svg alt="Tools pictogram" class="standard-svg" />
            <div class="description">
              <div class="inner-description">
                <p class="heading-02">Under construction</p>
                <p class="body-compact-01">
                  Check this content in the legacy system. It opens in a new tab.
                </p>
              </div>
              <cds-button
                id="open-related-clients-btn"
                kind="tertiary"
                size="md"
                @click.prevent="openRelatedClientsLegacy"
              >
                <span>Open in legacy system</span>
                <Launch16 slot="icon" />
              </cds-button>
            </div>
          </div>
        </div>
      </div>

      <div id="panel-history" role="tabpanel" aria-labelledby="tab-history" hidden>
        <div class="tab-panel tab-panel--populated" style="padding-top: 2.7rem;">
          <template v-if="data && isHistoryPanelVisible">
            <history-view />
          </template>
        </div>
      </div>
    </div>
  </div>

  <cds-modal
    id="reason-modal"
    aria-labelledby="reason-modal-heading"
    aria-describedby="reason-modal-body"
    size="sm"
    :open="reasonModalActiveInd"
    @cds-modal-closed="reasonModalActiveInd = false"
    v-if="data?.client.clientTypeCode"
  >
    <cds-modal-header>
      <cds-modal-close-button></cds-modal-close-button>
      <cds-modal-heading id="reason-modal-heading">
        Reason for change
      </cds-modal-heading>
    </cds-modal-header>
  
    <cds-modal-body id="reason-modal-body">
      <div class="modal-dropdown-container" v-if="reasonPatchData && reasonPatchData.length > 0">

        <p class="body-compact-01">
          Select a reason for the following 
          <span v-if="reasonPatchData.length == 1">change:</span>
          <span v-if="reasonPatchData.length > 1">changes:</span>
        </p>

        <div v-for="(patch, index) in reasonPatchData" 
            :key="index"
            class="grouping-24">
          <data-fetcher
            :url="`/api/codes/update-reasons/${data.client.clientTypeCode}/${patch.action}`"
            :min-length="0"
            :init-value="[]"
            :init-fetch="true"
            :params="{ method: 'GET' }"
            :disabled="!reasonModalActiveInd"
            #="{ content }"
          >
            <dropdown-input-component
              :id="`input-reason-${index}`"
              :label="getActionLabel(patch.action)"
              :initial-value="
                content?.find((item) => item.code === selectedReasons[index]?.reason)?.name
              "
              required
              required-label
              :model-value="content"
              :enabled="true"
              tip=""
              :validations="[...getValidations(`selectedReasons.${index}.reason`)]"
              style="width: 100% !important"
              @update:selected-value="
                (selectedValue) => {
                  updateSelectedReason(selectedValue, index, patch, selectedReasons);
                }
              "
              @error="reasonCodesValidations[index] = !$event"
            />
          </data-fetcher>
        </div>
      </div>
    </cds-modal-body>

    <cds-modal-footer>
      <cds-modal-footer-button
        id="reasonCancelBtn"
        kind="secondary" 
        data-modal-close 
        class="cds--modal-close-btn">
        Cancel
      </cds-modal-footer-button>
      <cds-modal-footer-button 
        id="reasonSaveBtn"
        kind="primary" 
        class="cds--modal-submit-btn" 
        v-on:click="confirmReasons()"
        :disabled="saveDisabled">
        Save changes
        <Save16 slot="icon" />
      </cds-modal-footer-button> 
    </cds-modal-footer>
  </cds-modal>

</template>
