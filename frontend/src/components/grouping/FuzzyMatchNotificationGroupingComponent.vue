<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
// Carbon
import "@carbon/web-components/es/components/notification/index";
// Composables
import { useEventBus } from "@vueuse/core";
// Types
import type {
  FuzzyMatchResult,
  FuzzyMatcherData,
  FuzzyMatcherEvent
} from "@/dto/CommonTypesDto";
import { greenDomain } from "@/CoreConstants";
import { convertFieldNameToSentence } from "@/services/ForestClientService";

const props = defineProps<{
  id: string;
  error?: FuzzyMatcherData;
}>();

const fuzzyBus = useEventBus<FuzzyMatchResult[]>(
  "fuzzy-error-notification"
);

const fuzzyMatchedError = ref<FuzzyMatcherData>(
  props.error ?? {
    show: false,
    fuzzy: false,
    matches: [],
  }
)

const handleFuzzyErrorMessage = (
  event: FuzzyMatcherEvent | undefined,
  payload?: any
) => {  
  if (event && event.matches.length > 0 && event.id === props.id) {
    fuzzyMatchedError.value.show = true;
    for (const match of event.matches) {
      if(!match.fuzzy) {
        fuzzyMatchedError.value.fuzzy = false;
      }
      fuzzyMatchedError.value.matches.push(match);
    }
  }else{
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
    case 'contact':
      return `https://${greenDomain}/int/client/client06ContactListAction.do?bean.clientNumber=${encodedClientNumber}`;
    case 'location':
      return `https://${greenDomain}/int/client/client07LocationListAction.do?bean.clientNumber=${encodedClientNumber}`;
    default:
      return `https://${greenDomain}/int/client/client02MaintenanceAction.do?bean.clientNumber=${encodedClientNumber}`;
  }
};

const renderListItem = (match: FuzzyMatchResult) => {
  let finalLabel = "";
  if (match.fieldId === 'contact' || match.fieldId === 'location') {
    finalLabel = "Matching one or more " + label + "s";
  }
  else {
    finalLabel = 
    (match.fuzzy ? "Partial m" : "M") + "atching on " + convertFieldNameToSentence(match.fieldId).toLowerCase();
  }

  finalLabel += " - Client number: ";

  const clients = [...new Set<string>(match.match.split(","))];
  finalLabel += clients
                  .map(clientNumber =>
                    '<a target="_blank" href="' + getLegacyUrl(clientNumber, match.fieldId) + '">' +
                    clientNumber +
                    "</a>")
                  .join(', ');
  
  return (
    finalLabel
  );
};

//watch for fuzzy match error messages
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
            Looks like ”Tolko” has a client number. Review their information in the Client Management System if necessary:
          </span>
          <ul>
            
            <!-- eslint-disable-next-line vue/no-v-html -->
            <li v-for="match in fuzzyMatchedError.matches" :key="match.fieldId"
              v-dompurify-html="getListItemContent(match)">
            </li>
          </ul>
          <span class="body-compact-02">You must inform the applicant of their number.</span>
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