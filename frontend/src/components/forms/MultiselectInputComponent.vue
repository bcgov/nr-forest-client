<template>
  <div>
    <bx-dropdown
      :value="selectedValue"
      :label-text="label"
      :helper-text="tip"
      @bx-dropdown-beingselected="(target:any) => validateInput(target.detail.item.__value)"
      @bx-dropdown-selected="(target:any) => addFromSelection(target.detail.item.__value)"
    >
      <bx-dropdown-item
        v-for="option in modelValue"
        :key="option.code"
        :value="option.code"
        >{{ option.name }}</bx-dropdown-item
      >
    </bx-dropdown>
    <bx-tag v-for="tag in items" type="blue" title="Clear selection">
      {{ tag }} <closeOutline16 @click="removeFromSelection(tag)" />
    </bx-tag>
  </div>
</template>
<script setup lang="ts">
import { ref, watch } from "vue";
import { type CodeNameType, isEmpty } from "@/core/CommonTypes";
import closeOutline16 from "@carbon/icons-vue/es/close/16";

//Define the input properties for this component
const props = defineProps<{
  id: string;
  label: string;
  tip: string;
  modelValue: Array<CodeNameType>;
  selectedValues: Array<string>;
  initialValue: string;
  validations: Array<Function>;
}>();

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: "error", value: string | undefined): void;
  (e: "empty", value: boolean): void;
  (e: "update:modelValue", value: string[] | undefined): void;
  (e: "update:selectedValue", value: Array<CodeNameType> | undefined): void;
}>();

//We initialize the error message handling for validation
const error = ref<string | undefined>("");

//We watch for error changes to emit events
watch(error, () => emit("error", error.value));

//We set it as a separated ref due to props not being updatable
const selectedValue = ref("");
//We set the value prop as a reference for update reason
emit("empty", isEmpty(props.modelValue));

//Watch for changes on the input
watch([selectedValue], () => validateInput(selectedValue.value));

//We call all the validations
const validateInput = (newValue: any) => {
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
};
watch(
  () => props.modelValue,
  () => (selectedValue.value = props.initialValue)
);

//Controls the selected values
const items = ref<string[]>([]);


const emitChange = () => {
  const reference = props.modelValue.filter((entry) => items.value.includes(entry.name));
  emit("update:modelValue", items.value);
  emit("update:selectedValue", reference);
  emit("empty", reference.length === 0);
}

const addFromSelection = (itemCode: string) => {
  const reference = props.modelValue.find((entry) => entry.code === itemCode);
  if (reference) items.value.push(reference.name);
  emitChange();
};

const removeFromSelection = (itemName: string) => {
  items.value = items.value.filter((entry) => entry !== itemName);
  emitChange();
}

watch([items], () => emitChange());

props.selectedValues?.forEach((value:string) => addFromSelection(value));

</script>
