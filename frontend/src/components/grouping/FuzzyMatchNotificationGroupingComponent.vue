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
  businessName?: string;
}>();

const fuzzyBus = useEventBus<FuzzyMatcherEvent>("fuzzy-error-notification");
const errorBus = useEventBus<ValidationMessageType[]>("submission-error-notification");

const fuzzyMatchedError = ref<FuzzyMatcherData>(
  props.error ?? {
    show: false,
    fuzzy: false,
    matches: [],
    description: "",
  },
);

defineExpose({
  fuzzyMatchedError,
});

const customError = ref<{title?:string, message?: string}>({});

const fieldNameToDescription : Record<string, string> = {
  "businessInformation.businessName": "client name",
  "businessInformation.notOwnedByPerson": "client name",
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
  "location.addresses[].streetAddress": "address",
  "location.addresses[].city": "address",
  "location.addresses[].country": "address",
  "location.addresses[].province": "address",
  "location.addresses[].postalCode": "address",
  "location.addresses[].businessPhoneNumber": "primary phone number",
  "location.addresses[].secondaryPhoneNumber": "secondary phone number",
  "location.addresses[].faxNumber": "fax",
  "location.addresses[].emailAddress": "email address",
  "location.contacts[].firstName": "contact",
  "location.contacts[].lastName": "contact",
  "location.contacts[].phoneNumber": "primary phone number",
  "location.contacts[].secondaryPhoneNumber": "secondary phone number",
  "location.contacts[].faxNumber": "fax",
  "location.contacts[].email": "email address",

};

const fieldNameToNamingGroups: Record<string, string[]> = {
  "businessInformation.notOwnedByPerson": ["businessInformation.businessName"],
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
  "location.addresses[].streetAddress": [
    "location.addresses[].streetAddress",
    "location.addresses[].city",
    "location.addresses[].province",
    "location.addresses[].country",
    "location.addresses[].postalCode",
  ],
  "location.contacts[].firstName": [
    "location.contacts[].firstName",
    "location.contacts[].lastName",
    "location.contacts[].email",
    /*
    Phone numbers are not included here because the error does not specify which phone matched.
    However the application should still be able to tell which phone matched since a specific rule
    for the specific phone should be matched at the same time.
    */
  ],
};

const fieldNameToLabel: Record<string, string> = {
  "businessInformation.notOwnedByPerson": "client",
  "businessInformation.individual": "name and date of birth",
  "businessInformation.individualAndDocument": "name, date of birth and ID number",
  "businessInformation.clientIdentification": "ID type and ID number",
  "businessInformation.businessName": "client name",
  "businessInformation.federalId": "federal identification number",
  "location.addresses[].streetAddress": "address",
  "location.addresses[].businessPhoneNumber": "primary phone number",
  "location.addresses[].secondaryPhoneNumber": "secondary phone number",
  "location.addresses[].faxNumber": "fax",
  "location.addresses[].emailAddress": "email address",
  "location.contacts[].firstName": "contact",
  "location.contacts[].businessPhoneNumber": "primary phone number",
  "location.contacts[].secondaryPhoneNumber": "secondary phone number",
  "location.contacts[].faxNumber": "fax",
  "location.contacts[].email": "email address",
};

const arrayIndexRegex = /\[(\d+)\]/;

const createErrorEvent = (fieldList: string[], warning: boolean) =>
  fieldList.map((fieldId) => {
    const genericField = fieldId.replace(arrayIndexRegex, "[]");
    return {
      fieldId,
      errorMsg: warning
        ? `There's already a client with this ${fieldNameToDescription[genericField] || convertFieldNameToSentence(fieldId).toLowerCase()}`
        : "Client already exists",
    };
  });


const emitFieldErrors = (fieldList: string[], warning: boolean) => {
  const errorEvent = createErrorEvent(fieldList, warning);
  errorBus.emit(errorEvent, {
    skipNotification: true,
    warning,
  });
};

const label = (matchedFieldsText: string, partialMatch: boolean) => {

  if(!matchedFieldsText)
  return '';

  const prefix = partialMatch ? "Partial matching on" : "Matching on";
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
  const cleanFieldName = result.field.replace(arrayIndexRegex, "[]");
  const clients = [...new Set<string>(result.match.split(","))];
  const clientNumberLabel = "Client number" + (clients.length > 1 ? "s" : "");
  const clientNumbers = clients.map(number => mapNumberToUrl(number, result.field)).join(", ");
  const finalLabel = `${clientNumberLabel} ${clientNumbers} ${clients.length > 1 ? "were" : "was"} found with similar ${fieldNameToDescription[cleanFieldName]}.`;
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

const clearNotification = () => {
  fuzzyMatchedError.value.show = false;
  fuzzyMatchedError.value.fuzzy = false;
  fuzzyMatchedError.value.matches = [];
  customError.value = {};
};

const handleFuzzyErrorMessage = (event: FuzzyMatcherEvent | undefined, payload?: any) => {
  if (!event) {
    clearNotification();
    return;
  }
  if (event.id === props.id) {
    if (!event.matches?.length) {
      clearNotification();
      return;
    }
    fuzzyMatchedError.value.show = true;
    fuzzyMatchedError.value.fuzzy = true;
    fuzzyMatchedError.value.matches = [];

    if(payload && payload.title && payload.message){
      customError.value = payload;
    }

    for (const rawMatch of event.matches.sort((a, b) => a.fuzzy ? 1 : -1)) {

      const genericField = rawMatch.field.replace(arrayIndexRegex, "[]");

      const match: MiscFuzzyMatchResult = {
        result: rawMatch,
        label: label(fieldNameToLabel[genericField], rawMatch.partialMatch),
      };

      if (!rawMatch.fuzzy) {
        fuzzyMatchedError.value.fuzzy = false;
      }

      fuzzyMatchedError.value.matches.push(match);

      let fieldsList = fieldNameToNamingGroups[genericField];

      if (genericField !== rawMatch.field && fieldsList) {
        const regexGroups = rawMatch.field.match(arrayIndexRegex);
        const id = regexGroups[1];
        fieldsList = fieldsList.map((field) => field.replace("[]", `[${id}]`));
      }

      /*
      Note: if the fieldsList is empty, it will use a single field with the same name returned from
      the API.
      */
     //if is better to let warning fields be colored as warning, change to rawMatch.fuzzy
      emitFieldErrors(fieldsList || [rawMatch.field], fuzzyMatchedError.value.fuzzy);
    }
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
    :title="customError.title || (fuzzyMatchedError.fuzzy ? 'Possible matching records found' : 'Client already exists')"
  >
    <div>
      <!-- eslint-disable-next-line vue/no-v-html -->
      <span class="body-compact-01" v-if="customError.message">{{ customError.message }}</span>
      <span 
        class="body-compact-01"
        v-if="fuzzyMatchedError.fuzzy && !customError.message"
        v-for="match in fuzzyMatchedError.matches"
        :key="match.result.field"
        v-dompurify-html="getListItemContent(match)"
      ></span>
        <span class="body-compact-01" v-if="fuzzyMatchedError.fuzzy && !customError.message">
          Review them in the Client Management System to determine if you should create a new client.
        </span>
        <span v-if="!fuzzyMatchedError.fuzzy && !customError.message" class="body-compact-01">
          It looks like ”{{ businessName }}” has client number 
          <span v-dompurify-html="getErrorsItemContent(fuzzyMatchedError.matches)"></span>.
        </span >
      <span v-if="!fuzzyMatchedError.fuzzy && !customError.message" class="body-compact-02">
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