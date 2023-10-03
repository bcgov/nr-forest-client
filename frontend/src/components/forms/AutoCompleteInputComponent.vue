<script setup lang="ts">
import { ref, computed, watch } from 'vue'
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
  () => (error.value = props.errorMessage),
)

//We set the value prop as a reference for update reason
const inputValue = ref(props.modelValue)

// This is to make the input list contains the selected value to show when component render
const inputList = computed<Array<BusinessSearchResult>>(() =>
  (!props.contents || props.contents.length === 0
    ? [{ name: props.modelValue, code: '', status: '', legalType: '' }]
    : props.contents
  ).filter((entry) => entry.name),
)

let selectedValue: BusinessSearchResult | undefined = undefined

//This function emits the events on update
const emitValueChange = (newValue: string): void => {
  selectedValue = inputList.value.find((entry) => entry.code === newValue)
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
  },
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
  emitValueChange(event?.detail?.item?.getAttribute('data-id'))
}

const onTyping = (event: any) => {
  inputValue.value = event.srcElement._filterInputValue
  emit('update:model-value', inputValue.value)
}

revalidateBus.on(() => validateInput(inputValue.value))
</script>

<template>
  <div class="grouping-02">
    <cds-combo-box
      :id="id"
      :name="id"
      :helper-text="tip"
      :title-text="label"
      :value="inputValue"
      filterable
      :invalid="error ? true : false"
      :invalid-text="error"
      @cds-combo-box-selected="selectAutocompleteItem"
      v-on:input="onTyping"
      v-on:blur="(event: any) => validateInput(event.srcElement._filterInputValue)"
      :data-focus="id"
      :data-scroll="id"
      :data-id="'input-' + id"
    >
      <cds-combo-box-item
        v-for="item in inputList"
        :key="item.code"
        :data-id="item.code"
        :data-value="item.name"
        :value="item.name"
      >
        {{ item.name }}
      </cds-combo-box-item>
    </cds-combo-box>
  </div>
</template>
