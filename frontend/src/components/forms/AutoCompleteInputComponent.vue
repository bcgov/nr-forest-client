<template>
  <bx-form-item>
    <bx-input
      :id="id"
      :name="id"
      type="text"
      :data-id="'input-' + id"
      :placeholder="'Start typing to search for your ' + label"
      :value="selectedValue"
      :label-text="label"
      :helper-text="tip"
      @focus="autoCompleteVisible = true"
      @blur="(event:any) => validateInput(event.target.value)"
      @input="(event:any) => selectedValue = event.target.value"
    />
    <div
      class="autocomplete-items"
      :id="id + 'list'"
      v-show="autoCompleteVisible && selectedValue.length > 2"
    >
      <div class="autocomplete-items-ct">
        <div
          v-for="item in contents"
          :key="item.code"
          :data-id="item.code"
          :data-value="item.name"
          class="autocomplete-items-cell"
          @click="selectAutocompleteItem"
        >
          <strong :data-id="item.code" :data-value="item.name">{{
            item.name
          }}</strong>
        </div>
      </div>
    </div>
  </bx-form-item>

  <span v-if="error" class="error-message">{{ error }}</span>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { isEmpty, type BusinessSearchResult } from "@/core/CommonTypes";

//Define the input properties for this component
const props = defineProps({
  id: { type: String, required: true },
  label: { type: String, required: true },
  tip: { type: String, required: false },
  modelValue: { type: String, required: true },
  contents: { type: Array<BusinessSearchResult>, required: true },
  validations: { type: Array<Function>, required: true },
});

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: "error", value: string | undefined): void;
  (e: "empty", value: boolean): void;
  (e: "update:model-value", value: string): void;
  (e: "update:selected-value", value: BusinessSearchResult | undefined): void;
}>();

const autoCompleteVisible = ref(false);

//We initialize the error message handling for validation
const error = ref<string | undefined>("");

//We watch for error changes to emit events
watch(error, () => emit("error", error.value));

//We set the value prop as a reference for update reason
const selectedValue = ref(props.modelValue);

//This function emits the events on update
const emitValueChange = (newValue: string): void => {
  const reference = props.contents.find((entry) => entry.name === newValue);
  emit("update:model-value", newValue);
  emit("empty", isEmpty(reference));
};

emit("empty", true);
watch(
  () => props.modelValue,
  () => (selectedValue.value = props.modelValue)
);
watch([selectedValue], () => emitValueChange(selectedValue.value));

//We call all the validations
const validateInput = (newValue: string) => {
  if (props.validations) {
    error.value =
      props.validations
        .map((validation) => validation(newValue))
        .filter((errorMessage) => {
          if (errorMessage) return true;
          return false;
        })
        .shift() ?? "";
  }
  setTimeout(() => (autoCompleteVisible.value = false), 150);
};

const selectAutocompleteItem = (event: any) => {
  const newValue = event.target.getAttribute("data-value");
  const reference = props.contents.find((entry) => entry.name === newValue);
  selectedValue.value = newValue;
  emit("update:selected-value", reference);
  autoCompleteVisible.value = false;
};
</script>
<style scoped>
.autocomplete {
  /*the container must be positioned relative:*/
  position: relative;
  display: inline-block;
}
.autocomplete-items {
  border-bottom: none;
  border-top: none;
  z-index: 99;
  left: 0;
  right: 0;
}
.autocomplete-items-ct {
  border: 1px solid #d4d4d4;
  position: absolute;
  padding: 10px;
  cursor: pointer;
  background-color: #fff;
  border-bottom: 1px solid #d4d4d4;
}

.autocomplete-items-cell {
  padding: 10px;
  cursor: pointer;
  background-color: #fff;
  border-bottom: 1px solid #d4d4d4;
}
.autocomplete-items-cell:hover {
  /*when hovering an item:*/
  background-color: #e9e9e9;
}
</style>
