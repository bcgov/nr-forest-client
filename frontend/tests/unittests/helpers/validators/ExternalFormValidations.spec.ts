import { describe, it, expect } from 'vitest'

import { 
  getField,
  getValidations,
  validate,
  addValidation 
} from '@/helpers/validators/ExternalFormValidations'

describe('ExternalFormValidations.ts', () => {

  const formDataDto = {
    businessInformation: {
      businessType: 'JJ',
      legalType: '',
      clientType: '',
      incorporationNumber: '',
      businessName: 'The Test Client Corp',
      goodStandingInd: '',
    },
    location: {
      addresses: [
        {
          locationName: 'Mailing address',
          streetAddress: '',
          country: { value: 'CA', text: 'Canada' },
          province: { value: '', text: '' },
          city: '',
          postalCode: 'A1A1A1',
        },
      ],
      contacts: [{
        locationNames: [{text:'Mailing address', value:'0'}],
      }],
    },
  }

  it('should get a list of functions for a field', () => {
    addValidation('test', () => '')
    expect(getValidations('test')).toEqual([expect.any(Function)])
  })
  it('should return an empty array for invalid fields', () => {
    expect(getValidations('test2')).toEqual([])
  })
  it('should return the value of a simple field', () => {
    expect(getField('businessInformation.businessType', formDataDto)).toEqual('JJ')
  })
  it('should return the value of a nested field', () => {
    expect(getField('location.addresses.*.country.text', formDataDto)).toEqual(['Canada'])
  })
  it('should return the value of a nested field with a condition', () => {
    expect(getField('location.addresses.*.postalCode(location.addresses.*.country.text === "CA")', formDataDto)).toEqual(['A1A1A1'])
  })
  it('should return the value of a nested field with a condition', () => {
    expect(getField('location.addresses.*.postalCode(location.addresses.*.country.text === "US")', formDataDto)).toEqual(['A1A1A1'])
  })
  it('should return the value of a nested field within a nested field', () => {
    expect(getField('location.contacts.*.locationNames.*.text', formDataDto)).toEqual([['Mailing address']])
  })
  it('should return true for a valid businessName', () => {
    expect(validate(['businessInformation.businessName'], formDataDto)).toBeTruthy()
  })
  it('should return true for nested fields', () => {
    expect(validate(['location.addresses.*.locationName'], formDataDto)).toBeTruthy()
  })
  it('should return true for nested fields with conditions', () => {
    expect(validate(['location.addresses.*.postalCode(location.addresses.*.country.text === "CA")'], formDataDto)).toBeTruthy()
  })
  it('should return true for nested fields withing nested fields', () => {
    expect(validate(['location.contacts.*.locationNames.*.text'], formDataDto)).toBeTruthy()
  })
  it('should return true for fields with no validations', () => {
    expect(validate(['businessInformation.businessType'], formDataDto)).toBeTruthy()
  })
  it('should return true for nested fields with conditions', () => {
    expect(validate(['location.addresses.*.postalCode(location.addresses.*.country.text === "CA")'], formDataDto)).toBeTruthy()
  })
})