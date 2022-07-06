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
        @input="updateValue"
        :checked="checked?.includes(option.code)"
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
    checked: Array as PropType<Array<String>>,
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
  methods: {
    updateValue(event: any) {
      let newModelValue = this.checked;
      if (newModelValue?.includes(event.target.value)) {
        newModelValue = newModelValue.filter((m) => m != event.target.value);
      } else {
        newModelValue?.push(event.target.value);
      }
      this.$emit("updateFormData", this.fieldProps.id, newModelValue);
    },
  },
});
</script>

<style scoped></style>
