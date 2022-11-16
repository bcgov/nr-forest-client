<template>
  <FormFieldTemplate :fieldProps="fieldProps">
    <label
      v-for="(option, index) in options"
      :key="index"
      style="display: block; text-align: left; margin-bottom: 4px"
    >
      <input
        type="checkbox"
        :value="option.code"
        v-model="computedValue"
        :name="name"
        :disabled="disabled"
      />
      {{ option.text }}
    </label>
  </FormFieldTemplate>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { PropType } from "vue";
import FormFieldTemplate from "./FormFieldTemplate.vue";
import type {
  FormFieldTemplateType,
  FormCheckBoxGroupOptionType,
} from "../core/AppType";

const props = defineProps({
  // form field template props (optional): label, required, tooltip, note, id
  fieldProps: {
    type: Object as PropType<FormFieldTemplateType>,
    default: {
      label: "Hello",
    },
  },
  value: Array as PropType<Array<string>>,
  options: {
    type: Array as PropType<Array<FormCheckBoxGroupOptionType>> | undefined,
    required: true,
    default: [{ code: 1, text: "Option 1" }],
  },
  // radio group name, has to be unique when using multiple radio groups
  name: {
    type: String,
    default: "radio-input",
  },
  disabled: { type: Boolean, default: false },
});

const emit = defineEmits(["updateValue"]);

const computedValue = computed({
  get() {
    return props.value;
  },
  set(newValue: Array<string>) {
    emit("updateValue", props.fieldProps.id, newValue);
  },
});
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "FormRadio",
});
</script>

<style scoped></style>
