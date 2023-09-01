<script setup lang="ts">
import { ref, computed, watch } from 'vue'
// Carbon
import '@carbon/web-components/es/components/combo-box/index';
// Composables
import { useEventBus } from '@vueuse/core'
// Types
import type { CodeNameType } from '@/dto/CommonTypesDto'
import { isEmpty } from '@/dto/CommonTypesDto'

//Define the input properties for this component
const props = defineProps<{
  id: string
  label: string
  tip: string
  modelValue: Array<CodeNameType>
  initialValue: string
  validations: Array<Function>
  errorMessage?: string
}>()

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: 'error', value: string | undefined): void
  (e: 'empty', value: boolean): void
  (e: 'update:modelValue', value: string | undefined): void
  (e: 'update:selectedValue', value: CodeNameType | undefined): void
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

//We set it as a separated ref due to props not being updatable
const selectedValue = ref(props.initialValue)
// This is to make the input list contains the selected value to show when component render
const inputList = computed<Array<CodeNameType>>(() =>
  ((!props.modelValue || props.modelValue.length === 0) ? [{name: props.initialValue, code: '',status:'',legalType:''}] : props.modelValue)
)

//We set the value prop as a reference for update reason
emit('empty', isEmpty(props.initialValue))
//This function emits the events on update
const emitValueChange = (newValue: string): void => {
  const reference = newValue
    ? props.modelValue.find((entry) => entry.code === newValue)
    : undefined

  emit('update:modelValue', newValue)
  emit('update:selectedValue', reference)
  emit('empty', isEmpty(newValue))
}
//Watch for changes on the input
watch([selectedValue], () => {
  validateInput(selectedValue.value)
  emitValueChange(selectedValue.value)
})

//We call all the validations
const validateInput = (newValue: any) => {
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

watch(inputList,() => (selectedValue.value = props.initialValue))

revalidateBus.on(() => validateInput(selectedValue.value))
</script>


<template>
  <div class="grouping-03">
    <cds-combo-box
      :id="id"
      filterable
      :helper-text="tip"
      :title-text="label"
      :value="selectedValue"
      :invalid="error ? true : false"
      :invalid-text="error"
      @cds-combo-box-selected="(event:any) => selectedValue = event.detail.item.getAttribute('data-id')"
      :data-focus="id"
      :data-scroll="id">
      <cds-combo-box-item 
        v-for="option in inputList"
        :key="option.code"
        :value="option.name"
        :data-id="option.code"
        :data-value="option.name">
        {{ option.name }}
      </cds-combo-box-item>
    </cds-combo-box>
</div>
</template>

