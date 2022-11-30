<template>
  <div>
    <FormInput
      v-if="schema.type == 'input'"
      :fieldProps="schema.fieldProps"
      :value="data"
      :disabled="schema.disabled ? true : false"
      @updateValue="updateFormValue"
    />
    <FormSelect
      v-if="schema.type == 'select'"
      :fieldProps="schema.fieldProps"
      :value="data"
      :options="schema.options"
      :disabled="schema.disabled ? true : false"
      @updateValue="updateFormValue"
    />
    <FormCheckboxGroup
      v-if="schema.type == 'checkboxgroup'"
      :fieldProps="schema.fieldProps"
      :value="data"
      :options="schema.options"
      :disabled="schema.disabled ? true : false"
      @updateValue="updateFormValue"
    />
    <FormRadioGroup
      v-if="schema.type == 'radiogroup'"
      :fieldProps="schema.fieldProps"
      :value="data"
      :options="schema.options"
      :disabled="schema.disabled ? true : false"
      @updateValue="updateFormValue"
    />
  </div>
</template>

<script setup lang="ts">
import type { PropType } from "vue";
import FormInput from "./FormInput.vue";
import FormSelect from "./FormSelect.vue";
import FormCheckboxGroup from "./FormCheckboxGroup.vue";
import FormRadioGroup from "./FormRadioGroup.vue";
import type {
  FormComponentSchemaType,
  CommonObjectType,
} from "../core/AppType";

const props = defineProps({
  schema: {
    type: Object as PropType<FormComponentSchemaType>,
    required: true,
  },
  data: {
    type: [String, Object, Number, Array],
    required: true,
  },
});

const emit = defineEmits(["updateFormValue"]);

const updateFormValue = (id, newValue) => {
  emit("updateFormValue", id, newValue);
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "FormComponentOptions",
});
</script>

<style scoped></style>
