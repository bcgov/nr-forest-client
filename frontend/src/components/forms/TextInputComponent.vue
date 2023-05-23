<template>
  <b-form-input 
    :id="id"                        
    :data-id="'input-' + id"
    v-model="selectedValue"
    @blur="validateInput"
    @input="$emit('update:modelValue', $event.target.value)"
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

//We initialize the error message handling for validation
const error = ref<string | undefined>("");
//We set it as a separated ref due to props not being updatable
const selectedValue = ref<string>(props.modelValue);

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: "error", value: string): void;
  (e: "empty", value: boolean): void;
  (e: "update:modelValue", value: string): void;
}>();

//We set the value prop as a reference for update reason
emit("empty", isEmpty(props.modelValue));


//We call all the validations
const validateInput = () => {  
  if (props.validations) {
    error.value = props.validations
      .map((validation) => validation(selectedValue.value))
      .filter((errorMessage) => {
        if (errorMessage) return true;
        return false;
      })
      .shift() ?? "";
  }
  emit("update:modelValue", selectedValue.value);
  emit("empty", isEmpty(selectedValue));
};

//We watch for error changes to emit events
watch(error, () => emit("error", error.value));
</script>
