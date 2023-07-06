import { describe, it, expect, beforeEach } from 'vitest'
import { useEventBus } from '@vueuse/core'
import type { ValidationMessageType } from '@/dto/CommonTypesDto'
import { submissionValidation } from '@/helpers/validators/SubmissionValidators'

describe('SubmissionValidators', () => {
  const errorBus = useEventBus<ValidationMessageType[]>(
    'submission-error-notification'
  )

  beforeEach(() => {
    errorBus.emit([{ fieldId: 'test', errorMsg: 'test error message' }])
  })

  it('should return empty string if no error is found', () => {
    const validate = submissionValidation('demo')
    expect(validate('test')).toBe('')
  })

  it('should return an error message if error is found', () => {
    const validate = submissionValidation('test')
    expect(validate('test')).toBe('test error message')
  })

  it('should return an error message then empty string when field is updated', () => {
    const validate = submissionValidation('test')
    expect(validate('test')).toBe('test error message')
    expect(validate('test updated')).toBe('')
  })

  it('should return an error message then empty string when field is updated', () => {
    const validate = submissionValidation('orange')
    expect(validate('orange')).toBe('')
    expect(validate('orange')).toBe('')
    errorBus.emit([{ fieldId: 'apple', errorMsg: 'test error message' }])
    expect(validate('orange')).toBe('')
    errorBus.emit([{ fieldId: 'orange', errorMsg: 'test error message' }])
    expect(validate('orange')).toBe('test error message')
    expect(validate('not orange')).toBe('')
  })
})
