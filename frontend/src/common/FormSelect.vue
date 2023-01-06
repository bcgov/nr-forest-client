<template>
  <FormFieldTemplate :fieldProps="fieldProps">
    <b-form-select
      v-model="computedValue"
      :options="options"
      :disabled="disabled"
    >
      <!-- :state="state" -->
      <!-- https://github.com/cdmoro/bootstrap-vue-3/issues/819 -->
    </b-form-select>
  </FormFieldTemplate>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { PropType } from "vue";
import FormFieldTemplate from "./FormFieldTemplate.vue";
import type {
  CommonObjectType,
  FormFieldTemplateType,
  FromSelectOptionType,
} from "../core/AppType";

const props = defineProps({
  // form field template props (optional): label, required, tooltip, note, id, errorMsg
  fieldProps: {
    type: Object as PropType<FormFieldTemplateType>,
    default: { id: "form-select" },
  },
  value: { type: [Object, String], required: true },
  disabled: { type: Boolean, default: false },
  state: { type: Boolean, default: null },
  options: {
    type: Array as PropType<Array<FromSelectOptionType>>,
    default: [],
  },
});

const emit = defineEmits(["updateValue"]);

const computedValue = computed({
  get() {
    return props.value;
  },
  set(newValue: CommonObjectType | string) {
    emit("updateValue", props.fieldProps.id, newValue);
  },
});
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "FormSelect",
});
</script>

<style scoped></style>
