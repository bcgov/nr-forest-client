<template>
  <div>
    <label v-if="!enabled" :for="id" class="bx--label">{{ label }}</label>
    <bx-form-item>
      <bx-input
        :id="id"
        :data-id="'input-' + id"
        :placeholder="placeholder"
        :value="selectedValue"
        :label-text="enabled ? label : null"
        :helper-text="tip"
        :disabled="!enabled"
        :color-scheme="enabled ? '' : 'light'"
        :invalid="error ? true : false"
        :validityMessage="error"
        v-mask="mask"
        @blur="(event:any) => validateInput(event.target.value)"
        @input="(event:any) => selectedValue = event.target.value"
      />
    </bx-form-item>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { isEmpty } from '@/core/CommonTypes'

//Define the input properties for this component
const props = defineProps<{
  id: string
  label: string
  tip?: string
  enabled?: boolean
  placeholder: string
  modelValue: string
  validations: Array<Function>
  errorMessage?: string
  mask?: string
}>()

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: 'error', value: string | undefined): void
  (e: 'empty', value: boolean): void
  (e: 'update:model-value', value: string): void
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
const selectedValue = ref<string>(props.modelValue)

//We set the value prop as a reference for update reason
emit('empty', isEmpty(props.modelValue))
watch(
  () => props.modelValue,
  () => (selectedValue.value = props.modelValue)
)

//This function emits the events on update
const emitValueChange = (newValue: string): void => {
  emit('update:model-value', newValue)
  emit('empty', isEmpty(newValue))
}
//Watch for changes on the input
watch([selectedValue], () => emitValueChange(selectedValue.value))

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

validateInput(selectedValue.value)
</script>
