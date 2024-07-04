import { describe, it, expect, beforeEach, afterEach } from "vitest";
import {
  addNewAddress,
  addNewContact,
  getContactDescription,
  getAddressDescription,
} from "@/services/ForestClientService";
import type { Contact, Address } from "@/dto/ApplyClientNumberDto";

describe("ForestClientService.ts", () => {
  // create an array of Address and an array of Contact to be used during test and set some random values inside beforeEach and clean it up before the next test

  let addresses: Address[];
  let contacts: Contact[];

  const sampleAddress = {
    streetAddress: "2975 Jutland Rd",
    country: {
      value: "CA",
      text: "Canada",
    },
    province: {
      value: "BC",
      text: "British Columbia",
    },
    city: "Victoria",
    postalCode: "V8T5J9",
    locationName: "Mailing address",
  };
  const sampleContact = {
    contactType: {
      value: "P",
      text: "Person",
    },
    firstName: "Jhon",
    lastName: "Wick",
    phoneNumber: "(111) 222-3344",
    email: "babayaga@theguild.ca",
    locationNames: [
      {
        value: "0",
        text: "Mailing address",
      },
    ],
  };

  beforeEach(() => {
    addresses = [sampleAddress];
    contacts = [sampleContact];
  });

  afterEach(() => {
    addresses = [];
    contacts = [];
  });

  it("add new address and return the result while modifying the original", () => {
    const result = addNewAddress(addresses);

    expect(result).toEqual(2);
    expect(addresses).toStrictEqual([
      sampleAddress,
      {
        locationName: "",
        streetAddress: "",
        country: { value: "", text: "" },
        province: { value: "", text: "" },
        city: "",
        postalCode: "",
      },
    ]);
  });

  it("add new contact and return the result while modifying the original", () => {
    const result = addNewContact(contacts);

    expect(result).toEqual(2);
    expect(contacts).toStrictEqual([
      sampleContact,
      {
        locationNames: [{ value: "", text: "" }],
        contactType: { value: "", text: "" },
        firstName: "",
        lastName: "",
        phoneNumber: "",
        email: "",
      },
    ]);
  });

  it("returns a string containing the contact's first name and last name", () => {
    const result = getContactDescription({ firstName: "John", lastName: "Wick" } as Contact, 7);
    expect(result).toEqual("John Wick");
  });

  it("returns a string containing the contact's index", () => {
    const result = getContactDescription({ firstName: "", lastName: "" } as Contact, 7);
    expect(result).toEqual("Contact #7");
  });

  it("returns a string containing the address' locationName", () => {
    const result = getAddressDescription({ locationName: "Nice Place" } as Address, 7);
    expect(result).toEqual("Nice Place");
  });

  it("returns a string containing the address' index", () => {
    const result = getAddressDescription({ locationName: "" } as Address, 7);
    expect(result).toEqual("Address #7");
  });

  it("returns a string containing the provided entityName and the address' index", () => {
    const result = getAddressDescription({ locationName: "" } as Address, 7, "Fancy");
    expect(result).toEqual("Fancy #7");
  });
});
