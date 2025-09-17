<script setup lang="ts" generic="T">
import { ref, computed, watch, nextTick } from "vue";
// Carbon
import "@carbon/web-components/es/components/dropdown/index";
import type { CDSDropdown } from "@carbon/web-components";
// Composables
import { useEventBus } from "@vueuse/core";
// Types
import type { CodeNameType, CodeNameValue } from "@/dto/CommonTypesDto";
import { isEmpty, type ValidationMessageType } from "@/dto/CommonTypesDto";

//Define the input properties for this component
const props = defineProps<{
  id: string;
  label: string;
  placeholder?: string;
  tip: string;
  modelValue: Array<CodeNameValue<T>>;
  initialValue: string;
  validations: Array<Function>;
  errorMessage?: string;
  required?: boolean;
  requiredLabel?: boolean;
  autocomplete?: string;
}>();

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: "error", value: string | undefined): void;
  (e: "empty", value: boolean): void;
  (e: "update:modelValue", value: string | undefined): void;
  (e: "update:selectedValue", value: CodeNameType | undefined): void;
}>();

//We initialize the error message handling for validation
const error = ref<string | undefined>(props.errorMessage ?? "");

const revalidateBus = useEventBus<string[] | undefined>("revalidate-bus");

const warning = ref(false);

//We set it as a separated ref due to props not being updatable
const selectedValue = ref(props.initialValue);
// This is to make the input list contains the selected value to show when component render
const inputList = computed<Array<CodeNameValue<T>>>(() =>
  !props.modelValue || props.modelValue.length === 0
    ? [{ name: props.initialValue, code: "", status: "", legalType: "", value: undefined }]
    : props.modelValue,
);

//We set the value prop as a reference for update reason
emit("empty", isEmpty(props.initialValue));
//This function emits the events on update
const emitValueChange = (newValue: string): void => {
  const reference = newValue
    ? props.modelValue.find((entry) => entry.name === newValue)
    : { code: "", name: "" };

  emit("update:modelValue", reference?.name);
  emit("update:selectedValue", reference);
  emit("empty", isEmpty(newValue));
};

/**
 * Sets the error and emits an error event.
 * @param errorObject - the error object or string
 */
const setError = (errorObject: string | ValidationMessageType | undefined) => {
  const errorMessage =
    typeof errorObject === "object" ? errorObject.errorMsg : errorObject;
  error.value = errorMessage || "";

  warning.value = false;
  if (typeof errorObject === "object") {
    warning.value = errorObject.warning;
  }

  /*
  The error should be emitted whenever it is found, instead of watching and emitting only when it
  changes.
  Because the empty event is always emitted, even when it remains the same payload, and then we
  rely on empty(false) to consider a value "valid". In turn we need to emit a new error event after
  an empty one to allow subscribers to know in case the field still has the same error.
  */
  emit("error", error.value);
};

/**
 * Performs all validations and returns the first error message.
 * If there's no error from the validations, returns props.errorMessage.
 *
 * @param {string} newValue
 * @returns {string | undefined} the error message or props.errorMessage
 */
const validatePurely = (newValue: string): string | undefined => {
  if (props.validations) {
    return props.validations
      .map((validation) => validation(newValue))
      .filter((errorMessage) => {
        if (errorMessage) return true;
        return false;
      })
      .reduce((acc, errorMessage) => acc || errorMessage, props.errorMessage);
  }
};

const validateInput = (newValue: any) => {
  if (props.validations) {
    setError(validatePurely(newValue));
  }
};

// Tells whether the current change was done manually by the user.
const isUserEvent = ref(false);

const selectItem = (event: any) => {
  selectedValue.value = event?.detail?.item?.getAttribute("data-value");
  isUserEvent.value = true;
};

/**
 * This array is used in a way that allows to mount a brand new combo-box
 * whenever it contains a different time.
 * This is done as a workaround that allows to clear the text displayed in
 * the combo-box.
 * @see FSADT1-900
 */
const comboBoxMountTime = ref<[number]>([Date.now()]);

//Watch for changes on the input
watch([selectedValue], () => {
  if (!selectedValue.value) {
    comboBoxMountTime.value = [Date.now()];
  }
  const reference = selectedValue.value
    ? props.modelValue.find((entry) => entry.name === selectedValue.value)
    : { code: "", name: "" };
  const errorMessage = validatePurely(reference ? reference.code : "");

  /*
  If the current change was not directly performed by the user, we don't want
  to set a non-empty error message on it.
  We don't want to call user's attention for something they didn't do wrong.
  Note: we still want to UPDATE the error message in case it already had an
  error, since the type of error could have changed.
  */
  if (isUserEvent.value || !errorMessage || error.value) {
    setError(errorMessage);
  }

  // resets variable
  isUserEvent.value = false;

  emitValueChange(selectedValue.value);
});

watch(inputList, () => (selectedValue.value = props.initialValue));

//We watch for error changes to emit events
watch(
  () => props.errorMessage,
  () => setError(props.errorMessage)
);
watch(
  () => props.initialValue,
  () => (selectedValue.value = props.initialValue)
);

revalidateBus.on((keys: string[] | undefined) => {
  if (keys === undefined || keys.includes(props.id)) {
    validateInput(selectedValue.value);
  }
});

const ariaInvalidString = computed(() => (error.value ? "true" : "false"));

const isFocused = ref(false);

// This is an array due to the v-for attribute.
const cdsDropdownArrayRef = ref<InstanceType<typeof CDSDropdown>[] | null>(
  null
);

watch(
  [
    cdsDropdownArrayRef,
    () => props.required,
    () => props.label,
    isFocused,
    ariaInvalidString,
  ],
  async ([cdsDropdownArray]) => {
    if (cdsDropdownArray) {
      // wait for the DOM updates to complete
      await nextTick();

      const dropdown = cdsDropdownArray[0];
      const input = dropdown?.shadowRoot?.querySelector("input");

      const helperTextId = "helper-text";
      const helperText = dropdown.shadowRoot?.querySelector(
        "[name='helper-text']"
      );
      if (helperText) {
        helperText.id = helperTextId;

        // For some reason the role needs to be dynamically changed to "alert" to announce.
        if (isFocused.value) {
          helperText.role = "generic";
        } else {
          helperText.role =
            ariaInvalidString.value === "true" ? "alert" : "generic";
        }
      }

      if (input) {
        // Propagate attributes to the input
        input.required = props.required;
        input.ariaLabel = props.label;
        input.ariaInvalid = ariaInvalidString.value;

        // Use the helper text as a field description
        input.setAttribute("aria-describedby", helperTextId);
      }
    }
  }
);

// For some reason, if helper-text is empty, invalid-text message doesn't work.
const safeHelperText = computed(() => props.tip || " ");

const color = "green";
</script>

<template>
  <div class="grouping-03">
    <div class="input-group">
      <cds-dropdown
        v-for="time in comboBoxMountTime"
        ref="cdsDropdownArrayRef"
        :key="time"
        :id="id"
        :autocomplete="autocomplete"
        :title-text="label"
        :aria-label="label"
        :class="warning ? 'warning' : ''"
        :clear-selection-label="`Clear ${label}`"
        :required="required"
        :data-required-label="requiredLabel"
        filterable
        :helper-text="safeHelperText"
        :label="placeholder"
        :value="selectedValue"
        :invalid="!warning && error ? true : false"
        :aria-invalid="ariaInvalidString"
        :invalid-text="!warning && error"
        :warn="warning"
        :warn-text="warning && error"
        @cds-dropdown-selected="selectItem"
        @focus="isFocused = true"
        @blur="
          (event: any) => {
            isFocused = false;
            validateInput(event.target.value);
          }
        "
        :data-focus="id"
        :data-scroll="id"
        v-shadow="4"
      >
        <cds-dropdown-item
          v-for="option in inputList"
          :key="option.code"
          :value="option.name"
          :data-id="option.code"
          :data-value="option.name"
        >
          <slot :option="option">
            {{ option.name }}
          </slot>
        </cds-dropdown-item>
      </cds-dropdown>
    </div>
  </div>
</template>
