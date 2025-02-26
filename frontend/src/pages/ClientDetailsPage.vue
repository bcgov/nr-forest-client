<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { AxiosError } from "axios";
import * as jsonpatch from "fast-json-patch";

// Carbon
import "@carbon/web-components/es/components/breadcrumb/index";
import "@carbon/web-components/es/components/notification/index";
import "@carbon/web-components/es/components/button/index";
import "@carbon/web-components/es/components/tabs/index";
import "@carbon/web-components/es/components/tag/index";
import "@carbon/web-components/es/components/accordion/index";
import summit from "@carbon/pictograms/es/summit";
import tools from "@carbon/pictograms/es/tools";

// Composables
import { useFetchTo, useJsonPatch } from "@/composables/useFetch";
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
import Launch16 from "@carbon/icons-vue/es/launch/16";

import { greenDomain } from "@/CoreConstants";
import {
  adminEmail,
  extractReasonFields,
  getObfuscatedEmailLink,
  includesAnyOf,
  toTitleCase,
  getActionLabel,
  getAction,
  getOldValue,
  reasonRequiredFields
} from "@/services/ForestClientService";
import ForestClientUserSession from "@/helpers/ForestClientUserSession";

import type { ClientDetails, ClientLocation, FieldUpdateReason, ModalNotification } from "@/dto/CommonTypesDto";

// Page components
import SummaryView from "@/pages/client-details/SummaryView.vue";
import LocationView from "@/pages/client-details/LocationView.vue";
import ContactView from "@/pages/client-details/ContactView.vue";
import { isNotEmpty } from "@/helpers/validators/GlobalValidators";

// Route related
const router = useRouter();
const clientNumber = router.currentRoute.value.params.id;

const toastBus = useEventBus<ModalNotification>("toast-notification");
const revalidateBus = useEventBus<string[] | undefined>("revalidate-bus");

const data = ref<ClientDetails>(undefined);

const userRoles = ForestClientUserSession.authorities;

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

const clientFullName = computed(() => {
  if (data.value) {
    const { legalFirstName, legalMiddleName, clientName } = data.value;
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
});

const formatCount = (count = 0) => {
  return String(count).padStart(2, "0");
};

const formatAddress = (location: ClientLocation) => {
  const { addressOne, city, province, country, postalCode } = location;
  const list = [addressOne, city, province, country, postalCode];
  return list.join(", ");
};

const pluralize = (word: string, count = 0) => {
  if (count === 1) {
    return word;
  }
  return `${word}s`;
};

const compareString = (a: string, b: string) => {
  if (a < b) {
    return -1;
  }
  if (a > b) {
    return 1;
  }
  return 0;
};

const sortedLocations = computed(() =>
  data.value.addresses?.toSorted((a, b) => compareString(a.clientLocnCode, b.clientLocnCode)),
);

const sortedContacts = computed(() =>
  data.value.contacts?.toSorted((a, b) => compareString(a.contactName, b.contactName)),
);

const formatLocation = (location: ClientLocation) => {
  const parts = [location.clientLocnCode];
  if (location.clientLocnName) {
    parts.push(location.clientLocnName);
  }

  const title = parts.join(" - ");

  return title;
};

const formatLocationsList = (
  locationCodes: string[],
  allLocations: ClientLocation[] = data.value.addresses,
) => {
  const list: string[] = [];
  if (Array.isArray(locationCodes)) {
    for (const curLocationCode of locationCodes.toSorted()) {
      const location = allLocations.find(
        (curLocation) => curLocation.clientLocnCode === curLocationCode,
      );

      const title = formatLocation(location);
      list.push(title);
    }
  }
  return list.join(", ");
};

const associatedLocationsRecord = computed(() => {
  const result: Record<string, string> = {};
  sortedContacts.value?.forEach((contact, index) => {
    result[index] = formatLocationsList(contact.locationCode);
  });
  return result;
});

const openRelatedClientsLegacy = () => {
  const url = `https://${greenDomain}/int/client/client04RelatedClientListAction.do?bean.clientNumber=${clientNumber}`;
  window.open(url, "_blank", "noopener");
};

const openMaintenanceLegacy = () => {
  const url = `https://${greenDomain}/int/client/client02MaintenanceAction.do?bean.clientNumber=${clientNumber}`;
  window.open(url, "_blank", "noopener");
};

const summitSvg = useSvg(summit);
const toolsSvg = useSvg(tools);

const summaryRef = ref<InstanceType<typeof SummaryView> | null>(null);

const reasonModalActiveInd = ref(false);

let reasonPatchData = ref<jsonpatch.Operation[]>([]);
let originalPatchData: jsonpatch.Operation[] = []; 
const finalPatchData = ref<jsonpatch.Operation[]>([]);
const selectedReasons = ref<FieldUpdateReason[]>([]);
const saveDisabled = ref(false);
const isSaveFirstClick = ref(false);

const updateSelectedReason = (
  selectedValue: string,
  content: Array<{ code: string, name: string }>,
  index: number,
  patch: any,
  selectedReasons: any[]
) => {

  const selectedOption = content.find((option) => option.name === selectedValue);
  
  if (selectedOption) {
    selectedReasons[index] = {
      field: patch.path.replace("/", ""),
      reason: selectedOption.code,
    };
  } 
  else {
    selectedReasons[index] = { field: patch.path.replace("/", ""), reason: "" };
  }
};

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
const confirmReasons = (reasons: FieldUpdateReason[]) => {
  isSaveFirstClick.value = false;
  const reasonInputIdList = reasonPatchData.value.map((_, index) => `input-reason-${index}`);
  revalidateBus.emit(reasonInputIdList);

  if (!checkReasonCodesValidations()) {
    saveDisabled.value = true;
    return;
  }

  // Continue with the patch process
  const updatedPatchData = [...reasonPatchData.value];

  updatedPatchData.forEach((patch, index) => {
    const reasonEntry = selectedReasons.value[index];
    if (reasonEntry) {
      patch.reason = reasonEntry.reason;
    }
  });

  reasonModalActiveInd.value = false;
  sendPatchRequest(updatedPatchData);
};

const sendPatchRequest = (reasonUpdatedPatchData: jsonpatch.Operation[]) => {
  const baseChanges = originalPatchData.map(({ reason, ...patch }) => patch);

  const reasonChanges = reasonUpdatedPatchData.flatMap((patch, index) => {
    return patch.reason
      ? [
          {
            op: "add",
            path: `/reasons/${index}/field`,
            value: patch.path.replace("/", "")
          },
          {
            op: "add",
            path: `/reasons/${index}/reason`,
            value: patch.reason
          }
        ]
      : [];
  });

  finalPatchData.value = [...baseChanges, ...reasonChanges];

  // Send API request
  const { fetch: patch, response, error } = useJsonPatch(
    `/api/clients/details/${clientNumber}`,
    finalPatchData.value,
    { skip: true }
  );

  patch().then(() => {
    if (response.value.status) {
      const toastNotification: ModalNotification = {
        kind: "Success",
        active: true,
        handler: () => {},
        message: `Client <span class="weight-700">“${clientFullName.value}”</span> was updated`,
        toastTitle: undefined,
      };
      toastBus.emit(toastNotification);
      summaryRef.value.lockEditing();
      data.value = undefined;
      fetchClientData();
    }
    if (error.value.status) {
      const toastNotification: ModalNotification = {
        kind: "Error",
        active: true,
        handler: () => {},
        message: "Failed to update client",
        toastTitle: undefined,
      };
      toastBus.emit(toastNotification);
      globalError.value = error.value;
    }
  });
};

// Function to save
const saveSummary = (patchData: jsonpatch.Operation[]) => {
  //Reset values
  selectedReasons.value = [];
  saveDisabled.value = false;

  originalPatchData = [...patchData];
  const reasonFields = extractReasonFields(patchData, data.value);

  // Initializes the validations array
  reasonCodesValidations.value = Array(reasonFields.length).fill(false);

  if (reasonFields.length > 0) {
    reasonPatchData.value = patchData
      .filter((patch) => reasonRequiredFields.has(patch.path.replace('/', '')))
      .map((patch) => {
        const field = patch.path.replace('/', '');
        const reasonEntry = reasonFields.find((r) => r.field === field);

        if (field === 'clientStatusCode') {
          const oldValue = data.value.clientStatusCode;
          const newValue = patch.value;
          const action = getAction(patch.path, oldValue, newValue);
          return { ...patch, reason: action || '' };
        }

        return { ...patch, reason: reasonEntry?.reason || '' };
      });

    // Prevents focusing input field on the modal
    setTimeout(() => {
      reasonModalActiveInd.value = true;
    }, 0);

    isSaveFirstClick.value = true;
  } else {
    sendPatchRequest(patchData);
  }

  resetGlobalError();
};

const globalError = ref();

const resetGlobalError = () => {
  globalError.value = {};
};

resetGlobalError();
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

        <h1 class="resource-details--title">
          <span>
            {{ toTitleCase(clientFullName) }}
          </span>
        </h1>
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

      <div class="grouping-14" v-if="data">
        <div class="grouping-05-short">
          <div>
            <h2 class="mg-tl-2 heading-05">Client summary</h2>

            <div class="grouping-10">
              <summary-view
                ref="summaryRef"
                :data="data"
                :userRoles="userRoles"
                @save="saveSummary"
              />
            </div>
          </div>
        </div>

        <cds-tabs value="locations" type="contained">
          <cds-tab id="tab-locations" target="panel-locations" value="locations">
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
          <cds-tab id="tab-activity" target="panel-activity" value="activity">
            <div>
              Activity log
              <RecentlyViewed16 />
            </div>
          </cds-tab>
        </cds-tabs>
      </div>
    </div>
    <div class="tab-panels-container" v-if="data">
      <div id="panel-locations" role="tabpanel" aria-labelledby="tab-locations" hidden>
        <div class="tab-panel tab-panel--populated">
          <h3 class="padding-left-1rem">
            {{ formatCount(data.addresses?.length) }}
            {{ pluralize("location", data.addresses?.length) }}
          </h3>
          <cds-accordion
            v-for="(location, index) in sortedLocations"
            :key="location.clientLocnCode"
            :id="`location-${location.clientLocnCode}`"
          >
            <cds-accordion-item size="lg" class="grouping-13">
              <div slot="title" class="flex-column-0_25rem">
                <span class="label-with-icon">
                  <LocationStar20 v-if="index === 0" />
                  <Location20 v-else />
                  {{ formatLocation(location) }}
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
              <location-view :data="location" />
            </cds-accordion-item>
          </cds-accordion>
        </div>
      </div>
      <div id="panel-contacts" role="tabpanel" aria-labelledby="tab-contacts" hidden>
        <div class="tab-panel tab-panel--populated" v-if="data.contacts?.length">
          <h3 class="padding-left-1rem">
            {{ formatCount(data.contacts?.length) }}
            {{ pluralize("contact", data.contacts?.length) }}
          </h3>
          <cds-accordion
            v-for="(contact, index) in sortedContacts"
            :key="contact.contactName"
            :id="`contact-${index}`"
          >
            <cds-accordion-item size="lg" class="grouping-13">
              <div slot="title" class="flex-column-0_25rem">
                <span class="label-with-icon">
                  <User20 />
                  {{ contact.contactName }}
                </span>
                <span
                  :id="`contact-${index}-title-locations`"
                  class="hide-open body-compact-01 padding-left-1_625rem"
                >
                  {{ associatedLocationsRecord[index] }}
                </span>
              </div>
              <contact-view
                :data="contact"
                :index="index"
                :associatedLocationsString="associatedLocationsRecord[index]"
              />
            </cds-accordion-item>
          </cds-accordion>
        </div>
        <div class="tab-panel tab-panel--empty" v-else>
          <div class="empty-table-list">
            <summit-svg alt="Summit pictogram" class="standard-svg" />
            <div class="inner-description">
              <p class="heading-02">Nothing to show yet!</p>
              <p class="body-compact-01" v-if="userHasAuthority">
                Click “Add contact” button to start
              </p>
              <p class="body-compact-01" v-else>
                No contacts have been added to this client account
              </p>
            </div>
          </div>
        </div>
      </div>
      <div id="panel-related" role="tabpanel" aria-labelledby="tab-related" hidden>
        <div class="tab-panel tab-panel--empty">
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
      <div id="panel-activity" role="tabpanel" aria-labelledby="tab-activity" hidden>
        <div class="tab-panel tab-panel--empty">
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
                id="open-maintenance-btn"
                kind="tertiary"
                size="md"
                @click.prevent="openMaintenanceLegacy"
              >
                <span>Open in legacy system</span>
                <Launch16 slot="icon" />
              </cds-button>
            </div>
          </div>
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
    v-if="data?.clientTypeCode"
  >
    <cds-modal-header>
      <cds-modal-close-button></cds-modal-close-button>
      <cds-modal-heading id="reason-modal-heading">
        Reason for change
      </cds-modal-heading>
    </cds-modal-header>
  
    <cds-modal-body id="reason-modal-body">
      <div v-if="reasonPatchData && reasonPatchData.length > 0">

        <p class="body-compact-01">
          Select a reason for the following 
          <span v-if="reasonPatchData.length == 1">change:</span>
          <span v-if="reasonPatchData.length > 1">changes:</span>
        </p>

        <div v-for="(patch, index) in reasonPatchData" 
            :key="index"
            class="grouping-24">
          <data-fetcher
            :url="`/api/codes/update-reasons/${data.clientTypeCode}/${getAction(patch.path, getOldValue(patch.path, data), patch.value)}`"
            :min-length="0"
            :init-value="[]"
            :init-fetch="true"
            :params="{ method: 'GET' }"
            #="{ content }"
          >
            <dropdown-input-component
              :id="`input-reason-${index}`"
              :label="getActionLabel(patch.reason)"
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
              @update:model-value="
                (selectedValue) => {
                  updateSelectedReason(selectedValue, content, index, patch, selectedReasons);
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
        kind="secondary" 
        data-modal-close 
        class="cds--modal-close-btn">
        Cancel
      </cds-modal-footer-button>
      <cds-modal-footer-button 
        id="reasonSaveBtn"
        kind="primary" 
        class="cds--modal-submit-btn" 
        v-on:click="confirmReasons(reasonPatchData)"
        :disabled="saveDisabled">
        Save changes
        <Logout16 slot="icon" />
      </cds-modal-footer-button> 
    </cds-modal-footer>
  </cds-modal>

</template>
