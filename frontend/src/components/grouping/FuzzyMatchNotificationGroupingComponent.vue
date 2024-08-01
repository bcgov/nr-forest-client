<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
// Carbon
import "@carbon/web-components/es/components/notification/index";
// Composables
import { useEventBus } from "@vueuse/core";
// Types
import type { FuzzyMatchResult, FuzzyMatcherData, FuzzyMatcherEvent } from "@/dto/CommonTypesDto";
import { greenDomain } from "@/CoreConstants";
import { convertFieldNameToSentence } from "@/services/ForestClientService";

const props = defineProps<{
  id: string;
  error?: FuzzyMatcherData;
  businessName: string;
}>();

const fuzzyBus = useEventBus<FuzzyMatcherEvent>("fuzzy-error-notification");

const fuzzyMatchedError = ref<FuzzyMatcherData>(
  props.error ?? {
    show: false,
    fuzzy: false,
    matches: [],
  },
);

const handleFuzzyErrorMessage = (event: FuzzyMatcherEvent | undefined, _payload?: any) => {
  if (event && event.matches.length > 0 && event.id === props.id) {
    fuzzyMatchedError.value.show = true;
    fuzzyMatchedError.value.fuzzy = true;
    fuzzyMatchedError.value.matches = [];
    for (const match of event.matches) {
      if (!match.fuzzy) {
        fuzzyMatchedError.value.fuzzy = false;
      }
      fuzzyMatchedError.value.matches.push(match);
    }
  } else {
    fuzzyMatchedError.value.show = false;
    fuzzyMatchedError.value.fuzzy = false;
    fuzzyMatchedError.value.matches = [];
  }
};

const getListItemContent = ref((match: FuzzyMatchResult) => {
  return match && match.match ? renderListItem(match) : "";
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

const renderListItem = (match: FuzzyMatchResult) => {
  let finalLabel = "";
  if (match.field === "contact" || match.field === "location") {
    finalLabel = "Matching one or more " + match.field + "s";
  } else {
    finalLabel =
      (match.fuzzy ? "Partial m" : "M") +
      "atching on " +
      convertFieldNameToSentence(match.field).toLowerCase();
  }

  finalLabel += " - Client number: ";

  const clients = [...new Set<string>(match.match.split(","))];
  finalLabel += clients
    .map(
      (clientNumber) =>
        '<a target="_blank" href="' +
        getLegacyUrl(clientNumber, match.field) +
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
const getUniqueClientNumbers = (matches: FuzzyMatchResult[]) => {
  const results: string[] = [];

  matches.forEach((data) => {
    results.push(...data.match.split(","));
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
          :key="match.field"
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
