<template>
  <div class="grouping-03">
  <bx-dropdown
    :id="id"
    :value="selectedValue"
    :label-text="label"
    :helper-text="tip"
    :invalid="error ? true : false"
    :validityMessage="error"
    @bx-dropdown-beingselected="(target:any) => selectedValue = target.detail.item.__value"
  >
    <bx-dropdown-item
      v-for="option in modelValue"
      :key="option.code"
      :value="option.code"
      >{{ option.name }}</bx-dropdown-item
    >
  </bx-dropdown>
</div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { type CodeNameType, isEmpty } from '@/dto/CommonTypesDto'

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

//We watch for error changes to emit events
watch(error, () => emit('error', error.value))
watch(
  () => props.errorMessage,
  () => (error.value = props.errorMessage)
)

//We set it as a separated ref due to props not being updatable
const selectedValue = ref(props.initialValue)
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
watch(
  () => props.modelValue,
  () => {
    selectedValue.value = ''
    setTimeout(() => (selectedValue.value = props.initialValue), 400)
  }
)
watch(
  () => props.initialValue,
  () => {
    if(selectedValue.value === props.initialValue) return
    setTimeout(() => (selectedValue.value = props.initialValue), 400)
  }
)
</script>
