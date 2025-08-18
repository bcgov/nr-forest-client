<script setup lang="ts">
// Carbon
import "@carbon/web-components/es/components/toggle/index";

const props = withDefaults(
  defineProps<{
    id: string;
    label: string;
    tip?: string;
    labelOn?: string;
    labelOff?: string;
  }>(),
  {
    labelOn: "On",
    labelOff: "Off",
  },
);

const model = defineModel<boolean>({ required: true });
</script>

<template>
  <cds-toggle
    :id="id"
    :label-text="label"
    :helper-text="tip"
    :label-a="labelOn"
    :label-b="labelOff"
    v-shadow="2"
    :checked="model"
    @cds-toggle-changed="model = $event.target.checked"
  />
</template>
