<script setup lang="ts">
import { ref, computed, watch, nextTick } from "vue";
// Carbon
import "@carbon/web-components/es/components/combo-box/index";
import type { CDSComboBox } from "@carbon/web-components";
// Composables
import { useEventBus } from "@vueuse/core";
// Types
import type { CodeNameType } from "@/dto/CommonTypesDto";
import { isEmpty } from "@/dto/CommonTypesDto";

//Define the input properties for this component
const props = defineProps<{
  id: string;
  label: string;
  placeholder?: string;
  tip: string;
  modelValue: Array<CodeNameType>;
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

const revalidateBus = useEventBus<void>("revalidate-bus");

//We set it as a separated ref due to props not being updatable
const selectedValue = ref(props.initialValue);
// This is to make the input list contains the selected value to show when component render
const inputList = computed<Array<CodeNameType>>(() =>
  !props.modelValue || props.modelValue.length === 0
    ? [{ name: props.initialValue, code: "", status: "", legalType: "" }]
    : props.modelValue
);

//We set the value prop as a reference for update reason
emit("empty", isEmpty(props.initialValue));
//This function emits the events on update
const emitValueChange = (newValue: string): void => {
  const reference = newValue
    ? props.modelValue.find((entry) => entry.name === newValue)
    : { code: '', name: '' };

  emit("update:modelValue", reference?.name);
  emit("update:selectedValue", reference);
  emit("empty", isEmpty(newValue));
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
    return (
      props.validations
        .map((validation) => validation(newValue))
        .filter((errorMessage) => {
          if (errorMessage) return true;
          return false;
        })
        .shift() ?? props.errorMessage
    );
  }
}

const validateInput = (newValue: any) => {
  if (props.validations) {
    error.value = validatePurely(newValue);
  }
};

// Tells whether the current change was done manually by the user.
const isUserEvent = ref(false)

const selectItem = (event: any) => {
  selectedValue.value = event?.detail?.item?.getAttribute("data-value");
  isUserEvent.value = true
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
  if (selectedValue.value === "") {
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
    error.value = errorMessage
  }

  // resets variable
  isUserEvent.value = false;

  emitValueChange(selectedValue.value);
});

watch(inputList, () => (selectedValue.value = props.initialValue));

//We watch for error changes to emit events
watch(error, () => emit("error", error.value));
watch(
  () => props.errorMessage,
  () => (error.value = props.errorMessage)
);
watch(
  () => props.initialValue,
  () => (selectedValue.value = props.initialValue)
);

revalidateBus.on(() => validateInput(selectedValue.value));

const ariaInvalidString = computed(() => (error.value ? "true" : "false"));

// This is an array due to the v-for attribute.
const cdsComboBoxArrayRef = ref<InstanceType<typeof CDSComboBox>[] | null>(null);

watch(
  [cdsComboBoxArrayRef, () => props.required, () => props.label, ariaInvalidString],
  async ([cdsComboBoxArray]) => {
    if (cdsComboBoxArray) {
      // wait for the DOM updates to complete
      await nextTick();

      const combo = cdsComboBoxArray[0];
      const input = combo?.shadowRoot?.querySelector("input");

      const helperTextId = "helper-text";
      const helperText = combo.shadowRoot?.querySelector("[name='helper-text']");
      if (helperText) {
        helperText.id = helperTextId;
        if (ariaInvalidString.value === "true") {
          helperText.role = "alert";
        } else {
          helperText.role = "none";
        }
      }

      if (input) {
        // Propagate attributes to the input
        input.required = props.required;
        input.ariaLabel = props.label;
        input.ariaInvalid = ariaInvalidString.value;
        input.setAttribute("aria-describedby", helperTextId);
      }
    }
  },
);

// For some reason, if helper-text is empty, invalid-text message doesn't work.
const safeHelperText = computed(() => props.tip || " ");
</script>

<template>
  <div class="grouping-03">
    <div class="input-group">
      <cds-combo-box
        v-for="time in comboBoxMountTime"
        ref="cdsComboBoxArrayRef"
        :key="time"
        :id="id"
        :autocomplete="autocomplete"
        :title-text="label"
        :aria-label="label"
        :clear-selection-label="`Clear ${label}`"
        :required="required"
        :data-required-label="requiredLabel"
        filterable
        :helper-text="safeHelperText"
        :label="placeholder"
        :value="selectedValue"
        :invalid="error ? true : false"
        :aria-invalid="ariaInvalidString"
        :invalidText="error"
        @cds-combo-box-selected="selectItem"
        @blur="(event: any) => validateInput(event.target.value)"
        :data-focus="id"
        :data-scroll="id"
        v-shadow="3"
      >
        <cds-combo-box-item 
          v-for="option in inputList"
          :key="option.code"
          :value="option.name"
          :data-id="option.code"
          :data-value="option.name">
          {{ option.name }}
        </cds-combo-box-item>
      </cds-combo-box>
    </div>
  </div>
</template>
