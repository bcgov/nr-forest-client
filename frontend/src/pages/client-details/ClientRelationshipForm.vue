<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import * as jsonpatch from "fast-json-patch";
import type {
  ClientDetails,
  ClientSearchResult,
  CodeNameType,
  CodeNameValue,
  RelatedClientEntry,
  SaveEvent,
} from "@/dto/CommonTypesDto";
import {
  getTagColorByClientStatus,
  preserveUnchangedData,
  formatLocation,
  highlightMatch,
  searchResultToText,
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

const props = defineProps<{
  data: RelatedClientEntry;
  index: string;
  locationIndex: string;
  client: ClientDetails;
  validations: Array<Function>;
  keepScrollBottomPosition?: boolean;
}>();

const emit = defineEmits<{
  (e: "save", payload: SaveEvent<RelatedClientEntry>): void;
  (e: "canceled"): void;
}>();

let originalData: RelatedClientEntry;
const formData = ref<RelatedClientEntry>();

const hasAnyChange = ref(false);

const formatData = (data: RelatedClientEntry) => {
  const formattedData = JSON.parse(JSON.stringify(data));
  return formattedData;
};

const resetFormData = () => {
  originalData = formatData(props.data);
  formData.value = formatData(props.data);
  hasAnyChange.value = false;
};

resetFormData();

watch(
  formData,
  () => {
    const updatedData = preserveUnchangedData(formData.value, originalData);
    hasAnyChange.value = JSON.stringify(updatedData) !== JSON.stringify(originalData);
  },
  { deep: true },
);

const cancel = () => {
  emit("canceled");
};

const saving = ref(false);

const setSaving = (value: boolean) => {
  saving.value = value;
};

defineExpose({ setSaving });

const save = () => {
  const updatedData = preserveUnchangedData(formData.value, originalData);

  const patch = jsonpatch.compare(originalData, updatedData);

  emit("save", {
    patch,
    updatedData,
    action: {
      infinitive: "update",
      pastParticiple: "updated",
    },
    operationType: "update",
  });
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
    console.log("relationship changed, clearing related client");
    formData.value.relatedClient.client = null;
  },
);

watch(
  () => formData.value.relatedClient.client?.code,
  (value) => {
    console.log("related client changed", value);
    if (!value) {
      rawSearchKeyword.value = "";
    }
    validation.relatedClient = !!value;
    formData.value.relatedClient.location = null;
  },
);

const onUpdateModeValueRelatedClient = () => {
  validation.relatedClient = false;
  formData.value.relatedClient.client = null;
};

const updateRelatedClient = (value: CodeNameValue<ClientSearchResult> | undefined) => {
  if (value) {
    console.log("updateRelatedClient", value);
    formData.value.relatedClient.client = {
      code: value.value.clientNumber,
      name: value.value.clientFullName, // Note: this should be ignored by the back-end
    };
  }
};

const updateRelatedClientLocation = (value: CodeNameType | undefined) => {
  if (value) {
    console.log("updateRelatedClientLocation", value);
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

const locationList = computed<CodeNameType[]>(() =>
  props.client.addresses.map((location) => ({
    code: location.clientLocnCode,
    name: formatLocation(location.clientLocnCode, location.clientLocnName),
  })),
);

const rawSearchKeyword = ref("");
const searchKeyword = computed(() => rawSearchKeyword.value.trim());

const predictiveSearchUri = computed(
  () =>
    `/api/clients/relation/${props.data.client.client.code}` +
    `?type=${formData.value.relationship?.code || ""}` +
    `&value=${encodeURIComponent(searchKeyword.value)}`,
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
    name: `${clientNumber}, ${clientFullName}`,
    value: searchResult,
  };
  return result;
};

const searchResultToCodeNameValueList = (
  list: ClientSearchResult[],
): CodeNameValue<ClientSearchResult>[] => list?.map(searchResultToCodeNameValue);

const getClientLocationList = (client: ClientDetails | undefined): CodeNameType[] =>
  client?.addresses?.map((location) => ({
    code: location.clientLocnCode,
    name: formatLocation(location.clientLocnCode, location.clientLocnName),
  }));
</script>

<template>
  <div class="form-edit no-padding">
    <dropdown-input-component
      :id="`rc-${locationIndex}-${index}-location`"
      label="Location"
      tip="Select the location created for this relationship or create a new location"
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
      required
      required-label
      @update:selected-value="updateLocation($event)"
      @empty="validation.location = !$event"
      @error="validation.location = !$event"
    />
    <data-fetcher
      :url="`/api/codes/relationship-types/${props.client.client.clientTypeCode}`"
      :min-length="0"
      :init-value="[]"
      :init-fetch="true"
      :params="{ method: 'GET' }"
      #="{ content }"
    >
      <dropdown-input-component
        :id="`rc-${locationIndex}-${index}-relationship-type`"
        label="Relationship type"
        :initial-value="content?.find((item) => item.code === formData.relationship?.code)?.name"
        required
        required-label
        :model-value="content"
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
        #="{ option }"
      >
        <cds-tag :type="getTagColorByClientStatus(option.name)" title="">
          <span>{{ option.name }}</span>
        </cds-tag>
      </dropdown-input-component>
    </data-fetcher>
    <data-fetcher
      v-model:url="predictiveSearchUri"
      :min-length="3"
      :init-value="[]"
      :init-fetch="false"
      :disabled="!searchKeyword || !!formData.relatedClient?.client?.code"
      #="{ content, loading, error }"
    >
      <AutoCompleteInputComponent
        :id="`rc-${locationIndex}-${index}-related-client`"
        :label="relatedClientLabel"
        autocomplete="off"
        tip="Start typing the client’s number or name. You can also create a new client"
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
        :validations-on-change="validationsOnChange"
        :loading="loading"
        @update:selected-value="updateRelatedClient($event)"
        @update:model-value="onUpdateModeValueRelatedClient"
        #="{ value }"
      >
        <div class="search-result-item" v-if="value">
          <span v-dompurify-html="highlightMatch(searchResultToText(value), searchKeyword)"></span>
          <cds-tag :type="getTagColorByClientStatus(value.clientStatus)" title="">
            <span>{{ value.clientStatus }}</span>
          </cds-tag>
        </div>
      </AutoCompleteInputComponent>
    </data-fetcher>
    <data-fetcher
      :url="`/api/clients/details/${formData.relatedClient?.client?.code}`"
      :min-length="3"
      :init-value="[]"
      :init-fetch="false"
      :disabled="!formData.relatedClient?.client?.code"
      #="{ content, loading, error }"
    >
      <dropdown-input-component
        :id="`rc-${locationIndex}-${index}-related-client-location`"
        label="Related client's location"
        tip=""
        :initial-value="
          getClientLocationList(content)?.find(
            (item) => item.code === formData.relatedClient.location?.code,
          )?.name
        "
        :model-value="getClientLocationList(content)"
        :validations="[
          ...getValidations('relatedClients.*.*.relatedClient.location'),
          submissionValidation(
            `relatedClients[${formData.client.location?.code}][${index}].relatedClient.location`,
          ),
        ]"
        required
        required-label
        @update:selected-value="updateRelatedClientLocation($event)"
        @empty="validation.relatedClientLocation = !$event"
        @error="validation.relatedClientLocation = !$event"
      />
    </data-fetcher>
    <div class="horizontal-input-grouping-1_5">
      <text-input-component
        id="percentageOwnership"
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
        id="hasSigningAuthority"
        label="Client has signing authority"
        v-model="formData.hasSigningAuthority"
      />
    </div>
    <!-- rawSearchKeyword: {{ rawSearchKeyword }}
    <br><br>
    formData.relatedClient?.client?.code: {{ formData.relatedClient?.client?.code }}
    <br><br>
    formData.hasSigningAuthority: {{ formData.hasSigningAuthority }}
    <br><br>
    validation: {{ validation }} -->
    <div class="form-group-buttons form-group-buttons--stretched">
      <cds-button
        id="summarySaveBtn"
        kind="primary"
        size="md"
        @click="save"
        :disabled="saving || !hasAnyChange || !checkValid()"
      >
        <span class="width-unset">Save changes</span>
        <Save16 slot="icon" />
      </cds-button>
      <cds-button
        id="summaryCancelBtn"
        kind="tertiary"
        size="md"
        @click="cancel"
        :disabled="saving"
      >
        <span class="width-unset">Cancel</span>
        <Close16 slot="icon" />
      </cds-button>
    </div>
  </div>
</template>
