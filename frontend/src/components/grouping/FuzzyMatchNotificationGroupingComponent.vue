<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
// Carbon
import "@carbon/web-components/es/components/notification/index";
// Composables
import { useEventBus } from "@vueuse/core";
// Types
import type {
  FuzzyMatcherData,
  FuzzyMatcherEvent,
  ValidationMessageType,
  MiscFuzzyMatchResult,
} from "@/dto/CommonTypesDto";
import { greenDomain } from "@/CoreConstants";
import { convertFieldNameToSentence } from "@/services/ForestClientService";

const props = defineProps<{
  id: string;
  error?: FuzzyMatcherData;
  businessName: string;
}>();

const fuzzyBus = useEventBus<FuzzyMatcherEvent>("fuzzy-error-notification");
const errorBus = useEventBus<ValidationMessageType[]>("submission-error-notification");

const fuzzyMatchedError = ref<FuzzyMatcherData>(
  props.error ?? {
    show: false,
    fuzzy: false,
    matches: [],
  },
);

const fieldNameToDescription : Record<string, string> = {
  "businessInformation.businessName": "client name",
  "businessInformation.firstName": "name",
  "businessInformation.lastName": "name",
  "businessInformation.birthdate": "date of birth",
  "businessInformation.clientIdentification": "identification type",
  "businessInformation.clientTypeOfId": "identification type",
  "businessInformation.clientIdNumber": "identification number",
  "identificationType.text": "identification type",
  "identificationProvince.text": "identification province",
  "businessInformation.clientAcronym": "client acronym",
  "businessInformation.doingBusinessAs": "doing business as",
  "businessInformation.workSafeBcNumber": "WorkSafeBC number",
};

const fieldNameToNamingGroups : Record<string, string> = {
  "businessInformation.businessName": ["businessInformation.businessName"],
  "businessInformation.firstName": [
      "businessInformation.firstName",
      "businessInformation.lastName",
      "businessInformation.birthdate"
    ],
  "businessInformation.lastName": [
      "businessInformation.firstName",
      "businessInformation.lastName",
      "businessInformation.birthdate",
      "businessInformation.identificationType",
      "businessInformation.clientTypeOfId",
      "businessInformation.clientIdNumber",
      "identificationType.text", 
      "identificationProvince.text",
      "businessInformation.clientIdentification",
    ],
  "businessInformation.clientIdentification": [
      "businessInformation.identificationType",
      "businessInformation.clientTypeOfId",
      "businessInformation.clientIdNumber",
      "identificationType.text", 
      "identificationProvince.text",
      "businessInformation.clientIdentification",
    ],
    "businessInformation.doingBusinessAs": ["businessInformation.doingBusinessAs"],
    "businessInformation.clientAcronym": ["businessInformation.clientAcronym"],
};

const fieldNameToLabel : Record<string, string> = {
  "businessInformation.firstName": "name and date of birth",
  "businessInformation.lastName": "name, date of birth and ID number",
  "businessInformation.clientIdentification": "ID type and ID number",
};

const createErrorEvent = (fieldList: string[], warning: boolean) =>
  fieldList.map((fieldId) => ({
    fieldId,
    errorMsg: warning ? `There's already a client with this "${fieldNameToDescription[fieldId]}"` : "Client already exists",
  }));

const emitFieldErrors = (fieldList: string[], warning: boolean) => {
  const errorEvent = createErrorEvent(fieldList, warning);
  errorBus.emit(errorEvent, {
    skipNotification: true,
    warning,
  });
};

const label = (matchedFieldsText: string, warning: boolean) => {

  if(!matchedFieldsText)
  return '';

  const prefix = warning ? "Partial matching on" : "Matching on";
  return `${prefix} ${matchedFieldsText}`;
}


const getListItemContent = ref((match: MiscFuzzyMatchResult) => {
  return match && match.result?.match ? renderListItem(match) : "";
});

const getLegacyUrl = (duplicatedClient, label) => {
  const encodedClientNumber = encodeURIComponent(duplicatedClient.trim());
  switch (label) {
    case "contact":
      return `https://${greenDomain}/int/client/client06ContactListAction.do?bean.clientNumber=${encodedClientNumber}`;
    case "location":
      return `https://${greenDomain}/int/client/client07LocationListAction.do?bean.clientNumber=${encodedClientNumber}`;
    default:
      return `https://${greenDomain}/int/client/client02MaintenanceAction.do?bean.clientNumber=${encodedClientNumber}`;
  }
};

const renderListItem = (misc: MiscFuzzyMatchResult) => {
  const { result } = misc;
  let finalLabel = "";
  if (misc.label) {
    finalLabel = misc.label;
  } else if (result.field === "contact" || result.field === "location") {
    finalLabel = "Matching one or more " + result.field + "s";
  } else {
    finalLabel =
      (result.fuzzy ? "Partial m" : "M") +
      "atching on " +
      convertFieldNameToSentence(result.field).toLowerCase();
  }

  finalLabel += " - Client number: ";

  const clients = [...new Set<string>(result.match.split(","))];
  finalLabel += clients
    .map(
      (clientNumber) =>
        '<a target="_blank" href="' +
        getLegacyUrl(clientNumber, result.field) +
        '">' +
        clientNumber +
        "</a>",
    )
    .join(", ");

  return finalLabel;
};

/**
 * Gets unique client numbers across all the matches
 * @param matches
 */
const getUniqueClientNumbers = (matches: MiscFuzzyMatchResult[]) => {
  const results: string[] = [];

  matches.forEach((data) => {
    results.push(...data.result.match.split(","));
  });

  return [...new Set(results)];
};

const uniqueClientNumbers = computed(() => getUniqueClientNumbers(fuzzyMatchedError.value.matches));


const handleFuzzyErrorMessage = (event: FuzzyMatcherEvent | undefined, _payload?: any) => {
  if (event && event.matches.length > 0 && event.id === props.id) {
        
    fuzzyMatchedError.value.show = true;
    fuzzyMatchedError.value.fuzzy = true;
    fuzzyMatchedError.value.matches = [];

    for (const rawMatch of event.matches) {

      const match: MiscFuzzyMatchResult = { result: rawMatch,label: label(fieldNameToLabel[rawMatch.field], rawMatch.fuzzy) };
      if (!rawMatch.fuzzy) {
        fuzzyMatchedError.value.fuzzy = false;
      }
      
      fuzzyMatchedError.value.matches.push(match);
      emitFieldErrors(fieldNameToNamingGroups[rawMatch.field], rawMatch.fuzzy);
    }
  } else {
    fuzzyMatchedError.value.show = false;
    fuzzyMatchedError.value.fuzzy = false;
    fuzzyMatchedError.value.matches = [];
  }
};

// watch for fuzzy match error messages
fuzzyBus.on(handleFuzzyErrorMessage);

// just for debugs
errorBus.on((errors,params) => {
  console.log("errors", errors,"params", params);
});
</script>

<template>
  <cds-actionable-notification
    v-if="fuzzyMatchedError.show"
    v-shadow="true"
    low-contrast="true"
    hide-close-button="true"
    open="true"
    :kind="fuzzyMatchedError.fuzzy ? 'warning' : 'error'"
    :title="fuzzyMatchedError.fuzzy ? 'Possible matching records found' : 'Client already exists'"
  >
    <div>
      <span class="body-compact-02">
        <template v-if="fuzzyMatchedError.fuzzy">
          {{ uniqueClientNumbers.length }} similar client
          <span v-if="uniqueClientNumbers.length === 1">record was</span>
          <span v-else>records were</span>
          found. Review their information in the Client Management System to determine if you should
          should create a new client:
        </template>
        <template v-else>
          Looks like ”{{ businessName }}” has a client number. Review their information in the
          Management System if necessary:
        </template>
      </span>
      <ul>
        <!-- eslint-disable-next-line vue/no-v-html -->
        <li
          v-for="match in fuzzyMatchedError.matches"
          :key="match.result.field"
          v-dompurify-html="getListItemContent(match)"
        ></li>
      </ul>
      <span v-if="!fuzzyMatchedError.fuzzy" class="body-compact-02">
        You must inform the applicant of their number.
      </span>
    </div>
  </cds-actionable-notification>
</template>

<style scoped>
cds-actionable-notification > div {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
cds-actionable-notification > div > ul {
  margin: 0;
  padding: 0;
  list-style-type: disc;
  margin-left: 1.25rem;
}
</style>