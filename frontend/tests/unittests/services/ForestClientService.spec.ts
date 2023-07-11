import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { addNewAddress, addNewContact } from '@/services/ForestClientService'
import type { Contact, Address } from '@/dto/ApplyClientNumberDto'

describe('ForestClientService.ts', () => {
  // create an array of Address and an array of Contact to be used during test and set some random values inside beforeEach and clean it up before the next test

  let addresses: Address[]
  let contacts: Contact[]

  const sampleAddress = {
    streetAddress: '2975 Jutland Rd',
    country: {
      value: 'CA',
      text: 'Canada'
    },
    province: {
      value: 'BC',
      text: 'British Columbia'
    },
    city: 'Victoria',
    postalCode: 'V8T5J9',
    locationName: 'Mailing Address'
  }
  const sampleContact = {
    contactType: {
      value: 'P',
      text: 'Person'
    },
    firstName: 'Jhon',
    lastName: 'Wick',
    phoneNumber: '(111) 222-3344',
    email: 'babayaga@theguild.ca',
    locationNames: [
      {
        value: '0',
        text: 'Mailing Address'
      }
    ]
  }

  beforeEach(() => {
    addresses = [sampleAddress]
    contacts = [sampleContact]
  })

  afterEach(() => {
    addresses = []
    contacts = []
  })

  it('add new address and return the result while modifying the original', () => {
    const result = addNewAddress(addresses)

    expect(result).toEqual(2)
    expect(addresses).toStrictEqual([
      sampleAddress,
      {
        locationName: '',
        streetAddress: '',
        country: { value: '', text: '' },
        province: { value: '', text: '' },
        city: '',
        postalCode: ''
      }
    ])
  })

  it('add new contact and return the result while modifying the original', () => {
    const result = addNewContact(contacts)

    expect(result).toEqual(2)
    expect(contacts).toStrictEqual([
      sampleContact,
      {
        locationNames: [{ value: '', text: '' }],
        contactType: { value: '', text: '' },
        firstName: '',
        lastName: '',
        phoneNumber: '',
        email: ''
      }
    ])
  })
})
