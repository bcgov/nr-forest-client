import { isRef } from "vue";
import * as jsonpatch from "fast-json-patch";
import type { Address } from "./ApplyClientNumberDto";

export interface CodeDescrType {
  value: string;
  text: string;
};

export interface CodeNameType {
  code: string;
  name: string;
};

export interface CodeNameValue<T> extends CodeNameType {
  value: T;
};

export interface BusinessSearchResult {
  code: string;
  name: string;
  status: string;
  legalType: string;
};

export interface ValidationMessageType {
  fieldId: string;
  fieldName: string;
  errorMsg: string;
  originalValue?: string;
  warning?: boolean;
  custom?: Record<string, any>;
};

export interface FuzzyMatchResult {
  field: string;
  match: string;
  fuzzy: boolean;
  partialMatch: boolean;
};

export interface MiscFuzzyMatchResult {
  label?: string;
  result: FuzzyMatchResult;
};

export interface FuzzyMatcherData {
  show: boolean;
  fuzzy: boolean;
  matches: MiscFuzzyMatchResult[];
};

export interface FuzzyMatcherEvent {
  id: string;
  matches: FuzzyMatchResult[];
};

export const isEmpty = (receivedValue: any): boolean => {
  const value = isRef(receivedValue) ? receivedValue.value : receivedValue;
  return value === undefined || value === null || value === "";
};

export enum BusinessTypeEnum {
  Unknow,
  R,
  U,
};

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
  ULC,
};

export enum ClientTypeEnum {
  C,
  S,
  A,
  I,
  P,
  L,
  RSP,
  USP,
  B,
  T,
  G,
  F,
  U,
};

export enum IdentificationTypeEnum {
  BRTH,
  CDDL,
  PASS,
  CITZ,
  FNID,
  USDL,
  OTHR,
};

export interface IdentificationCodeNameType {
  code: string;
  name: string;
  countryCode: string;
};

export interface IdentificationCodeDescrType {
  value: string;
  text: string;
  countryCode: string;
};

export interface ProgressData {
  kind: string;
  title: string;
  subtitle: string;
  enabled: boolean;
};

export interface Submitter {
  name: string;
  email: string;
  provider: string;
  userId: string | undefined;
  businessId: string | undefined;
  firstName: string;
  lastName: string;
  businessName: string;
  birthdate: string;
  address: Address;
};

export interface ModalNotification {
  name?: string;
  message: string;
  kind: string;
  toastTitle: string;
  active: boolean;
  handler: () => void;
};

export interface SessionProperties {
  user: Submitter | undefined;
  token: string | undefined;
  authorities: string[];
  logIn: (provider: string) => void;
  logOut: () => void;
  isLoggedIn: () => boolean;
  loadDetails: () => Submitter | undefined;
};

type ProgressNotificationKind = "disabled" | "error" | "navigate";

export interface ProgressNotification {
  kind: ProgressNotificationKind;
  value?: number | number[] | boolean;
};

export interface SubmissionList {
  id: string;
  name: string;
  status: string;
  submittedAt: string;
  user: string;
  requestType: string;
  clientType: string;
  district: string;
};

export interface ClientSearchResult {
  clientNumber: string;
  clientAcronym: string;
  clientFullName: string;
  clientType: string;
  city: string;
  clientStatus: string;
};

export interface SubmissionDetails {
  submissionId: number;
  submissionStatus: string;
  submissionType: string;
  submittedTimestamp: Date;
  updateTimestamp: Date;
  approvedTimestamp: Date;
  updateUser: string;
  business: SubmissionDetailsBusiness;
  contact: SubmissionDetailsContact[];
  address: SubmissionDetailsAddress[];
  matchers: SubmissionDetailsMatchers;
  rejectionReason: string;
  confirmedMatchUserId: string;
};

export interface SubmissionDetailsBusiness {
  businessType: string;
  registrationNumber: string;
  clientNumber: string;
  organizationName: string;
  clientType: string;
  clientTypeDesc: string;
  goodStandingInd: string;
  birthdate: string;
  district: string;
  districtDesc: string;
};

export interface SubmissionDetailsContact {
  index: number;
  contactType: string;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  emailAddress: string;
  locations: string[];
  userId: string;
};

export interface SubmissionDetailsAddress {
  index: number;
  streetAddress: string;
  country: string;
  province: string;
  city: string;
  postalCode: string;
  name: string;
};

export interface SubmissionDetailsMatchers {
  goodStanding: string;
  corporationName: string;
  registrationNumber: string;
  contact: string;
  location: string;
};

export interface ClientLocation {
  clientNumber: string;
  clientLocnCode: string;
  clientLocnName: string;
  addressOne: string;
  addressTwo: string;
  addressThree: string;
  city: string;
  provinceCode: string;
  provinceDesc: string;
  postalCode: string;
  countryCode: string;
  countryDesc: string;
  businessPhone: string;
  homePhone: string;
  cellPhone: string;
  faxNumber: string;
  emailAddress: string;
  locnExpiredInd: string;
  returnedMailDate: string;
  trustLocationInd: string;
  cliLocnComment: string;
  createdBy: string;
  updatedBy: string;
};

export const createClientLocation = (
  clientNumber: string,
  clientLocnCode: string,
): ClientLocation => {
  const location = {
    clientNumber,
    clientLocnCode,
    clientLocnName: "",
    addressOne: "",
    addressTwo: "",
    addressThree: null,
    city: "",
    provinceCode: "BC",
    provinceDesc: "British Columbia",
    postalCode: "",
    countryCode: "CA",
    countryDesc: "Canada",
    businessPhone: "",
    homePhone: "",
    cellPhone: "",
    faxNumber: "",
    emailAddress: "",
    locnExpiredInd: "N",
    returnedMailDate: undefined,
    trustLocationInd: undefined,
    cliLocnComment: "",
    createdBy: undefined,
    updatedBy: undefined,
  };
  return location;
};

export interface ClientContact {
  clientNumber: string;
  contactId: number;
  contactName: string;
  contactTypeCode: string;
  contactTypeDesc: string;
  businessPhone: string;
  secondaryPhone: string;
  faxNumber: string;
  emailAddress: string;
  locationCodes: string[];
};

export const createClientContact = (contactId: number, clientNumber: string): ClientContact => {
  const contact = {
    contactId,
    clientNumber,
    locationCodes: [],
    contactName: "",
    contactTypeCode: "",
    contactTypeDesc: "",
    businessPhone: "",
    secondaryPhone: "",
    faxNumber: "",
    emailAddress: "",
    createdBy: undefined,
    updatedBy: undefined,
  };
  return contact;
};

export interface FieldReason {
  field: string;
  reason: string;
};

export interface FieldAction {
  field: string;
  action: string;
};

export interface RelatedClientDto {
  client: CodeNameType;
  location: CodeNameType;
}

export interface RelatedClientEntry {
  relationship: CodeNameType;
  percentageOwnership: number;
  hasSigningAuthority: boolean | null;
  isMainParticipant: boolean;
  client: RelatedClientDto;
  relatedClient: RelatedClientDto;
}

export type RelatedClientList = Record<string, RelatedClientEntry[]>;

export interface ClientInformation {
  clientNumber: string;
  clientName: string;
  legalFirstName: string;
  legalMiddleName: string;
  clientStatusCode: string;
  clientStatusDesc: string;
  clientTypeCode: string;
  clientTypeDesc: string;
  clientIdTypeCode: string;
  clientIdTypeDesc: string;
  clientIdentification: string;
  registryCompanyTypeCode: string;
  corpRegnNmbr: string;
  clientAcronym: string;
  wcbFirmNumber: string;
  ocgSupplierNmbr: string;
  clientComment: string;
  clientCommentUpdateDate: Date;
  clientCommentUpdateUser: string;
  goodStandingInd: string;
  birthdate: string;
};

export interface ClientDetails {
  client: ClientInformation;
  doingBusinessAs: string;
  addresses: ClientLocation[];
  contacts: ClientContact[];
  reasons: FieldReason[];
  relatedClients: RelatedClientList;
};

const userRoles = ["CLIENT_ADMIN", "CLIENT_SUSPEND", "CLIENT_EDITOR", "CLIENT_VIEWER"] as const;
export type UserRole = (typeof userRoles)[number];

export interface ActionWords {
  infinitive: string;
  pastParticiple: string;
};

type OperationType = "update" | "insert" | "delete";

export interface SaveEvent<T> {
  patch: jsonpatch.Operation[] | null;
  updatedData: T;
  action: ActionWords;
  operationType: OperationType;
};

export interface HistoryLogDetail {
  columnName: string;
  oldValue: string;
  newValue: string;
};

export interface HistoryLogReason {
  actionCode: string;
  reason: string;
};

export interface HistoryLogResult {
  tableName: string;
  idx: string;
  identifierLabel: string;
  updateTimestamp: Date;
  updateUserid: string;
  changeType: string;
  details: HistoryLogDetail[];
  reasons: HistoryLogReason[];
}
