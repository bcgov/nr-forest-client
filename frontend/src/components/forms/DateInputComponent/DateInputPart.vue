<script setup lang="ts">
// Carbon
import '@carbon/web-components/es/components/text-input/index';

// Define the input properties for this component
const props = defineProps<{
  parentId: string
  datePart: "year" | "month" | "day"
  selectedValue: string
  enabled?: boolean
  invalid: boolean
}>();

const emit = defineEmits<{
  blur: [event: FocusEvent]
  input: [event: Event]
}>();

const capitalizedDatePart = props.datePart[0].toUpperCase() + props.datePart.substring(1);

const id = props.parentId + capitalizedDatePart;

const placeholders = {
  year: "YYYY",
  month: "MM",
  day: "DD"
};
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
      :data-id="'input-' + parentId + '-' + datePart"
      v-shadow="4"
    />
  </div>
</template>
