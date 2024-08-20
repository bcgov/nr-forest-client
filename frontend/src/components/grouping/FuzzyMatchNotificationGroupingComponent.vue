<script setup lang="ts">
import { computed, ref } from "vue";
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
  "businessInformation.registrationNumber": "registration number",
  "businessInformation.individual": "name and birthdate",
  "businessInformation.individualAndDocument": "name, birthdate and identification number",
  "businessInformation.birthdate": "date of birth",
  "businessInformation.clientIdentification": "identification type",
  "businessInformation.clientTypeOfId": "identification type",
  "businessInformation.clientIdNumber": "identification number",
  "identificationType.text": "identification type",
  "identificationProvince.text": "identification province",
  "businessInformation.clientAcronym": "client acronym",
  "businessInformation.doingBusinessAs": "doing business as",
  "businessInformation.workSafeBcNumber": "WorkSafeBC number",
  "businessInformation.federalId": "federal identification number",
  "businessInformation.firstName": "name",
  "businessInformation.lastName": "name",
};

const fieldNameToNamingGroups : Record<string, string> = {
  "businessInformation.businessName": ["businessInformation.businessName"],
  "businessInformation.registrationNumber": ["businessInformation.businessName"],
  "businessInformation.federalId": ["businessInformation.businessName"],
  "businessInformation.individual": [
      "businessInformation.firstName",
      "businessInformation.lastName",
      "businessInformation.birthdate",
      "businessInformation.businessName",
    ],
  "businessInformation.individualAndDocument": [
      "businessInformation.firstName",
      "businessInformation.lastName",
      "businessInformation.birthdate",
      "businessInformation.clientIdentification",
    ],
  "businessInformation.clientIdentification": [
      "businessInformation.identificationType",
      "identificationProvince.text",
      "businessInformation.clientIdentification",
    ],
    "businessInformation.doingBusinessAs": ["businessInformation.doingBusinessAs"],
    "businessInformation.clientAcronym": ["businessInformation.clientAcronym"],
};

const fieldNameToLabel : Record<string, string> = {
  "businessInformation.individual": "name and date of birth",
  "businessInformation.individualAndDocument": "name, date of birth and ID number",
  "businessInformation.clientIdentification": "ID type and ID number",
  "businessInformation.businessName": "client name",
  "businessInformation.federalId": "federal identification number",
};

const createErrorEvent = (fieldList: string[], warning: boolean) =>
  fieldList.map((fieldId) => ({
    fieldId,
    errorMsg: warning ? `There's already a client with this ${fieldNameToDescription[fieldId] || convertFieldNameToSentence(fieldId).toLowerCase()}` : "Client already exists",
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

const getErrorsItemContent = ref((matches: MiscFuzzyMatchResult[]) => {

  const matchValue = matches && matches.length > 0 ? getUniqueClientNumbers(matches) : [];    
  const nonFuzzyMatch = matches.find((match) => !match.result?.fuzzy);

  return nonFuzzyMatch && nonFuzzyMatch.result?.match ? renderErrorItem(
    {
      ...nonFuzzyMatch,
      result: { ...nonFuzzyMatch.result, match: matchValue.join(",") },
    }) : "";
});

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

const mapNumberToUrl = (clientNumber: string, field: string) => {
  return '<a target="_blank" href="' + getLegacyUrl(clientNumber, field) + '">' + clientNumber + "</a>";
};

const renderListItem = (misc: MiscFuzzyMatchResult) => {
  const { result } = misc;
  const clients = [...new Set<string>(result.match.split(","))];
  const clientNumberLabel = clients.length > 1 ? "Client numbers" : "Client number";
  const clientNumbers = clients.map(number => mapNumberToUrl(number, result.field)).join(", ");
  const finalLabel = `${clientNumberLabel} ${clientNumbers} ${clients.length > 1 ? "were" : "was"} found with similar ${fieldNameToDescription[result.field]}.`;
  return finalLabel;
};

const renderErrorItem = (misc: MiscFuzzyMatchResult) => {
  const { result } = misc;  
  const clients = [...new Set<string>(result.match.split(","))];
  const clientNumbers = clients.map(number => mapNumberToUrl(number, result.field)).join(", ");
  return clientNumbers;
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

    for (const rawMatch of event.matches.sort((a, b) => a.fuzzy ? 1 : -1)) {

      const match: MiscFuzzyMatchResult = { result: rawMatch,label: label(fieldNameToLabel[rawMatch.field], rawMatch.fuzzy) };
      if (!rawMatch.fuzzy) {
        fuzzyMatchedError.value.fuzzy = false;
      }
      
      fuzzyMatchedError.value.matches.push(match);
      emitFieldErrors(fieldNameToNamingGroups[rawMatch.field] || [rawMatch.field], fuzzyMatchedError.value.fuzzy);
    }
  } else {
    fuzzyMatchedError.value.show = false;
    fuzzyMatchedError.value.fuzzy = false;
    fuzzyMatchedError.value.matches = [];
  }
};

// watch for fuzzy match error messages
fuzzyBus.on(handleFuzzyErrorMessage);
</script>

<template>
  <cds-actionable-notification
    v-if="fuzzyMatchedError.show"
    :id="`fuzzy-match-notification-${id}`"
    v-shadow="true"
    low-contrast="true"
    hide-close-button="true"
    open="true"
    :kind="fuzzyMatchedError.fuzzy ? 'warning' : 'error'"
    :title="fuzzyMatchedError.fuzzy ? 'Possible matching records found' : 'Client already exists'"
  >
    <div>
      <!-- eslint-disable-next-line vue/no-v-html -->
      <span 
        class="body-compact-01"
        v-if="fuzzyMatchedError.fuzzy"
        v-for="match in fuzzyMatchedError.matches"
        :key="match.result.field"
        v-dompurify-html="getListItemContent(match)"
      ></span>
        <span class="body-compact-01" v-if="fuzzyMatchedError.fuzzy">
          Review them in the Client Management System to determine if you should create a new client.
        </span>
        <span v-else class="body-compact-01">
          It looks like ”{{ businessName }}” has client number 
          <span v-dompurify-html="getErrorsItemContent(fuzzyMatchedError.matches)"></span>.
        </span >
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