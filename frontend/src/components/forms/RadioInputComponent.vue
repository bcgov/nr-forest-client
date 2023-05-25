<template>  
    <b-form-group label="Choose one these options:" v-slot="{ ariaDescribedby}">
      <b-form-radio v-for="(option, index) in modelValue" :key="index"
        v-model="selectedValue"
        :id="id+'_'+option.value"
        :value="option.value"
        :name="option.text"
        :aria-describedby="ariaDescribedby"
        @change="validateInput"
      >{{  option.text  }}</b-form-radio>
    </b-form-group>      
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { type CodeDescrType, isEmpty } from "@/core/CommonTypes";

const props = defineProps({
  id: { type: String, required: true },
  modelValue: {
    type: Array<CodeDescrType>,
    required: true,
  },
  validations: { type: Array<Function>, required: true },
});

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: "error", value: string): void;
  (e: "empty", value: boolean): void;
  (e: "update:modelValue", value: string|undefined): void;
}>();

const selectedValue = ref({});
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
      .shift() ?? "";
  }
  emit("empty", isEmpty(selectedValue));
};

//We watch for input changes to emit events
watch(selectedValue, () => {
  if (error.value) {
    error.value = "";
  }  
  emit("update:modelValue",
    props.modelValue.map(entry => entry.value).find(entry => entry === selectedValue.value)
  );

});

//We watch for error changes to emit events
watch(error, () => emit("error", error.value));
</script>
