import { useEventBus } from '@vueuse/core'
import type { ValidationMessageType } from '@/core/CommonTypes'

const errorBus = useEventBus<ValidationMessageType[]>(
  'submission-error-notification'
)

let submissionValidators = []

errorBus.on((errors) => {
  submissionValidators = errors.map((error) => {
    return { ...error, originalValue: '' }
  })
})

const updateValidators = (fieldId: string, value: string) => {
  submissionValidators = submissionValidators.map((validator) => {
    if (validator.fieldId === fieldId) {
      return { ...validator, originalValue: value }
    }
    return validator
  })
}

export const submissionValidation = (
  fieldName: string
): ((value: string) => string) => {
  return (value: string) => {
    const foundError = submissionValidators.find(
      (validator) => validator.fieldId === fieldName
    )
    if (
      foundError &&
      (foundError.originalValue === value || foundError.originalValue === '')
    ) {
      updateValidators(fieldName, value)
      return foundError.errorMsg
    }
    return ''
  }
}
