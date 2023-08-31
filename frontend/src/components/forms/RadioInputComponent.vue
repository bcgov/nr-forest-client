<script setup lang="ts">
import { ref, watch } from 'vue'
// Carbon 
import '@carbon/web-components/es/components/radio-button/index';
// Composables
import { useEventBus } from '@vueuse/core'
// Types
import type { CodeDescrType } from '@/dto/CommonTypesDto'
import { isEmpty } from '@/dto/CommonTypesDto'

const props = defineProps<{
  id: string
  label: string
  tip?: string
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

const revalidateBus = useEventBus<void>('revalidate-bus')

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

const updateSelectedValue = (event: any) =>
  (selectedValue.value = event.detail.value)
  revalidateBus.on(() => validateInput())
</script>

<template>
  <div class="grouping-01">
    <cds-radio-button-group
      :id="id + 'rb'"
      :name="id + 'rb'"
      label-position="right"
      orientation="vertical"
      :legend-text="label"
      :helper-text="tip"
      v-model="selectedValue"
      :invalid="error ? true : false"
      :invalid-text="error"
      @cds-radio-button-group-changed="updateSelectedValue"
      class="grouping-01"
      :data-focus="id"
      :data-scroll="id"
    >
      <cds-radio-button
        v-for="option in modelValue"
        :key="id + 'rb' + option.value"
        :label-text="option.text"
        :value="option.value"
      />
    </cds-radio-button-group>
  </div>
</template>


