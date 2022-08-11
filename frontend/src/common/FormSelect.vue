<template>
  <FormFieldTemplate :fieldProps="fieldProps">
    <b-form-select
      v-model="computedSelectedValue"
      :options="options"
    ></b-form-select>
  </FormFieldTemplate>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import type { PropType } from "vue";
import FormFieldTemplate from "./FormFieldTemplate.vue";
import type {
  CommonObjectType,
  FormFieldTemplateType,
  FromSelectOptionType,
} from "../core/AppType";

export default defineComponent({
  name: "FormSelect",
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
    selectedValue: [Object, String],
    options: {
      type: Array as PropType<Array<FromSelectOptionType>>,
      required: true,
    },
  },
  computed: {
    computedSelectedValue: {
      get() {
        return this.selectedValue;
      },
      set(newValue: CommonObjectType | string) {
        this.$emit("updateFormData", this.fieldProps.id, newValue);
      },
    },
  },
});
</script>

<style scoped></style>
