<script setup lang="ts">
import { ref, watch, nextTick, computed } from "vue";
// Carbon
import "@carbon/web-components/es/components/radio-button/index";
import type { CDSRadioButtonGroup, CDSRadioButton } from "@carbon/web-components";
// Composables
import { useEventBus } from "@vueuse/core";
// Types
import type { CodeDescrType } from "@/dto/CommonTypesDto";
import { isEmpty } from "@/dto/CommonTypesDto";

const props = defineProps<{
  id: string;
  label: string;
  tip?: string;
  modelValue: Array<CodeDescrType>;
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
  (e: "update:model-value", value: string | undefined): void;
}>();

const selectedValue = ref<string>(props.initialValue);
//We initialize the error message handling for validation
const error = ref<string | undefined>(props.errorMessage ?? "");

const revalidateBus = useEventBus<void>("revalidate-bus");

//We watch for error changes to emit events
watch(error, () => emit("error", error.value));
watch(
  () => props.errorMessage,
  () => (error.value = props.errorMessage)
);

//We call all the validations
const validateInput = () => {
  if (props.validations) {
    error.value =
      props.validations
        .map((validation) => validation(selectedValue.value))
        .filter((errorMessage) => {
          if (errorMessage) return true;
          return false;
        })
        .shift() ?? props.errorMessage;
  }
  emit("empty", isEmpty(selectedValue));
};

//We watch for input changes to emit events
watch(selectedValue, () => {
  validateInput();
  emit(
    "update:model-value",
    props.modelValue
      .map((entry) => entry.value)
      .find((entry) => entry === selectedValue.value)
  );
  emit("empty", isEmpty(selectedValue));
});

const updateSelectedValue = (event: any) =>
  (selectedValue.value = event.detail.value);
revalidateBus.on(() => validateInput());

const ariaInvalidString = computed(() => (error.value ? "true" : "false"));

const isFocused = ref(false);

const cdsRadioButtonGroupRef = ref<InstanceType<typeof CDSRadioButtonGroup> | null>(null);

watch([cdsRadioButtonGroupRef, isFocused, ariaInvalidString], async ([cdsRadioButtonGroup]) => {
  if (cdsRadioButtonGroup) {
    // wait for the DOM updates to complete
    await nextTick();

    const helperTextId = "helper-text";
    const helperText = cdsRadioButtonGroup.shadowRoot?.querySelector(".cds--form__helper-text");
    if (helperText) {
      helperText.id = helperTextId;
    }

    const invalidTextId = "invalid-text";
    const invalidText = cdsRadioButtonGroup.shadowRoot?.querySelector(".cds--form-requirement");
    if (invalidText) {
      invalidText.id = invalidTextId;

      // For some reason the role needs to be dynamically changed to "alert" to announce.
      if (isFocused.value) {
        invalidText.role = "generic";
      } else {
        invalidText.role = ariaInvalidString.value === "true" ? "alert" : "generic";
      }
    }

    const fieldset = cdsRadioButtonGroup.shadowRoot?.querySelector("fieldset");
    if (fieldset) {
      fieldset.role = "radiogroup";
      fieldset.setAttribute("aria-label", props.label);
      fieldset.ariaInvalid = ariaInvalidString.value;

      // Use the helper text as a field description
      fieldset.setAttribute(
        "aria-describedby",
        ariaInvalidString.value === "true" ? invalidTextId : helperTextId,
      );
    }
  }
});

const cdsRadioButtonArrayRef = ref<InstanceType<typeof CDSRadioButton>[] | null>(null);

watch([cdsRadioButtonArrayRef, () => props.required], async (cdsRadioButtonArray) => {
  if (cdsRadioButtonArray) {
    // wait for the DOM updates to complete
    await nextTick();

    for (const radio of cdsRadioButtonArrayRef.value) {
      const label = radio.shadowRoot?.querySelector("label");
      if (label) {
        // Fixes the association as it's wrong in the component.
        label.htmlFor = "radio";
      }
      const input = radio.shadowRoot?.querySelector("input");
      if (input) {
        // Propagate attributes to the input
        input.required = props.required;
      }
    }
  }
});

// For this component, the Screen Reader s=is more responsive to alerts with aria-live set.
const ariaLive = "polite";
</script>

<template>
  <div class="grouping-01">
    <div class="input-group">
      <cds-radio-button-group
        ref="cdsRadioButtonGroupRef"
        :id="id + 'rb'"
        :name="id + 'rb'"
        :legend-text="label"
        :data-required-label="requiredLabel"
        label-position="right"
        orientation="vertical"
        :helper-text="tip"
        v-model="selectedValue"
        :invalid="error ? true : false"
        :aria-invalid="ariaInvalidString"
        :invalid-text="error"
        :aria-live="ariaLive"
        @cds-radio-button-group-changed="updateSelectedValue"
        :data-focus="id"
        :data-scroll="id"
        v-shadow="2"
      >
        <cds-radio-button
          ref="cdsRadioButtonArrayRef"
          v-shadow="1"
          :id="id + 'rb' + option.value"
          v-for="option in modelValue"
          :key="id + 'rb' + option.value"
          :label-text="option.text"
          :value="option.value"
          role="radio"
          :aria-checked="selectedValue === option.value"
          @focus="isFocused = true"
          @blur="isFocused = false"
        />
      </cds-radio-button-group>
    </div>
  </div>
</template>


