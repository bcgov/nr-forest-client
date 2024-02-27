<script setup lang="ts">
import { ref, computed, watch, nextTick } from "vue";
// Carbon
import "@carbon/web-components/es/components/multi-select/index";
import "@carbon/web-components/es/components/tag/index";
import type { CDSMultiSelect } from "@carbon/web-components";
// Composables
import { useEventBus } from "@vueuse/core";
// Types
import type { CodeNameType } from "@/dto/CommonTypesDto";

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
emit("empty", props.selectedValues ? props.selectedValues.length === 0 : true);

//Controls the selected values
const items = ref<string[]>([]);

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
        .shift() ?? props.errorMessage;

    error.value = validate(items.value);
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
  () => (error.value = props.errorMessage)
);

revalidateBus.on(() => validateInput(selectedValue.value));

const cdsMultiSelect = ref<InstanceType<typeof CDSMultiSelect> | null>(null);

watch(cdsMultiSelect, async (value) => {
  if (value) {
    // wait for the DOM updates to complete
    await nextTick();

    const triggerDiv = value.shadowRoot?.querySelector("div[role='button']");
    if (triggerDiv) {
      // Properly indicate as required.
      triggerDiv.ariaRequired = props.required ? "true" : "false";
    }
  }
});
</script>

<template>
  <div class="grouping-03">
    <div class="frame-02">
      <div class="input-group">
        <cds-multi-select
          ref="cdsMultiSelect"
          :id="id"
          :value="selectedValue"
          :label="selectedValue"
          :title-text="label"
          :aria-label="label"
          :required="required"
          :data-required-label="requiredLabel"
          :helper-text="tip"
          :invalid="error ? true : false"
          :invalid-text="error"
          filterable
          @cds-multi-select-selected="selectItems"
          @blur="(event: any) => validateInput(event.target.value)"
          :data-focus="id"
          :data-scroll="id"
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
