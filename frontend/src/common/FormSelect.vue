<template>
  <FormFieldTemplate :fieldProps="fieldProps">
    <b-form-select
      v-model="computedValue"
      :options="options"
      :disabled="disabled"
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
    value: [Object, String],
    disabled: { type: Boolean, default: false },
    options: {
      type: Array as PropType<Array<FromSelectOptionType>>,
      required: true,
    },
  },
  computed: {
    computedValue: {
      get() {
        return this.value;
      },
      set(newValue: CommonObjectType | string) {
        this.$emit("updateValue", this.fieldProps.id, newValue);
      },
    },
  },
});
</script>

<style scoped></style>
