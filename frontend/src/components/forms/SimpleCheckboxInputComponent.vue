<script setup lang="ts">
// Carbon
import "@carbon/web-components/es/components/checkbox/index";

const props = defineProps<{
  id: string;
  label: string;
  checkboxLabel: string;
  tip?: string;
  requiredLabel?: boolean;
}>();

const model = defineModel<boolean>({ required: true });
</script>

<template>
  <cds-checkbox-group
    :id="id"
    :legend-text="label"
    :data-required-label="requiredLabel"
    :helper-text="tip"
    v-shadow="2"
  >
    <cds-checkbox
      v-shadow="1"
      :checked="model"
      @cds-checkbox-changed="model = $event.detail.checked"
      >{{ checkboxLabel }}</cds-checkbox
    >
  </cds-checkbox-group>
</template>
