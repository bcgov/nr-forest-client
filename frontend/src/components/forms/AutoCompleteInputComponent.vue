<script setup lang="ts">
import { ref, watch } from 'vue'
// Carbon
import '@carbon/web-components/es/components/combo-box/index';
// Composables
import { useEventBus } from '@vueuse/core'
// Types
import type { BusinessSearchResult } from '@/dto/CommonTypesDto'
import { isEmpty } from '@/dto/CommonTypesDto'

//Define the input properties for this component
const props = defineProps<{
  id: string
  label: string
  tip?: string
  placeholder?: string
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

//We initialize the error message handling for validation
const error = ref<string | undefined>(props.errorMessage || '')

const revalidateBus = useEventBus<void>('revalidate-bus')

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
  selectedValue = props.contents.find((entry) => entry.code === newValue)
  emit('update:model-value', selectedValue?.name ?? newValue)
  emit('update:selected-value', selectedValue)
  emit('empty', isEmpty(selectedValue))
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
  emitValueChange(event.target.value)
}

const onTyping = (event: any) => {
  inputValue.value = event.srcElement._filterInputValue
  emit('update:model-value', inputValue.value)
}

revalidateBus.on(() => validateInput(inputValue.value))
</script>

<template>
  <div class="groupong-02">
    <cds-combo-box
      :id="id"
      :name="id"
      :helper-text="tip"
      :title-text="label"
      :value="inputValue"
      :label="modelValue"
      filterable
      :invalid="error ? true : false"
      :invalid-text="error"
      @cds-combo-box-selected="selectAutocompleteItem"
      v-on:input="onTyping"
      :data-focus="id"
      :data-scroll="id"
      :data-id="'input-' + id"
      >

      <cds-combo-box-item
        v-for="item in contents"
        :key="item.code"
        :data-id="item.code"
        :data-value="item.name" 
        :value="item.code">
        {{ item.name }}
      </cds-combo-box-item>
      
    </cds-combo-box>
  </div>
</template>


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
