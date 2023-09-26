<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
// Carbon
import "@carbon/web-components/es/components/notification/index";
// Composables
import { useEventBus } from "@vueuse/core";
// Types
import type {
  ValidationMessageType,
  ProgressNotification,
} from "@/dto/CommonTypesDto";
import type { Address, FormDataDto } from "@/dto/ApplyClientNumberDto";

const props = defineProps<{
  formData: FormDataDto;
  scrollToNewContact: () => void;
}>();

const nonAssociatedAddressList = reactive<string[]>([]);

const locations = computed(() =>
  props.formData.location.addresses.map(
    (address: Address) => address.locationName
  )
);

watch(locations, (locations) => {
  // Iterates backwards to prevent issues after removing an item from the list
  for (let index = nonAssociatedAddressList.length - 1; index >= 0; index--) {
    const addressName = nonAssociatedAddressList[index];
    if (!locations.includes(addressName)) {
      // The address does not exist anymore
      // Remove it from the error list
      nonAssociatedAddressList.splice(index, 1);
    }
  }
});

// Define the bus to receive the global error messages and one to send the progress indicator messages
const notificationBus = useEventBus<ValidationMessageType | undefined>(
  "error-notification"
);
const progressIndicatorBus = useEventBus<ProgressNotification>(
  "progress-indicator-bus"
);
const errorBus = useEventBus<ValidationMessageType[]>(
  "submission-error-notification"
);

// Define the global error message
const globalErrorMessage = ref<ValidationMessageType | undefined>(undefined);

// Handle the global error message, including filtering unsatisfactory messages
const handleErrorMessage = (
  event: ValidationMessageType | undefined,
  payload?: any
) => {
  // Handling incorrect client type selection
  if (event && event.fieldId === "businessInformation.clientType") {
    // Show as inline notification
    globalErrorMessage.value = {
      fieldId: "server.validation.error",
      errorMsg:
        "Individuals cannot be selected. Please select a different client.",
    };
    // Emit back to the form so it can be displayed as a field error
    errorBus.emit([
      {
        fieldId: "businessInformation.businessName",
        errorMsg:
          "Individuals cannot be selected. Please select a different client.",
      },
    ]);
    // Make step 1 with error
    progressIndicatorBus.emit({ kind: "error", value: [0] });

    // Handling address without association
  } else if (
    event &&
    event.fieldId.startsWith("location.addresses[") &&
    event.fieldId.endsWith("].locationName")
  ) {
    if (payload) {
      if (event.errorMsg) {
        // The address is missing association
        const foundIndex = nonAssociatedAddressList.indexOf(payload);
        if (foundIndex === -1) {
          // Add it to the error list
          nonAssociatedAddressList.push(payload);
        }
      } else {
        // The address is properly associated
        const foundIndex = nonAssociatedAddressList.indexOf(payload);
        if (foundIndex >= 0) {
          // Remove it from the error list
          nonAssociatedAddressList.splice(foundIndex, 1);
        }
      }
    }
  } else {
    globalErrorMessage.value = event;
  }
};

// Watch for changes in the global error message bus
notificationBus.on(handleErrorMessage);

// Send a message to the progress indicator bus to navigate to a step
const goToStep = (step: number) => {
  progressIndicatorBus.emit({ kind: "navigate", value: step });
};
</script>

<template>
  <div class="top-notification" v-if="globalErrorMessage?.fieldId || nonAssociatedAddressList.length > 0">

    <cds-inline-notification
        v-if="globalErrorMessage?.fieldId === 'missing.info'"
        v-shadow="true"
        low-contrast="true"
        hide-close-button="true"
        open="true"
        kind="error"
        title="Missing information:"
        subtitle="You can't go to the next step before filling all fields correctly."
      >
    </cds-inline-notification>

    <cds-inline-notification
      v-if="globalErrorMessage?.fieldId === 'missing.contact.assigned'"
      v-shadow="true"
      low-contrast="true"
      hide-close-button="true"
      open="true"
      kind="error"
      title="Assigned contact required:">
    <span>You must associate "{{ globalErrorMessage.errorMsg }}" address with an existing contact or <a href="#" @click="goToStep(2)">add a new contact</a> before submitting the application again.</span>
    </cds-inline-notification>

    <cds-actionable-notification
      v-if="globalErrorMessage?.fieldId === 'missing.section.assigned'"
      v-shadow="true"
      low-contrast="true"
      hide-close-button="true"
      open="true"
      kind="error"
      title="Your application could not be submitted:"      
    >    
    <div v-if="globalErrorMessage.errorMsg">
      Review the "{{ globalErrorMessage.errorMsg }}" before submitting the application again.
    </div>
    <div v-else>
      Review the information in the sections below before submitting the application again.
    </div>
    </cds-actionable-notification>

    <cds-actionable-notification
      v-if="globalErrorMessage?.fieldId === 'server.validation.error'"
      v-shadow="true"
      low-contrast="true"
      hide-close-button="true"
      open="true"
      kind="error"
      title="Your application could not be submitted:"      
    >    
    <div>{{ globalErrorMessage.errorMsg }}</div>    
  </cds-actionable-notification>

  <cds-inline-notification
    v-for="item in nonAssociatedAddressList"
    :key="item"
    v-shadow="true"
    low-contrast="true"
    hide-close-button="true"
    open="true"
    kind="error"
  >
    <p class="body-compact-01">
      <span class="heading-compact-01 heading-compact-01-dark">Assigned contact required:</span>
      You must associate <span class="heading-compact-01 heading-compact-01-dark">“{{ item }}”</span> address with an existing contact or <a href="#" @click.prevent="scrollToNewContact">add a new contact</a> before submitting the application again.
    </p>
  </cds-inline-notification>
  </div>
</template>
