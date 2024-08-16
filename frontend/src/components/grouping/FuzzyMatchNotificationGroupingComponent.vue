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
};

const fieldNameToNamingGroups: Record<string, string[]> = {
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
};

const fieldNameToLabel: Record<string, string> = {
  "businessInformation.individual": "name and date of birth",
  "businessInformation.individualAndDocument": "name, date of birth and ID number",
  "businessInformation.clientIdentification": "ID type and ID number",
  "businessInformation.businessName": "client name",
  "businessInformation.federalId": "federal identification number",
};

const createErrorEvent = (fieldList: string[], warning: boolean) =>
  fieldList.map((fieldId) => ({
    fieldId,
    errorMsg: warning ? `There's already a client with this "${fieldNameToDescription[fieldId] || convertFieldNameToSentence(fieldId).toLowerCase()}"` : "Client already exists",
  }));

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
      (result.partialMatch ? "Partial m" : "M") +
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

const clearNotification = () => {
  fuzzyMatchedError.value.show = false;
  fuzzyMatchedError.value.fuzzy = false;
  fuzzyMatchedError.value.matches = [];
  fuzzyMatchedError.value.description = "";
};

interface DescriptionOption {
  condition: (id: string) => boolean;
  getDescription: () => string;
}

const fuzzySuffix =
  "Review their information in the Client Management System to determine if you should create a new client:";

const descriptionOptionList: DescriptionOption[] = [
  {
    condition: (id: string) => id === "global" && fuzzyMatchedError.value.fuzzy,
    getDescription: () => `${uniqueClientNumbers.value.length} similar client
        ${uniqueClientNumbers.value.length === 1 ? "record was" : "records were"}
        found. ${fuzzySuffix}`,
  },
  {
    condition: (id: string) => id === "global" && !fuzzyMatchedError.value.fuzzy,
    getDescription:
      () => `Looks like ”${props.businessName}” has a client number. Review their information in the
        Management System if necessary:`,
  },
  {
    condition: (id: string) => id.startsWith("location.addresses"),
    getDescription: () => `${uniqueClientNumbers.value.length} client
        ${uniqueClientNumbers.value.length === 1 ? "record was" : "records were"}
        found with locations similar to this one. ${fuzzySuffix}`,
  },
];

const handleFuzzyErrorMessage = (event: FuzzyMatcherEvent | undefined, _payload?: any) => {
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

    for (const rawMatch of event.matches) {
      const match: MiscFuzzyMatchResult = {
        result: rawMatch,
        label: label(fieldNameToLabel[rawMatch.field], rawMatch.partialMatch),
      };

      if (!rawMatch.fuzzy) {
        fuzzyMatchedError.value.fuzzy = false;
      }

      fuzzyMatchedError.value.matches.push(match);
      const arrayIndexRegex = /\[(\d+)\]/;
      const genericField = rawMatch.field.replace(arrayIndexRegex, "[]");
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
      emitFieldErrors(fieldsList || [rawMatch.field], rawMatch.fuzzy);
    }

    // Setting the error description
    const option = descriptionOptionList.find((option) => option.condition(props.id));
    if (option) {
      fuzzyMatchedError.value.description = option.getDescription();
    } else {
      console.warn(
        `fuzzy description not found, using the global one (with fuzzy: ${fuzzyMatchedError.value.fuzzy})`,
      );
      const fallbackOption = descriptionOptionList.find((option) => option.condition("global"));
      fuzzyMatchedError.value.description = fallbackOption.getDescription();
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
    :title="fuzzyMatchedError.fuzzy ? 'Possible matching records found' : 'Client already exists'"
  >
    <div>
      <span class="body-compact-02">
        {{ fuzzyMatchedError.description }}
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