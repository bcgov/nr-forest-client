<template>
  <b-form-input 
    :id="id"                        
    :data-id="'input-' + id"
    v-model="selectedValue"
    @blur="event => validateInput(selectedValue)"
    @input="event => emitValueChange(selectedValue)"
    />
    <span v-if="error" class="error-message">{{ error }}</span>  
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { isEmpty } from "@/core/CommonTypes";

//Define the input properties for this component
const props = defineProps({
  id: { type: String, required: true },
  modelValue: { type: String, required: true },
  validations: { type: Array<Function>, required: true },
});

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: "error", value: string|undefined): void;
  (e: "empty", value: boolean): void;
  (e: "update:modelValue", value: string|undefined): void;
}>();


//We initialize the error message handling for validation
const error = ref<string | undefined>("");

//We watch for error changes to emit events
watch(error, () => emit("error", error.value));

//We set it as a separated ref due to props not being updatable
const selectedValue = ref<string>(props.modelValue);
//We set the value prop as a reference for update reason
emit("empty", isEmpty(props.modelValue));
//This function emits the events on update
const emitValueChange = (newValue: string) : void =>{
  emit("update:modelValue", newValue);
  emit("empty", isEmpty(newValue));
};
//Watch for changes on the input
watch([selectedValue],() => emitValueChange(selectedValue.value));


//We call all the validations
const validateInput = (newValue:string) => {  
  if (props.validations) {
    error.value = props.validations
      .map((validation) => validation(newValue))
      .filter((errorMessage) => {
        if (errorMessage) return true;
        return false;
      })
      .shift() ?? "";
  }
};


</script>
