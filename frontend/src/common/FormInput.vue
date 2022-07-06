<template>
  <FormFieldTemplate :fieldProps="fieldProps">
    <b-form-input :value="inputValue" @input="updateInput"></b-form-input>
  </FormFieldTemplate>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import type { PropType } from "vue";
import FormFieldTemplate from "./FormFieldTemplate.vue";
import type { FormFieldTemplateType } from "../core/AppType";

export default defineComponent({
  name: "FormInput",
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
    inputValue: String,
  },
  methods: {
    updateInput(newValue: String) {
      this.$emit("updateFormData", this.fieldProps.id, newValue);
    },
  },
});
</script>

<style scoped></style>
