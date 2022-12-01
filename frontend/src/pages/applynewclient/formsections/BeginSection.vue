<template>
  <CollapseCard
    :title="beginSectionSchema.container.title"
    defaultOpen
    :id="beginSectionSchema.container.id"
  >
    <div v-for="(row, rowIndex) in beginSectionSchema.content" :key="rowIndex">
      <FormComponentOptions
        :data="data[row.fieldProps.id]"
        :schema="row"
        @updateFormValue="(id, newValue) => updateFormValue(id, newValue)"
      />
    </div>
  </CollapseCard>
</template>

<script setup lang="ts">
import type { PropType } from "vue";
import CollapseCard from "../../../common/CollapseCard.vue";
import FormComponentOptions from "../../../common/FormComponentOptions.vue";
import { beginSectionSchema } from "../NewClient";
import type { FormSectionSchemaType } from "../../../core/AppType";

const props = defineProps({
  data: {
    type: Object as PropType<FormSectionSchemaType>,
    required: true,
  },
});

const emit = defineEmits(["updateFormValue"]);

const updateFormValue = (id, newValue) => {
  emit("updateFormValue", id, newValue);
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "BeginSection",
});
</script>

<style scoped></style>
