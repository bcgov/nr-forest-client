<script setup lang="ts">
// Carbon
import '@carbon/web-components/es/components/text-input/index';
// Types
import { DatePart } from "./common";

// Define the input properties for this component
const props = defineProps<{
  parentId: string;
  datePart: DatePart;
  selectedValue: string;
  enabled?: boolean;
  invalid: boolean;
}>();

const emit = defineEmits<{
  blur: [event: FocusEvent]
  input: [event: Event]
}>();

const datePartName = DatePart[props.datePart];

const capitalizedDatePart = datePartName[0].toUpperCase() + datePartName.substring(1);

const id = props.parentId + capitalizedDatePart;

const placeholders = {
  [DatePart.year]: "YYYY",
  [DatePart.month]: "MM",
  [DatePart.day]: "DD"
};

const placeholder = placeholders[props.datePart];

const mask = "#".repeat(placeholder.length);
</script>

<template>
  <div class="input-group">
    <div class="cds--text-input__label-wrapper">
      <label :id="parentId + capitalizedDatePart + 'Label'" :for="id" class="cds-text-input-label">
        {{ enabled ? capitalizedDatePart : null }}
      </label>
    </div>
    <cds-text-input
      v-if="enabled"
      :id="id"
      type="tel"
      :placeholder="placeholders[datePart]"
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
