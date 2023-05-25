<template>
  <b-form-input :id="id"    
    v-model="selectedValue"
    :list="id + 'list'"
    autocomplete="off"
    type="text"
    @blur="event => validateInput(selectedValue)"
    @input="event => emitValueChange(selectedValue)"
    />    
    <datalist :id="id + 'list'">
      <option v-for="item in contents" :key="item.code" :value="item.name" >
        {{ item.name }}
      </option>
    </datalist>
    <span v-if="error" class="error-message">{{ error }}</span>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { isEmpty, type BusinessSearchResult } from "@/core/CommonTypes";

//Define the input properties for this component
const props = defineProps({
  id: { type: String, required: true },
  modelValue: { type: String, required: true },
  contents: { type: Array<BusinessSearchResult>, required: true },
  validations: { type: Array<Function>, required: true },
});

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: "error", value: string|undefined): void;
  (e: "empty", value: boolean): void;
  (e: "update:modelValue", value: string): void;
  (e: "update:selectedValue", value: BusinessSearchResult|undefined): void;
}>();


//We initialize the error message handling for validation
const error = ref<string | undefined>("");

//We watch for error changes to emit events
watch(error, () => emit("error", error.value));

//We set the value prop as a reference for update reason
const selectedValue = ref("");

//This function emits the events on update
const emitValueChange = (newValue: string) : void =>{
  const reference = props.contents.find((entry) => entry.name === newValue);  
  emit("update:modelValue", newValue);
  emit("update:selectedValue",reference);  
  emit("empty", isEmpty(reference));

};

emit("empty", true);

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
