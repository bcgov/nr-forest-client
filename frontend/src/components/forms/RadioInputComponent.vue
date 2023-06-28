<template>
  <div>
    <label :for="id + 'rb'" class="bx--label">{{ label }}</label>
    <bx-radio-button-group
      label-position="right"
      orientation="vertical"
      :name="id + 'rb'"
      :id="id + 'rb'"
      v-model="selectedValue"
      @bx-radio-button-group-changed="updateSelectedValue"
    >
      <bx-radio-button
        v-for="option in modelValue"
        :key="id + 'rb' + option.value"
        :label-text="option.text"
        :value="option.value"
      />
    </bx-radio-button-group>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { type CodeDescrType, isEmpty } from '@/core/CommonTypes'

const props = defineProps<{
  id: string
  label: string
  modelValue: Array<CodeDescrType>
  initialValue: string
  validations: Array<Function>
  errorMessage?: string
}>()

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: 'error', value: string | undefined): void
  (e: 'empty', value: boolean): void
  (e: 'update:model-value', value: string | undefined): void
}>()

const selectedValue = ref<string>(props.initialValue)
//We initialize the error message handling for validation
const error = ref<string | undefined>(props.errorMessage || '')

//We watch for error changes to emit events
watch(error, () => emit('error', error.value))
watch(
  () => props.errorMessage,
  () => (error.value = props.errorMessage)
)

//We call all the validations
const validateInput = () => {
  if (props.validations) {
    error.value =
      props.validations
        .map((validation) => validation(selectedValue.value))
        .filter((errorMessage) => {
          if (errorMessage) return true
          return false
        })
        .shift() ?? props.errorMessage
  }
  emit('empty', isEmpty(selectedValue))
}

//We watch for input changes to emit events
watch(selectedValue, () => {
  validateInput()
  emit(
    'update:model-value',
    props.modelValue
      .map((entry) => entry.value)
      .find((entry) => entry === selectedValue.value)
  )
  emit('empty', isEmpty(selectedValue))
})

//We watch for error changes to emit events
watch(error, () => emit('error', error.value))

const updateSelectedValue = (event: any) =>
  (selectedValue.value = event.detail.value)
//validateInput()
</script>
