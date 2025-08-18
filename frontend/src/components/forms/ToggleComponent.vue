<script setup lang="ts">
// Carbon
import "@carbon/web-components/es/components/toggle/index";

const props = withDefaults(
  defineProps<{
    id: string;
    label: string;
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
  <div class="grouping-02" :class="$attrs.class">
    <cds-toggle
      :id="id"
      :label-text="label"
      :label-a="labelOn"
      :label-b="labelOff"
      v-shadow="2"
      :checked="model"
      @cds-toggle-changed="model = $event.target.checked"
    />
  </div>
</template>
