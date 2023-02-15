<template>
  <FormFieldTemplate :fieldProps="fieldProps">
    <b-form-input v-model="computedValue" :disabled="disabled" :state="state" :list="fieldProps.dataListId">
    </b-form-input>
    <datalist :id="fieldProps.dataListId">
      <option 
        v-for="entry in searchData"        
        :value="entry.value">
        {{entry.text}}
      </option>      
    </datalist>    
  </FormFieldTemplate>
</template>

<script setup lang="ts">
import { computed, watch, ref } from "vue";
import type { PropType } from "vue";
import FormFieldTemplate from "./FormFieldTemplate.vue";
import type { FormFieldAutoCompleteTemplateType } from "../core/FormType";

//This is the event bus used to update the data and do the autocomplete
import EventBus from "@/services/EventBus";

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

//The property associated to the datalist
let searchData = ref([]);

//We wire the response of the event to the update of the datalist
EventBus.addEventListener(props.fieldProps.dataListId!, (ev: any) => {
  //We update the searchData with the event data  
  //The data should be previously converted
  searchData.value = ev.data;
});

//We watch the text data being changed
watch(computedValue,(curr,_) =>{
  //When the text data is bigger than the expected size
  if(curr.toString().length >= 3){
    //We emmit an event to notify that the data changed
    EventBus.emit(props.fieldProps.id,curr);    
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
