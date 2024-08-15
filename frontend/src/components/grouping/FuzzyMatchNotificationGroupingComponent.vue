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

const clearNotification = () => {
  fuzzyMatchedError.value.show = false;
  fuzzyMatchedError.value.fuzzy = false;
  fuzzyMatchedError.value.matches = [];
  fuzzyMatchedError.value.description = "";
};

const handleFuzzyErrorMessage = (event: FuzzyMatcherEvent | undefined, _payload?: any) => {
  if (!event) {
    clearNotification();
    return;
  }
  if (event.id === props.id) {
    if (event.matches.length > 0) {
      fuzzyMatchedError.value.show = true;
      fuzzyMatchedError.value.fuzzy = true;
      fuzzyMatchedError.value.matches = [];
      for (const rawMatch of event.matches) {
        const match: MiscFuzzyMatchResult = { result: rawMatch };
        if (!rawMatch.fuzzy) {
          fuzzyMatchedError.value.fuzzy = false;
        }
        fuzzyMatchedError.value.matches.push(match);

        const identificationTypeGroup = ["identificationType.text", "identificationProvince.text"];
        const clientIdentificationGroup = [
          "businessInformation.clientIdentification",
          "businessInformation.clientTypeOfId",
          "businessInformation.clientIdNumber",
        ];

        const warning = rawMatch.fuzzy;
        const createErrorEvent = (fieldList: string[]) =>
          fieldList.map((fieldId) => ({
            fieldId,
            errorMsg: warning ? "There's already a client with this name" : "Client already exists",
          }));
        const emitFieldErrors = (fieldList: string[]) => {
          const errorEvent = createErrorEvent(fieldList);
          errorBus.emit(errorEvent, {
            skipNotification: true,
            warning,
          });
        };
        const label = (matchedFieldsText) => {
          const prefix = warning ? "Partial matching on" : "Matching on";
          return `${prefix} ${matchedFieldsText}`;
        };
        if (rawMatch.field === "businessInformation.businessName") {
          if (rawMatch.fuzzy) {
            match.label = label("name and date of birth");
            emitFieldErrors([
              "businessInformation.firstName",
              "businessInformation.lastName",
              "businessInformation.birthdate",
            ]);
          } else {
            match.label = label("name, date of birth and ID number");
            emitFieldErrors([
              "businessInformation.firstName",
              "businessInformation.lastName",
              "businessInformation.birthdate",
              ...clientIdentificationGroup,
            ]);
          }
        }
        if (rawMatch.field === "businessInformation.identification") {
          match.label = label("ID type and ID number");
          emitFieldErrors([...identificationTypeGroup, ...clientIdentificationGroup]);
        }

        const streetAddressGroups = rawMatch.field.match(
          /location\.addresses\[(\d+)\]\.streetAddress/,
        );
        if (streetAddressGroups) {
          match.label = label("one or more locations");
          const id = streetAddressGroups[1];
          emitFieldErrors([
            `location.addresses[${id}].streetAddress`,
            `location.addresses[${id}].city`,
            `location.addresses[${id}].province`,
            `location.addresses[${id}].country`,
            `location.addresses[${id}].postalCode`,
          ]);
        }

        const emailAddressGroups = rawMatch.field.match(/location\.addresses\[(\d+)\]\.emailAddress/);
        if (emailAddressGroups) {
          match.label = label("one or more locations");
          const id = emailAddressGroups[1];
          emitFieldErrors([`location.addresses[${id}].emailAddress`]);
        }
      }

      const fuzzySuffix =
        "Review their information in the Client Management System to determine if you should create a new client:";

      if (props.id === "global") {
        fuzzyMatchedError.value.description = fuzzyMatchedError.value.fuzzy
          ? `${uniqueClientNumbers.value.length} similar client
            ${uniqueClientNumbers.value.length === 1 ? "record was" : "records were"}
            found. ${fuzzySuffix}`
          : `Looks like ”${props.businessName}” has a client number. Review their information in the
            Management System if necessary:`;
      }
      if (props.id.startsWith("location.addresses")) {
        fuzzyMatchedError.value.description = `${uniqueClientNumbers.value.length} client
            ${uniqueClientNumbers.value.length === 1 ? "record was" : "records were"}
            found with locations similar to this one. ${fuzzySuffix}`;
      }
    } else {
      fuzzyMatchedError.value.show = false;
    }
  }
};

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

// watch for fuzzy match error messages
fuzzyBus.on(handleFuzzyErrorMessage);
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