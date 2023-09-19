import { isRef } from 'vue'

export interface CodeDescrType {
  value: string
  text: string
}

export interface CodeNameType {
  code: string
  name: string
}

export interface BusinessSearchResult {
  code: string
  name: string
  status: string
  legalType: string
}

export interface ValidationMessageType {
  fieldId: string
  errorMsg: string
  originalValue?: string
}

export const isEmpty = (receivedValue: any): boolean => {
  const value = isRef(receivedValue) ? receivedValue.value : receivedValue
  return value === undefined || value === null || value === ''
}

export enum ClientTypeEnum {
  Unknow,
  R,
  U
}

export interface ProgressData {
  kind: string
  title: string
  subtitle: string
  enabled: boolean
}

export interface Submitter {
  name: string
  email: string
  provider: string
  userId: string | undefined
  firstName: string
  lastName: string
  businessName: string
}

export interface ModalNotification {
  message: string
  kind: string
  toastTitle: string
  active: boolean
  handler: () => void
}

export interface SessionProperties {
  user: Submitter | undefined
  logIn: (provider: string) => void
  logOut: () => void
  isLoggedIn: () => boolean
  loadDetails: () => Submitter | undefined
}

type ProgressNotificationKind = 'disabled' | 'error' | 'navigate'

export interface ProgressNotification {
  kind: ProgressNotificationKind
  value?: number | number[] | boolean
}

export interface SubmissionList {
  id: string
  name: string
  status: string
  updated: string
  user: string
  requestType: string
  clientType: string
}

export interface SubmissionDetails {
  submissionId: number
  submissionStatus: string
  submissionType: string
  submittedTimestamp: string
  updateTimestamp: string
  updateUser: string
  business: SubmissionDetailsBusiness
  contact: SubmissionDetailsContact[]
  address: SubmissionDetailsAddress[]
  matchers: SubmissionDetailsMatchers
}

export interface SubmissionDetailsBusiness {
  businessType: string
  incorporationNumber: string
  clientNumber: string
  organizationName: string
  clientType: string
  goodStanding: string
}

export interface SubmissionDetailsContact {
  index: number
  contactType: string
  firstName: string
  lastName: string
  phoneNumber: string
  emailAddress: string
  locations: string[]
  userId: string
}

export interface SubmissionDetailsAddress {
  index: number
  streetAddress: string
  country: string
  province: string
  city: string
  postalCode: string
  name: string
}

export interface SubmissionDetailsMatchers {
  goodStanding: string
  legalName: string
}
