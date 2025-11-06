<script setup lang="ts">
import { computed, inject, onMounted, onUnmounted, reactive, ref, watch } from "vue";
import * as jsonpatch from "fast-json-patch";
import type {
  ClientDetails,
  ClientLocation,
  ClientSearchResult,
  CodeNameType,
  CodeNameValue,
  IndexedRelatedClient,
  SaveEvent,
} from "@/dto/CommonTypesDto";
import {
  getTagColorByClientStatus,
  preserveUnchangedData,
  formatLocation,
  highlightMatch,
  searchResultToText,
  buildRelatedClientIndex,
  buildRelatedClientCombination,
  isLocationExpired,
  formatRelatedClient,
  sortCodeNameByName,
} from "@/services/ForestClientService";

import Save16 from "@carbon/icons-vue/es/save/16";
import Close16 from "@carbon/icons-vue/es/close/16";

// Importing validators
import { getValidations } from "@/helpers/validators/StaffFormValidations";
import {
  resetSubmissionValidators,
  submissionValidation,
} from "@/helpers/validators/SubmissionValidators";
import {
  isAscii,
  isMaxSizeMsg,
  isMinSizeMsg,
  optional,
} from "@/helpers/validators/GlobalValidators";
import { useFetchTo } from "@/composables/useFetch";
import type { GoToTab, SaveableComponent } from "./shared";
import { useRouter } from "vue-router";

const props = defineProps<{
  locationIndex: string;
  index: string;
  data: IndexedRelatedClient;
  clientData: ClientDetails;
  validations: Array<Function>;
  keepScrollBottomPosition?: boolean;
}>();

const emit = defineEmits<{
  (e: "save", payload: SaveEvent<IndexedRelatedClient>): void;
  (e: "delete"): void;
  (e: "canceled"): void;
}>();

const router = useRouter();

let formattedOriginalData: IndexedRelatedClient;
const formData = ref<IndexedRelatedClient>();

const noValidation = () => "";

const validateCombinedData =
  props.validations.length === 0
    ? noValidation
    : props.validations[0](
        "Combination",
        buildRelatedClientIndex(props.locationIndex, props.index),
      );
const combinationError = ref<string | undefined>("");

const uniquenessValidation = () => {
  combinationError.value = validateCombinedData(buildRelatedClientCombination(formData.value));
};

// Watch for changes on the input
watch(
  formData,
  () => {
    uniquenessValidation();
  },
  {
    deep: true,
  },
);

const hasAnyChange = ref(false);

const formatInputData = (data: IndexedRelatedClient) => {
  const formattedData: IndexedRelatedClient = JSON.parse(JSON.stringify(data));

  const {
    client: { location: clientLocation },
    relatedClient: { location: relatedClientLocation },
  } = formattedData;

  if (clientLocation) {
    clientLocation.name = formatLocation(clientLocation.code, clientLocation.name);
  }
  if (relatedClientLocation) {
    relatedClientLocation.name = formatLocation(
      relatedClientLocation.code,
      relatedClientLocation.name,
    );
  }

  const stringPercentageOwnership =
    typeof formattedData.percentageOwnership === "number"
      ? String(formattedData.percentageOwnership)
      : "";

  formattedData.percentageOwnership = stringPercentageOwnership as unknown as number;

  return formattedData;
};

const resetFormData = () => {
  formattedOriginalData = formatInputData(props.data);
  formData.value = formatInputData(props.data);
  hasAnyChange.value = false;
};

resetFormData();

watch(
  formData,
  () => {
    const updatedData = preserveUnchangedData(formData.value, formattedOriginalData);
    hasAnyChange.value = JSON.stringify(updatedData) !== JSON.stringify(formattedOriginalData);
  },
  { deep: true },
);

const cancel = () => {
  emit("canceled");
};

// This is only to fulfill the SaveableComponent interface.
const lockEditing = () => {};

const saving = ref(false);

const setSaving = (value: boolean) => {
  saving.value = value;
};

defineExpose<SaveableComponent>({ setSaving, lockEditing });

const formatOutputData = (data: IndexedRelatedClient) => {
  const formattedData: IndexedRelatedClient = JSON.parse(JSON.stringify(data));

  if (typeof formattedData.percentageOwnership === "string") {
    formattedData.percentageOwnership = formattedData.percentageOwnership
      ? Number(formattedData.percentageOwnership)
      : null;
  }

  return formattedData;
};

const save = () => {
  const updatedData = formatOutputData(
    preserveUnchangedData(formData.value, formattedOriginalData),
  );

  const originalData = props.data;

  const patch =
    props.locationIndex === "null" ? null : jsonpatch.compare(originalData, updatedData);

  const action =
    props.locationIndex === "null"
      ? {
          infinitive: "create",
          pastParticiple: "created",
        }
      : {
          infinitive: "update",
          pastParticiple: "updated",
        };

  const operationType = props.locationIndex === "null" ? "insert" : "update";

  emit("save", {
    patch,
    updatedData,
    action,
    operationType,
  });
};

const handleDelete = () => {
  emit("delete");
};

const validation = reactive<Record<string, boolean>>({
  location: !!formData.value?.client.location,
  relationshipType: !!formData.value?.relationship,
  relatedClient: !!formData.value?.relatedClient?.client,
  relatedClientLocation: !!formData.value?.relatedClient?.location,
  percentageOwnership: true,
});

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) => accumulator && currentValue,
    true,
  );

const updateLocation = (value: CodeNameType | undefined) => {
  if (value) {
    formData.value.client.location = value;
  }
};

const updateRelationship = (value: CodeNameType | undefined) => {
  if (value) {
    formData.value.relationship = value;
  }
};

watch(
  () => formData.value.relationship?.code,
  () => {
    formData.value.relatedClient.client = null;
    rawSearchKeyword.value = "";
  },
);

watch(
  () => formData.value.relatedClient.client?.code,
  (relatedClientNumber) => {
    if (!relatedClientNumber) {
      rawSearchKeyword.value = "";
    }
    validation.relatedClient = !!relatedClientNumber;
    formData.value.relatedClient.location = null;
    relatedClientDetails.value = undefined;
    if (relatedClientNumber) {
      fetchRelatedClientDetails();
    }
  },
);

const onUpdateModeValueRelatedClient = () => {
  validation.relatedClient = false;
  formData.value.relatedClient.client = null;
};

const updateRelatedClient = (value: CodeNameValue<ClientSearchResult> | undefined) => {
  if (value) {
    formData.value.relatedClient.client = {
      code: value.value.clientNumber,
      name: value.value.clientFullName, // Note: this should be ignored by the back-end
    };
  }
};

const updateRelatedClientLocation = (value: CodeNameType | undefined) => {
  if (value) {
    formData.value.relatedClient.location = value;
  }
};

watch(
  formData,
  () => {
    resetSubmissionValidators();
  },
  {
    deep: true,
  },
);

const getLocationList = (addresses: ClientLocation[], curLocationCode: string) => {
  const list = addresses
    ?.filter(
      (location) => !isLocationExpired(location) || location.clientLocnCode === curLocationCode,
    )
    .map((location) => ({
      code: location.clientLocnCode,
      name: formatLocation(location.clientLocnCode, location.clientLocnName),
    }));

  return list;
};

const locationList = computed<CodeNameType[]>(() =>
  getLocationList(props.clientData.addresses, props.data.client.location?.code),
);

const getInitialRawSearchKeyword = () => {
  const relatedClient = props.data.relatedClient.client;
  const result = relatedClient ? formatRelatedClient(relatedClient.code, relatedClient.name) : "";
  return result;
};

const rawSearchKeyword = ref(getInitialRawSearchKeyword());
const searchKeyword = computed(() => rawSearchKeyword.value.trim());

const predictiveSearchUri = computed(
  () =>
    `/api/clients/relation/${props.data.client.client.code}` +
    `?type=${formData.value.relationship?.code || ""}` +
    `&value=${encodeURIComponent(searchKeyword.value)}`,
);

const relatedClientDetailsUrl = computed(() => {
  const relatedClientNumber = formData.value.relatedClient.client?.code;
  if (relatedClientNumber) {
    return `/api/clients/details/${relatedClientNumber}`;
  }
  return "";
});

const relatedClientDetails = ref<ClientDetails>();

const { fetch: fetchRelatedClientDetails } = useFetchTo(
  relatedClientDetailsUrl,
  relatedClientDetails,
  {
    skip: !relatedClientDetailsUrl.value,
  },
);

const relatedClientLabel = "Related client";

const lowerCaseLabel = relatedClientLabel.toLowerCase();

const validationsOnChange = [isAscii(lowerCaseLabel), isMaxSizeMsg(lowerCaseLabel, 50)];

const relatedClientValidations = [
  optional(isMinSizeMsg(lowerCaseLabel, 3)),
  ...validationsOnChange,
];

/**
 * Converts a client search result to a code/name/value representation.
 * @param searchResult The client search result
 */
const searchResultToCodeNameValue = (
  searchResult: ClientSearchResult,
): CodeNameValue<ClientSearchResult> => {
  const { clientNumber, clientFullName } = searchResult;
  const result = {
    code: clientNumber,
    name: formatRelatedClient(clientNumber, clientFullName),
    value: searchResult,
  };
  return result;
};

const searchResultToCodeNameValueList = (
  list: ClientSearchResult[],
): CodeNameValue<ClientSearchResult>[] => list?.map(searchResultToCodeNameValue);

const relatedClientLocationList = computed(() =>
  getLocationList(relatedClientDetails.value?.addresses, props.data.relatedClient.location?.code),
);

const displayNewLocationModal = ref(false);

const handleNewLocation = () => {
  displayNewLocationModal.value = true;
};

const displayNewClientModal = ref(false);

const handleNewClient = () => {
  displayNewClientModal.value = true;
};

const goToTab = inject<GoToTab>("goToTab");

const confirmNewLocation = () => {
  goToTab("locations");
  displayNewLocationModal.value = false;
  emit("canceled");
};

const confirmNewClient = () => {
  router.push("/new-client-staff");
  displayNewClientModal.value = false;
  emit("canceled");
};
</script>

<template>
  <div class="form-edit no-padding">
    <dropdown-input-component
      :id="`rc-${locationIndex}-${index}-location`"
      label="Location"
      :initial-value="
        locationList?.find((item) => item.code === formData.client.location?.code)?.name
      "
      :model-value="locationList"
      :validations="[
        ...getValidations('relatedClients.*.*.client.location'),
        submissionValidation(
          `relatedClients[${formData.client.location?.code}][${index}].client.location`,
        ),
      ]"
      :error-message="combinationError"
      required
      required-label
      @update:selected-value="updateLocation($event)"
      @empty="validation.location = !$event"
      @error="validation.location = !$event"
    >
      <template #tip>
        Select the location created for this relationship or
        <a id="createLocationLink" href="#" @click.prevent="handleNewLocation">
          create a new location
        </a>
      </template>
    </dropdown-input-component>
    <data-fetcher
      :url="`/api/codes/relationship-types/${props.clientData.client.clientTypeCode}`"
      :min-length="0"
      :init-value="[]"
      :init-fetch="true"
      :params="{ method: 'GET' }"
      #="{ content }"
    >
      <dropdown-input-component
        :id="`rc-${locationIndex}-${index}-relationship`"
        label="Relationship type"
        :initial-value="content?.find((item) => item.code === formData.relationship?.code)?.name"
        required
        required-label
        :model-value="sortCodeNameByName(content)"
        :enabled="true"
        tip=""
        :validations="[
          ...getValidations('relatedClients.*.*.client.relationship'),
          submissionValidation(
            `relatedClients[${formData.client.location?.code}][${index}].client.relationship`,
          ),
        ]"
        @update:selected-value="updateRelationship($event)"
        @empty="validation.relationshipType = !$event"
      />
    </data-fetcher>
    <data-fetcher
      v-model:url="predictiveSearchUri"
      :min-length="3"
      :init-value="[]"
      :init-fetch="false"
      :disabled="searchKeyword?.length < 3 || !!formData.relatedClient?.client?.code"
      #="{ content, loading, error }"
    >
      <AutoCompleteInputComponent
        :id="`rc-${locationIndex}-${index}-relatedClient`"
        :label="relatedClientLabel"
        autocomplete="off"
        required
        required-label
        v-model="rawSearchKeyword"
        :contents="searchResultToCodeNameValueList(content)"
        :validations="[
          ...getValidations('relatedClients.*.*.relatedClient.client'),
          submissionValidation(
            `relatedClients[${formData.client.location?.code}][${index}].relatedClient.client`,
          ),
        ]"
        :error-message="combinationError"
        :validations-on-change="validationsOnChange"
        :loading="loading"
        @update:selected-value="updateRelatedClient($event)"
        @update:model-value="onUpdateModeValueRelatedClient"
      >
        <template #default="{ value }">
          <div class="search-result-item" v-if="value">
            <span
              v-dompurify-html="highlightMatch(searchResultToText(value), searchKeyword)"
            ></span>
            <cds-tag :type="getTagColorByClientStatus(value.clientStatus)" title="">
              <span>{{ value.clientStatus }}</span>
            </cds-tag>
          </div>
        </template>
        <template #tip>
          Start typing the client’s number or name. You can also
          <a id="createClientLink" href="#" @click.prevent="handleNewClient">
            create a new client
          </a>
        </template>
      </AutoCompleteInputComponent>
    </data-fetcher>
    <dropdown-input-component
      :id="`rc-${locationIndex}-${index}-relatedClient-location`"
      label="Related client's location"
      tip=""
      :initial-value="
        relatedClientLocationList?.find(
          (item) => item.code === formData.relatedClient.location?.code,
        )?.name
      "
      :model-value="relatedClientLocationList"
      :validations="[
        ...getValidations('relatedClients.*.*.relatedClient.location'),
        submissionValidation(
          `relatedClients[${formData.client.location?.code}][${index}].relatedClient.location`,
        ),
      ]"
      :error-message="combinationError"
      required
      required-label
      @update:selected-value="updateRelatedClientLocation($event)"
      @empty="validation.relatedClientLocation = !$event"
      @error="validation.relatedClientLocation = !$event"
    />
    <div class="horizontal-input-grouping-1_5">
      <text-input-component
        :id="`rc-${locationIndex}-${index}-percentageOwnership`"
        label="Percentage owned"
        mask="###"
        type="tel"
        placeholder=""
        tip="For example “50”"
        autocomplete="off"
        v-model="formData.percentageOwnership"
        :validations="[
          ...getValidations('relatedClients.*.*.percentageOwnership'),
          submissionValidation(
            `relatedClients[${formData.client.location?.code}][${index}].percentageOwnership`,
          ),
        ]"
        enabled
        @empty="validation.percentageOwnership = true"
        @error="validation.percentageOwnership = !$event"
      />
      <toggle-component
        :id="`rc-${locationIndex}-${index}-hasSigningAuthority`"
        label="Client has signing authority"
        v-model="formData.hasSigningAuthority"
      />
    </div>
    <div class="form-group-buttons form-group-buttons--stretched">
      <cds-button
        :id="`rc-${locationIndex}-${index}-SaveBtn`"
        kind="primary"
        size="md"
        @click="save"
        :disabled="saving || !hasAnyChange || !checkValid()"
      >
        <span class="width-unset">Save changes</span>
        <Save16 slot="icon" />
      </cds-button>
      <cds-button
        :id="`rc-${locationIndex}-${index}-CancelBtn`"
        kind="tertiary"
        size="md"
        @click="cancel"
        :disabled="saving"
      >
        <span class="width-unset">Cancel</span>
        <Close16 slot="icon" />
      </cds-button>
      <cds-button
        v-if="props.locationIndex !== 'null'"
        :id="`rc-${locationIndex}-${index}-DeleteBtn`"
        kind="danger--tertiary"
        size="md"
        @click="handleDelete"
        :disabled="saving"
      >
        <span class="width-unset">Delete relationship</span>
        <Trash16 slot="icon" />
      </cds-button>
    </div>
  </div>
  <cds-modal
    id="modal-new-location"
    aria-labelledby="modal-new-location-heading"
    size="sm"
    :open="displayNewLocationModal"
    @cds-modal-closed="displayNewLocationModal = false"
  >
    <cds-modal-header>
      <cds-modal-close-button></cds-modal-close-button>
      <cds-modal-heading id="modal-new-location-heading">
        Create a new location for a relationship
      </cds-modal-heading>
    </cds-modal-header>

    <cds-modal-body id="modal-new-location-body" class="column-gap-2-rem">
      <cds-inline-notification
        v-shadow="2"
        id="newLocationNotification"
        low-contrast="true"
        open="true"
        kind="warning"
        hide-close-button="true"
        title="The relationship will not be saved"
      >
        <div class="body-compact-01">Start again once the new location has been created.</div>
      </cds-inline-notification>
      <span>You will be redirected to this client's “Client Locations” tab.</span>
    </cds-modal-body>

    <cds-modal-footer>
      <cds-modal-footer-button
        kind="secondary"
        data-modal-close
        class="cds--modal-close-btn"
        :disabled="saving"
      >
        Cancel
      </cds-modal-footer-button>

      <cds-modal-footer-button
        kind="danger"
        class="cds--modal-submit-btn"
        v-on:click="confirmNewLocation"
        :danger-descriptor="`Discard relationship and redirect`"
        :disabled="saving"
      >
        Discard relationship and redirect
        <Trash16 slot="icon" />
      </cds-modal-footer-button>
    </cds-modal-footer>
  </cds-modal>
  <cds-modal
    id="modal-new-client"
    aria-labelledby="modal-new-client-heading"
    size="sm"
    :open="displayNewClientModal"
    @cds-modal-closed="displayNewClientModal = false"
  >
    <cds-modal-header>
      <cds-modal-close-button></cds-modal-close-button>
      <cds-modal-heading id="modal-new-client-heading">
        Create a new client for a relationship
      </cds-modal-heading>
    </cds-modal-header>

    <cds-modal-body id="modal-new-client-body" class="column-gap-2-rem">
      <cds-inline-notification
        v-shadow="2"
        id="newClientNotification"
        low-contrast="true"
        open="true"
        kind="warning"
        hide-close-button="true"
        title="The relationship will not be saved"
      >
        <div class="body-compact-01">
          Start again once the new client has been created and approved.
        </div>
      </cds-inline-notification>
      <span>You will be redirected to the “Create client” page.</span>
    </cds-modal-body>

    <cds-modal-footer>
      <cds-modal-footer-button
        kind="secondary"
        data-modal-close
        class="cds--modal-close-btn"
        :disabled="saving"
      >
        Cancel
      </cds-modal-footer-button>

      <cds-modal-footer-button
        kind="danger"
        class="cds--modal-submit-btn"
        v-on:click="confirmNewClient"
        :danger-descriptor="`Discard relationship and redirect`"
        :disabled="saving"
      >
        Discard relationship and redirect
        <Trash16 slot="icon" />
      </cds-modal-footer-button>
    </cds-modal-footer>
  </cds-modal>
</template>
