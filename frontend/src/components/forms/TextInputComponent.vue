<script setup lang="ts">
import { ref, watch, nextTick, computed } from "vue";
// Carbon
import "@carbon/web-components/es/components/text-input/index";
import type { CDSTextInput } from "@carbon/web-components";
// Composables
import { useEventBus } from "@vueuse/core";
// Types
import { isEmpty } from "@/dto/CommonTypesDto";
import type { TextInputType } from "@/components/types";

//Define the input properties for this component
const props = withDefaults(
  defineProps<{
    id: string;
    label: string;
    tip?: string;
    enabled?: boolean;
    placeholder: string;
    modelValue: string;
    validations: Array<Function>;
    errorMessage?: string;
    mask?: string;
    required?: boolean;
    requiredLabel?: boolean;
    type?: TextInputType;
    autocomplete?: string;

    /** Display numeric virtual keyboard. Note: do not use this when type is tel. */
    numeric?: boolean;
  }>(),
  {
    type: "text",
  },
);

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: "error", value: string | undefined): void;
  (e: "empty", value: boolean): void;
  (e: "update:model-value", value: string): void;
}>();

//We initialize the error message handling for validation
const error = ref<string | undefined>(props.errorMessage ?? "");

const revalidateBus = useEventBus<void>("revalidate-bus");

/**
 * Sets the error and emits an error event.
 * @param errorMessage - the error message
 */
const setError = (errorMessage: string | undefined) => {
  error.value = errorMessage;

  /*
  The error should be emitted whenever it is found, instead of watching and emitting only when it
  changes.
  Because the empty event is always emitted, even when it remains the same payload, and then we
  rely on empty(false) to consider a value "valid". In turn we need to emit a new error event after
  an empty one to allow subscribers to know in case the field still has the same error.
  */
  emit('error', error.value);
}

watch(
  () => props.errorMessage,
  () => setError(props.errorMessage),
);

//We set it as a separated ref due to props not being updatable
const selectedValue = ref<string>(props.modelValue);

//We set the value prop as a reference for update reason
emit("empty", isEmpty(props.modelValue));

//This function emits the events on update
const emitValueChange = (newValue: string): void => {
  emit("update:model-value", newValue);
  emit("empty", isEmpty(newValue));
};
//Watch for changes on the input
watch([selectedValue], () => {
  emitValueChange(selectedValue.value);

  // We don't to validate at each key pressed, but we do want to validate when it's changed from outside
  if (!isUserEvent.value) {
    validateInput(selectedValue.value);
  }

  // resets variable
  isUserEvent.value = false;
});

//We call all the validations
const validateInput = (newValue: string) => {
  if (props.validations) {
    setError(
      props.validations
        .map((validation) => validation(newValue))
        .filter((errorMessage) => {
          if (errorMessage) return true;
          return false;
        })
        .shift() ?? props.errorMessage,
    );
  }
};

revalidateBus.on(() => {
  validateInput(selectedValue.value);
});

watch(
  () => props.modelValue,
  () => (selectedValue.value = props.modelValue)
);

// Tells whether the current change was done manually by the user.
const isUserEvent = ref(false);

const selectValue = (event: any) => {
  selectedValue.value = event.target.value;
  isUserEvent.value = true
};

const originalDescribedBy = ref<string>();

const isFocused = ref(false);

const ariaInvalidString = computed(() => (error.value ? "true" : "false"));

const cdsTextInputRef = ref<InstanceType<typeof CDSTextInput> | null>(null);

watch(
  [cdsTextInputRef, () => props.numeric, isFocused, ariaInvalidString],
  async ([cdsTextInput]) => {
    if (cdsTextInput) {
      // wait for the DOM updates to complete
      await nextTick();

      const invalidTextId = "invalid-text";
      const invalidText = cdsTextInput.shadowRoot?.querySelector("[name='invalid-text']");
      if (invalidText) {
        invalidText.id = invalidTextId;

        // For some reason the role needs to be dynamically changed to "alert" to announce.
        if (isFocused.value) {
          invalidText.role = "generic";
        } else {
          invalidText.role = ariaInvalidString.value === "true" ? "alert" : "generic";
        }
      }

      const input = cdsTextInput.shadowRoot?.querySelector("input");
      if (input) {
        // display either a numeric or an alphanumeric (default) keyboard on mobile devices
        input.inputMode = props.numeric ? "numeric" : "text";
        input.ariaInvalid = ariaInvalidString.value;

        if (!originalDescribedBy.value) {
          originalDescribedBy.value = input.getAttribute("aria-describedby");
        }

        // Use the helper text as a field description
        input.setAttribute(
          "aria-describedby",
          ariaInvalidString.value === "true" ? invalidTextId : originalDescribedBy.value,
        );
      }
    }
  },
);
</script>

<template>
  <div v-if="enabled" class="grouping-02" :class="$attrs.class">
    <div class="input-group">
      <cds-text-input
        v-if="enabled"
        ref="cdsTextInputRef"
        :id="id"
        :autocomplete="autocomplete"
        :required="required"
        :label="label"
        :aria-label="label"
        :data-required-label="requiredLabel"
        :type="type"
        :placeholder="placeholder"
        :value="selectedValue"
        :helper-text="tip"
        :disabled="!enabled"
        :invalid="error ? true : false"
        :invalid-text="error"
        aria-live="polite"
        v-masked="mask"
        @focus="isFocused = true"
        @blur="
          (event: any) => {
            isFocused = false;
            validateInput(event.target.value);
          }
        "
        @input="selectValue"
        :data-focus="id"
        :data-scroll="id"
        :data-id="'input-' + id"
        v-shadow="3"
      />
    </div>
  </div>

  <div v-if="!enabled" class="grouping-04">
    <div :data-scroll="id" class="grouping-04-label">
      <span :for="id" class="label-01">{{ label }}</span>
    </div>
    <span class="text-01">{{ modelValue }}</span>
  </div>

</template>

<style scoped>
</style>
