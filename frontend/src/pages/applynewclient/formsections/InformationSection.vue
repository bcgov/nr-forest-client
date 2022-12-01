<template>
  <CollapseCard
    :title="informationSectionSchema[computedCompanyType].container.title"
    defaultOpen
    :id="informationSectionSchema[computedCompanyType].container.id"
  >
    <div
      v-for="(row, rowIndex) in informationSectionSchema[computedCompanyType]
        .content"
      :key="rowIndex"
    >
      <FormComponentOptions
        v-if="!row.depend || data[row.depend.fieldId] == row.depend.value"
        :data="data[row.fieldProps.id]"
        :schema="row"
        @updateFormValue="(id, newValue) => updateFormValue(id, newValue)"
      />
    </div>
  </CollapseCard>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { PropType } from "vue";
import CollapseCard from "../../../common/CollapseCard.vue";
import FormComponentOptions from "../../../common/FormComponentOptions.vue";
import { informationSectionSchema } from "../NewClient";
import type { FormSectionSchemaType } from "../../../core/AppType";

const props = defineProps({
  data: {
    type: Object as PropType<FormSectionSchemaType>,
    required: true,
  },
  companyType: {
    type: String, // individual or soleProprietorship or company
    required: true,
  },
});

const computedCompanyType = computed(() => {
  console.log("123", props.companyType);
  if (
    props.companyType != "individual" &&
    props.companyType != "soleProprietorship"
  ) {
    return "company";
  }

  return props.companyType;
});

const emit = defineEmits(["updateFormValue"]);

const updateFormValue = (id, newValue) => {
  emit("updateFormValue", id, newValue);
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "InformationSection",
});
</script>

<style scoped></style>
