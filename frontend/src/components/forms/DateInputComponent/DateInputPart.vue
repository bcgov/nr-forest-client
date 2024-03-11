<script setup lang="ts">
import { ref, watch, nextTick } from "vue";
// Carbon
import '@carbon/web-components/es/components/text-input/index';
import type { CDSTextInput } from "@carbon/web-components";
// Types
import { DatePart } from "./common";

// Define the input properties for this component
const props = defineProps<{
  parentId: string;
  parentTitle: string;
  datePart: DatePart;
  selectedValue: string;
  enabled?: boolean;
  invalid: boolean;
  required?: boolean;
}>();

const emit = defineEmits<{
  blur: [event: FocusEvent]
  input: [event: Event]
}>();

const datePartName = DatePart[props.datePart];

const capitalizedDatePart = datePartName[0].toUpperCase() + datePartName.substring(1);

const id = props.parentId + capitalizedDatePart;

defineExpose({
  id,
});

const placeholders = {
  [DatePart.year]: "YYYY",
  [DatePart.month]: "MM",
  [DatePart.day]: "DD",
};

const placeholder = placeholders[props.datePart];

const mask = "#".repeat(placeholder.length);

const ariaLabel = `${props.parentTitle} ${datePartName}`;

const cdsTextInputRef = ref<InstanceType<typeof CDSTextInput> | null>(null);

watch(cdsTextInputRef, async (cdsTextInput) => {
  if (cdsTextInput) {
    // wait for the DOM updates to complete
    await nextTick();

    const input = cdsTextInput.shadowRoot?.querySelector("input");
    if (input) {
      // display numeric keyboard on mobile devices
      input.inputMode = "numeric";
    }
  }
});
</script>

<template>
  <div class="input-group">
    <cds-text-input
      v-if="enabled"
      ref="cdsTextInputRef"
      :id="id"
      :required="required"
      :label="capitalizedDatePart"
      :aria-label="ariaLabel"
      :placeholder="placeholder"
      :value="selectedValue"
      :disabled="!enabled"
      :invalid="invalid"
      @blur="(e) => emit('blur', e)"
      @input="(e) => emit('input', e)"
      :data-focus="id"
      :data-scroll="id"
      :data-id="'input-' + parentId + '-' + datePartName"
      v-shadow="4"
      v-masked="mask"
    />
  </div>
</template>
