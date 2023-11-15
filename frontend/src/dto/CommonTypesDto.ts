import { isRef } from "vue";
import type { Address } from "./ApplyClientNumberDto";

export interface CodeDescrType {
  value: string;
  text: string;
}

export interface CodeNameType {
  code: string;
  name: string;
}

export interface BusinessSearchResult {
  code: string;
  name: string;
  status: string;
  legalType: string;
}

export interface ValidationMessageType {
  fieldId: string;
  errorMsg: string;
  originalValue?: string;
}

export const isEmpty = (receivedValue: any): boolean => {
  const value = isRef(receivedValue) ? receivedValue.value : receivedValue;
  return value === undefined || value === null || value === "";
};

export enum BusinessTypeEnum {
  Unknow,
  R,
  U,
}

export enum LegalTypeEnum {
  A,
  B,
  BC,
  C,
  CP,
  EPR,
  FOR,
  LIC,
  REG,
  S,
  XS,
  XCP,
  SP,
  GP,
  LP,
  XL,
  XP,
}

export enum ClientTypeEnum {
  C,
  S,
  A,
  I,
  P,
  L,
  RSP,
  USP,
}

export interface ProgressData {
  kind: string;
  title: string;
  subtitle: string;
  enabled: boolean;
}

export interface Submitter {
  name: string;
  email: string;
  provider: string;
  userId: string | undefined;
  firstName: string;
  lastName: string;
  businessName: string;
  birthdate: string;
  address: Address;
}

export interface ModalNotification {
  message: string;
  kind: string;
  toastTitle: string;
  active: boolean;
  handler: () => void;
}

export interface SessionProperties {
  user: Submitter | undefined;
  logIn: (provider: string) => void;
  logOut: () => void;
  isLoggedIn: () => boolean;
  loadDetails: () => Submitter | undefined;
}

type ProgressNotificationKind = "disabled" | "error" | "navigate";

export interface ProgressNotification {
  kind: ProgressNotificationKind;
  value?: number | number[] | boolean;
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
  submittedTimestamp: Date
  updateTimestamp: Date
  approvedTimestamp: Date
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
