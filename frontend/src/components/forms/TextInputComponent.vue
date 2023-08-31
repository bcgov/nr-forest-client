<script setup lang="ts">
import { ref, watch } from 'vue'
// Carbon
import '@carbon/web-components/es/components/text-input/index';
// Composables
import { useEventBus } from '@vueuse/core'
// Types
import { isEmpty } from '@/dto/CommonTypesDto'

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

const revalidateBus = useEventBus<void>('revalidate-bus')

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
  () => {
    selectedValue.value = props.modelValue
    validateInput(selectedValue.value)
  }
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

revalidateBus.on(() => {
  validateInput(selectedValue.value)
})
</script>

<template>
  <div class="grouping-02" v-if="enabled">
    <cds-text-input
      v-if="enabled"
      :id="id"
      :placeholder="placeholder"
      :value="selectedValue"
      :label="enabled ? label : null"
      :helper-text="tip"
      :disabled="!enabled"
      :invalid="error ? true : false"
      :invalid-text="error"
      v-mask="mask"
      @blur="(event:any) => validateInput(event.target.value)"
      @input="(event:any) => selectedValue = event.target.value"
      :data-focus="id"
      :data-scroll="id"
      :data-id="'input-' + id"
    />
  </div>

  <div v-if="!enabled" class="grouping-04">
    <div :data-scroll="id" class="grouping-04-label"><span :for="id" class="label-01">{{ label }}</span></div>
    <span class="text-01">{{ selectedValue }}</span>
  </div>

</template>

<style scoped>
.bx-input-disabled {
  height: 2.5rem;
  color: var(--light-theme-text-text-primary, #131315);
  padding-top: 0.69rem;
}
</style>
