<template>
  <FormFieldTemplate :fieldProps="fieldProps">
    <b-form-input v-model="computedValue"></b-form-input>
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
    value: [String, Number],
  },
  computed: {
    computedValue: {
      get() {
        return this.value;
      },
      set(newValue: string | number) {
        this.$emit("updateValue", this.fieldProps.id, newValue);
      },
    },
  },
});
</script>

<style scoped></style>
