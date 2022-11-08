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
      />
      {{ option.text }}
    </label>
  </FormFieldTemplate>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import type { PropType } from "vue";
import FormFieldTemplate from "./FormFieldTemplate.vue";
import type { FormFieldTemplateType } from "../core/AppType";

export default defineComponent({
  name: "FormRadio",
  components: {
    FormFieldTemplate,
  },
  props: {
    // form field template props (optional): label, required, tooltip, note, id
    fieldProps: {
      type: Object as PropType<FormFieldTemplateType>,
      default: {
        label: "Hello",
      },
    },
    value: Array as PropType<Array<String>>,
    options: {
      type: Array as
        | PropType<Array<{ code: string; text: string }>>
        | undefined,
      required: true,
      default: [{ code: 1, text: "Option 1" }],
    },
    // radio group name, has to be unique when using multiple radio groups
    name: {
      type: String,
      default: "radio-input",
    },
  },
  computed: {
    computedValue: {
      get() {
        return this.value;
      },
      set(newValue: string) {
        this.$emit("updateValue", this.fieldProps.id, newValue);
      },
    },
  },
});
</script>

<style scoped></style>
