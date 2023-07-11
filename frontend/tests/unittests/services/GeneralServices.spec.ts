import { describe, it, expect, beforeEach } from 'vitest'
import { conversionFn } from '@/services/GeneralServices'

describe('GeneralServices.ts', () => {
  let code: any

  beforeEach(() => {
    code = {
      code: 'code',
      name: 'name',
      legalType: 'legalType'
    }
  })

  it('returns an object with the correct properties', () => {
    const result = conversionFn(code)

    expect(result).toEqual({
      value: { value: 'code', text: 'name' },
      text: 'name',
      legalType: 'legalType'
    })
  })
})
