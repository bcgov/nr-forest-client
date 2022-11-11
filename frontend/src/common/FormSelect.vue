<template>
  <FormFieldTemplate :fieldProps="fieldProps">
    <b-form-select
      v-model="computedValue"
      :options="options"
      :disabled="disabled"
    ></b-form-select>
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
  // form field template props (optional): label, required, tooltip, note, id
  fieldProps: {
    type: Object as PropType<FormFieldTemplateType>,
    default: {
      label: "Hello",
    },
  },
  value: [Object, String],
  disabled: { type: Boolean, default: false },
  options: {
    type: Array as PropType<Array<FromSelectOptionType>>,
    required: true,
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
