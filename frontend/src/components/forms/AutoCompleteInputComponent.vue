<template>  
    <input
      :id="id"
      :data-id="'input-' + id"
      :list="id + 'list'"
      v-model="selectedValue"
      @blur="validateInput"
      @input="$emit('update:modelValue', $event.target.value)"
    />
    <datalist :id="id + 'list'">
      <option v-for="item in dataList" :key="item.code" :value="item.value" >
        {{ item.value }}
      </option>
    </datalist>
    <span v-if="error" class="error-message">{{ error }}</span>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { isEmpty, type CodeDescrType } from "@/core/CommonTypes";

//Define the input properties for this component
const props = defineProps({
  id: { type: String, required: true },
  modelValue: { type: String, required: true },
  dataList: { type: Array<CodeDescrType>, required: true },
  validations: { type: Array<Function>, required: true },
});

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: "error", value: string): void;
  (e: "empty", value: boolean): void;
  (e: "update:modelValue", value: string): void;
  (e: "update:selectedValue", value: CodeDescrType|undefined): void;
}>();

//We set the value prop as a reference for update reason
const selectedValue = ref(props.modelValue);
emit("empty", true);
//We initialize the error message handling for validation
const error = ref<string | undefined>("");

//We call all the validations
const validateInput = () => {
  if (props.validations) {
    error.value = props.validations
      .map((validation) => validation(selectedValue.value))
      .filter((errorMessage) => {
        if (errorMessage) return true;
        return false;
      })
      .shift();
  } 
  
  const value: CodeDescrType = props.dataList.find((entry) => entry.value === selectedValue.value);
  
  emit("update:modelValue", selectedValue.value);
  emit("update:selectedValue",value);
  emit("empty", isEmpty(value));
};

//We watch for error changes to emit events
watch(error, () => emit("error", error.value));

</script>
