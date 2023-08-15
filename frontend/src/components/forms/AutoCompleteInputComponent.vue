<template>
  <bx-form-item>
    <bx-input
      :id="id"
      :name="id"
      type="text"
      :data-id="'input-' + id"
      :placeholder="'Start typing to search for your ' + label"
      :value="inputValue"
      :label-text="label"
      :helper-text="tip"
      @focus="autoCompleteVisible = true"
      :invalid="error ? true : false"
      :validityMessage="error"
      @blur="(event:any) => blur(event.target.value)"
      @input="(event:any) => inputValue = event.target.value"
    />
    <div
      class="autocomplete-items"
      :id="id + 'list'"
      v-show="
        autoCompleteVisible && inputValue.length > 2 && contents.length > 0
      "
    >
      <div class="autocomplete-items-ct" v-if="loading">
        <bx-inline-loading status="active">Loading data...</bx-inline-loading>
      </div>
      <div class="autocomplete-items-ct" v-else>
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
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { isEmpty, type BusinessSearchResult } from '@/dto/CommonTypesDto'

//Define the input properties for this component
const props = defineProps<{
  id: string
  label: string
  tip?: string
  modelValue: string
  contents: Array<BusinessSearchResult>
  validations: Array<Function>
  errorMessage?: string
  loading?: boolean
}>()

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: 'error', value: string | undefined): void
  (e: 'empty', value: boolean): void
  (e: 'update:model-value', value: string): void
  (e: 'update:selected-value', value: BusinessSearchResult | undefined): void
}>()

const autoCompleteVisible = ref(false)

//We initialize the error message handling for validation
const error = ref<string | undefined>(props.errorMessage || '')

//We watch for error changes to emit events
watch(error, () => emit('error', error.value))
watch(
  () => props.errorMessage,
  () => (error.value = props.errorMessage)
)

//We set the value prop as a reference for update reason
const inputValue = ref(props.modelValue)

let selectedValue: BusinessSearchResult | undefined = undefined

//This function emits the events on update
const emitValueChange = (newValue: string): void => {
  const reference = props.contents.find((entry) => entry.name === newValue)
  emit('update:model-value', newValue)
  emit('empty', isEmpty(reference))

  /* 
  Fire the update:selected-value with undefined when:
  - there was an item previously selected (clicked);
  - and the value in the input was changed by typing or removing some
  characters.
  Note: we know the change was not due to selecting (clicking) a new item
  because otherwise the newValue would already correspond to the
  selectedValue.name.
  */
  if (selectedValue && newValue !== selectedValue.name) {
    selectedValue = undefined
    emit('update:selected-value', selectedValue)
  }
}

emit('empty', true)
watch(
  () => props.modelValue,
  () => {
    inputValue.value = props.modelValue
    validateInput(inputValue.value)
  }
)
watch([inputValue], () => {
  validateInput(inputValue.value)
  emitValueChange(inputValue.value)
})

const blur = (newValue: string) => {
  validateInput(newValue)
  setTimeout(() => (autoCompleteVisible.value = false), 150)
}
//We call all the validations
const validateInput = (newValue: string) => {
  if (props.validations) {
    error.value =
      props.validations
        .map((validation) => validation(newValue))
        .filter((errorMessage) => {
          if (errorMessage) return true
          return false
        })
        .shift() ?? props.errorMessage
  }
}

const selectAutocompleteItem = (event: any) => {
  const newValue = event.target.getAttribute('data-value')
  selectedValue = props.contents.find((entry) => entry.name === newValue)
  inputValue.value = newValue
  emit('update:selected-value', selectedValue)
  autoCompleteVisible.value = false
}
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
