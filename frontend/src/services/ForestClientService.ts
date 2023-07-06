import type { Address, Contact } from '../dto/ApplyClientNumberDto'
import type { CodeDescrType } from '@/core/CommonTypesDto'

export const addNewAddress = (addresses: Address[]): number => {
  const blankAddress: Address = {
    locationName: '',
    streetAddress: '',
    country: { value: '', text: '' } as CodeDescrType,
    province: { value: '', text: '' } as CodeDescrType,
    city: '',
    postalCode: ''
  }

  const newAddresses = addresses.push(blankAddress)
  return newAddresses
}

export const addNewContact = (contacts: Contact[]): number => {
  const blankContact: Contact = {
    locationNames: [{ value: '', text: '' }] as CodeDescrType[],
    contactType: { value: '', text: '' } as CodeDescrType,
    firstName: '',
    lastName: '',
    phoneNumber: '',
    email: ''
  }

  const newContacts = contacts.push(blankContact)
  return newContacts
}
