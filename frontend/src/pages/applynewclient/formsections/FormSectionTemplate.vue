<template>
  <CollapseCard
    :title="sectionProps.container.title"
    defaultOpen
    :id="sectionProps.container.id"
  >
    <div v-for="(row, rowIndex) in sectionProps.content" :key="rowIndex">
      <FormComponentOptions
        v-if="!row.depend || data[row.depend.fieldId] == row.depend.value"
        :data="data[row.fieldProps.id]"
        :error="computedErrorMsg(row.fieldProps.id)"
        :schema="row"
        @updateFormValue="
          (fieldId, newValue) =>
            formData.mutations.updateFormValue(
              sectionProps.container.id,
              fieldId,
              newValue
            )
        "
        @updateFormArrayValue="
          (subFieldId, newValue, rowIndex) =>
            formData.mutations.updateFormArrayValue(
              sectionProps.container.id, // container id
              row.fieldProps.id, // field id
              subFieldId,
              newValue,
              rowIndex
            )
        "
        @addRow="
          () =>
            formData.mutations.addRow(
              sectionProps.container.id,
              row.fieldProps.id
            )
        "
        @deleteRow="
          (rowIndex) =>
            formData.mutations.deleteRow(
              sectionProps.container.id,
              row.fieldProps.id,
              rowIndex
            )
        "
      />
    </div>
  </CollapseCard>
</template>

<script setup lang="ts">
import { computed } from "vue";
import _ from "lodash";
import type { PropType } from "vue";
import CollapseCard from "../../../common/CollapseCard.vue";
import FormComponentOptions from "../../../common/FormComponentOptions.vue";
import { formData } from "../../../store/newclientform/FormData";
import { validationResult } from "../../../store/newclientform/FormValidation";
import type {
  CommonObjectType,
  FormSectionSchemaType,
} from "../../../core/FormType";

const props = defineProps({
  data: {
    type: Object as PropType<CommonObjectType>,
    required: true,
  },
  sectionProps: {
    type: Object as PropType<FormSectionSchemaType>,
    required: true,
  },
});

const computedErrorMsg = computed(() => {
  return (fieldId: string) => {
    if (_.has(validationResult.state, [props.sectionProps.container.id]))
      return validationResult.state[props.sectionProps.container.id].filter(
        (each) => each.fieldId == fieldId
      );
    return [];
  };
});
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "FormSectionTemplate",
});
</script>

<style scoped></style>
