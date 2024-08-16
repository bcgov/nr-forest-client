<script setup lang="ts">
import { ref, computed, watch, nextTick } from "vue";
// Carbon
import "@carbon/web-components/es/components/multi-select/index";
import "@carbon/web-components/es/components/tag/index";
import type { CDSMultiSelect } from "@carbon/web-components";
// Composables
import { useEventBus } from "@vueuse/core";
// Types
import type { CodeNameType, ValidationMessageType } from "@/dto/CommonTypesDto";

//Define the input properties for this component
const props = defineProps<{
  id: string;
  label: string;
  tip: string;
  modelValue: Array<CodeNameType>;
  selectedValues?: Array<string>;
  initialValue: string;
  validations: Array<Function>;
  errorMessage?: string;
  required?: boolean;
  requiredLabel?: boolean;
}>();

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: "error", value: string | undefined): void;
  (e: "empty", value: boolean): void;
  (e: "update:modelValue", value: string[] | undefined): void;
  (e: "update:selectedValue", value: Array<CodeNameType> | undefined): void;
}>();

//We initialize the error message handling for validation
const error = ref<string | undefined>(props.errorMessage ?? "");

const revalidateBus = useEventBus<string[]|undefined>("revalidate-bus");

const warning = ref(false);

//We set it as a separated ref due to props not being updatable
const selectedValue = ref(props.initialValue);
// This is to make the input list contains the selected value to show when component render
const inputList = computed<Array<CodeNameType>>(() =>
  !props.modelValue || props.modelValue.length === 0
    ? [{ name: props.initialValue, code: "", status: "", legalType: "" }]
    : props.modelValue
);

//We set the value prop as a reference for update reason
emit("empty", props.selectedValues ? props.selectedValues.length === 0 : true);

//Controls the selected values
const items = ref<string[]>([]);

/**
 * Sets the error and emits an error event.
 * @param errorObject - the error object or string
 */
const setError = (errorObject: string | ValidationMessageType | undefined) => {
  const errorMessage = typeof errorObject === "object" ? errorObject.errorMsg : errorObject;
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
  emit('error', error.value);
}

//We call all the validations
const validateInput = (newValue: any) => {
  if (props.validations && props.validations.length > 0) {
    const hasError = (message: string) => {
      if (message) return true;
      return false;
    };
    const validate = (value: string[]) =>
      props.validations
        .map((validation) => validation(value))
        .filter(hasError)
        .reduce(
          (acc, errorMessage) => acc || errorMessage,
          props.errorMessage
        );

    setError(validate(items.value));
  }
};

const emitChange = () => {
  const reference = props.modelValue.filter((entry) =>
    items.value.includes(entry.name)
  );
  emit("update:modelValue", items.value);
  emit("update:selectedValue", reference);
  emit("empty", reference.length === 0);
};

const addFromSelection = (itemCode: string) => {
  const reference = props.modelValue.find((entry) => entry.name === itemCode);
  if (reference) {
    items.value.push(reference.name);
    selectedValue.value = items.value.join(",");
  }
};

const selectItems = (event: any) => {
  // Why this undefined data check is here you might ask? Well, it's because I can't emit the event with value on target
  const contentValue =
    event?.data !== undefined ? event?.data : event.target.value;
  selectedValue.value = contentValue;
  items.value = contentValue.split(",").filter((value: string) => value);
};

props.selectedValues?.forEach((value: string) => addFromSelection(value));

watch([items], () => emitChange());

watch(
  () => props.modelValue,
  () => (selectedValue.value = props.initialValue)
);

watch([selectedValue], () => validateInput(selectedValue.value));

//We watch for error changes to emit events
watch(error, () => emit("error", error.value));
watch(
  () => props.errorMessage,
  () => setError(props.errorMessage)
);

revalidateBus.on((keys: string[] | undefined) => {
  if(keys === undefined || keys.includes(props.id)) {
    validateInput(selectedValue.value)
  }
});

const ariaInvalidString = computed(() => (error.value ? "true" : "false"));

const isFocused = ref(false);

const cdsMultiSelectRef = ref<InstanceType<typeof CDSMultiSelect> | null>(null);

watch(
  [cdsMultiSelectRef, () => props.required, isFocused, ariaInvalidString],
  async ([cdsMultiSelect]) => {
    if (cdsMultiSelect) {
      // wait for the DOM updates to complete
      await nextTick();

      const helperTextId = "helper-text";
      const helperText = cdsMultiSelect.shadowRoot?.querySelector("[name='helper-text']");
      if (helperText) {
        helperText.id = helperTextId;

        // For some reason the role needs to be dynamically changed to "alert" to announce.
        if (isFocused.value) {
          helperText.role = "generic";
        } else {
          helperText.role = ariaInvalidString.value === "true" ? "alert" : "generic";
        }
      }

      const triggerDiv = cdsMultiSelect.shadowRoot?.querySelector("div[role='button']");
      if (triggerDiv) {
        // Properly indicate as required.
        triggerDiv.ariaRequired = props.required ? "true" : "false";
        triggerDiv.ariaInvalid = ariaInvalidString.value;

        // Use the helper text as a field description
        triggerDiv.setAttribute("aria-describedby", helperTextId);
      }
    }
  },
);
</script>

<template>
  <div class="grouping-03">
    <div class="frame-02">
      <div class="input-group">
        <cds-multi-select
          ref="cdsMultiSelectRef"
          :id="id"
          :value="selectedValue"
          :label="selectedValue"
          :class="warning ? 'warning' : ''"
          :title-text="label"
          :aria-label="label"
          :required="required"
          :data-required-label="requiredLabel"
          :helper-text="tip"
          :invalid="!warning && error ? true : false"
          :aria-invalid="ariaInvalidString"
          :invalid-text="!warning && error"
          :warn="warning"
          :warn-text="warning && error"
          filterable
          @cds-multi-select-selected="selectItems"
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
          <cds-multi-select-item
            v-for="option in inputList"
            :key="option.code"
            :value="option.name"
            :data-id="option.code"
            :data-value="option.name">
            {{ option.name }}
          </cds-multi-select-item>
        </cds-multi-select>
      </div>
    </div>
  </div>
</template>
