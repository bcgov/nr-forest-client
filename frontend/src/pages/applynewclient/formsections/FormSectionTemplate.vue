<template>
  <CollapseCard
    :title="sectionProps.container.title"
    defaultOpen
    :id="sectionProps.container.id"
  >
    <div v-for="(row, rowIndex) in sectionProps.content" :key="rowIndex">
      <FormComponentOptions
        v-if="!row.depend || data[row.depend.fieldModelName] == row.depend.value"
        :data="data[row.fieldProps.modelName]"
        :error="computedErrorMsg(row.fieldProps.modelName)"
        :disabledFields="computedDisabledFields(row.fieldProps.modelName)"
        :disableAll="disableAllFields.state.value"
        :schema="row"
        @updateFormValue="
          (newValue, path = '') =>
            formData.actions.updateFormValue(
              newValue,
              path != ''
                ? `${sectionProps.container.id}.${path}`
                : `${sectionProps.container.id}`
            )
        "
        @updateFormArrayValue="
          (newValue, path = '') =>
            formData.actions.updateFormValue(
              newValue,
              path != ''
                ? `${sectionProps.container.id}.${row.fieldProps.modelName}.${path}`
                : `${sectionProps.container.id}.${row.fieldProps.modelName}`
            )
        "
        @addRow="
          (path = '') =>
            formData.actions.addRow(
              path != ''
                ? `${sectionProps.container.id}.${row.fieldProps.modelName}.${path}`
                : `${sectionProps.container.id}.${row.fieldProps.modelName}`
            )
        "
        @deleteRow="
          (rowIndex, path = '') =>
            formData.actions.deleteRow(
              rowIndex,
              path != ''
                ? `${sectionProps.container.id}.${row.fieldProps.modelName}.${path}`
                : `${sectionProps.container.id}.${row.fieldProps.modelName}`
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
import {
  disabledFields,
  disableAllFields,
} from "../../../store/newclientform/FormDisable";
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
  return (fieldModelName: string) => {
    if (_.has(validationResult.state, [props.sectionProps.container.id]))
      return validationResult.state[props.sectionProps.container.id].filter(
        (each) => _.includes(each.path, fieldModelName)
      );
    return [];
  };
});

const computedDisabledFields = computed(() => {
  return (fieldModelName: string) => {
    if (_.has(disabledFields.state, [props.sectionProps.container.id]))
      return disabledFields.state[props.sectionProps.container.id].filter(
        (each) => _.includes(each, fieldModelName)
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
