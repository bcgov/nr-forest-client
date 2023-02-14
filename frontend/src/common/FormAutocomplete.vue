<template>
  <FormFieldTemplate :fieldProps="fieldProps">
    <b-form-input v-model="computedValue" :disabled="disabled" :state="state" :list="fieldProps.dataListId">
    </b-form-input>
    <datalist :id="fieldProps.dataListId">
      <option 
        v-for="entry in searchData"
        :key="entry.code"
        :value="entry.name">
        {{entry.name}}
      </option>      
    </datalist>    
  </FormFieldTemplate>
</template>

<script setup lang="ts">
import { computed, reactive, watch, ref } from "vue";
import type { PropType } from "vue";
import FormFieldTemplate from "./FormFieldTemplate.vue";
import type { FormFieldAutoCompleteTemplateType } from "../core/FormType";
import { useFetchTo } from "../services/forestClient.service";

const emit = defineEmits(["updateValue"]);

const props = defineProps({
  // form field template props (optional): label, required, tooltip, note, id, errorMsg
  fieldProps: {
    type: Object as PropType<FormFieldAutoCompleteTemplateType>,
    default: { id: "form-input",dataListId: "form-input-datalist" },
  },
  value: { type: [String, Number], required: true },
  disabled: { type: Boolean, default: false },
  state: { type: Boolean, default: null },
});

const computedValue = computed({
  get() {
    return props.value;
  },
  set(newValue: string | number) {
    emit("updateValue", newValue, props.fieldProps.modelName);
  },
});

let searchData = ref([]);

watch(computedValue,(curr,_) =>{
  if(curr.toString().length >= 3){
    useFetchTo(`/api/orgbook/name/${curr}`,searchData,{method: 'get'});
  }
});
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "FormAutoComplete",
});
</script>

<style scoped></style>
