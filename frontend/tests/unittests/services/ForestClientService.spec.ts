import { describe, it, expect, beforeEach, afterEach } from "vitest";
import {
  addNewAddress,
  addNewContact,
  getContactDescription,
  getAddressDescription,
  toTitleCase,
  getTagColorByClientStatus,
  goodStanding,
  formatPhoneNumber,
  getPrevailingRole,
  includesAnyOf,
  extractReasonFields,
  getAction,
  getActionLabel,
  getEnumKeyByEnumValue,
  getFormattedHtml,
  locationToCreateFormat,
  locationToEditFormat,
  keepOnlyNumbersAndLetters,
  toSentenceCase,
  contactToCreateFormat,
  contactToEditFormat,
  indexToLocationCode,
  formatLocation,
  preserveUnchangedData,
  formatAddress,
  compareAny,
  buildRelatedClientIndex,
  buildRelatedClientCombination,
} from "@/services/ForestClientService";
import type { Contact, Address } from "@/dto/ApplyClientNumberDto";
import type {
  UserRole,
  ClientDetails,
  ClientLocation,
  ClientContact,
  RelatedClientEntry,
  CodeNameType,
} from "@/dto/CommonTypesDto";
import type * as jsonpatch from "fast-json-patch";

describe("ForestClientService.ts", () => {
  describe("Address and Contact utils", () => {
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
      index: 0,
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
      index: 0,
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
          index: 1,
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
          index: 1,
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

    it("returns a string containing the address' index when it's an empty string", () => {
      const result = getAddressDescription({ locationName: "" } as Address, 7);
      expect(result).toEqual("Address #7");
    });

    it("returns a string containing the address' index when it's null", () => {
      const result = getAddressDescription({ locationName: null } as Address, 7);
      expect(result).toEqual("Address #7");
    });

    it("returns a string containing the provided entityName and the address' index", () => {
      const result = getAddressDescription({ locationName: "" } as Address, 7, "Fancy");
      expect(result).toEqual("Fancy #7");
    });
  });

  describe("toTitleCase", () => {
    it.each([
      ["TWO WORDS", "Two Words"],
      ["NAME (DOING)", "Name (Doing)"], // parentheses
      ["A.C.R.O.N.", "A.C.R.O.N."], // period
    ])("gets the expected result from input '%s': '%s'", (input, expectedOutput) => {
      expect(toTitleCase(input)).toEqual(expectedOutput);
    });

    it("returns empty string when input is undefined", () => {
      expect(toTitleCase(undefined as unknown as string)).toEqual("");
    });
  });

  describe("getTagColorByClientStatus", () => {
    it.each([
      ["Active", "green"],
      ["Deactivated", "purple"],
      ["Receivership", "magenta"],
      ["Suspended", "red"],
      ["Deceased", "gray"],
    ])("returns the correct color for status '%s': '%s'", (status, expectedColor) => {
      expect(getTagColorByClientStatus(status)).toEqual(expectedColor);
    });
  
    it.each([
      ["", ""],
      [undefined, ""],
      [null, ""],
      ["NonExistent", ""],
    ])("returns an empty string for invalid status '%s'", (status, expectedColor) => {
      expect(getTagColorByClientStatus(status)).toEqual(expectedColor);
    });
  });  

  describe("goodStanding", () => {
    const yExpected = "Good standing";
    const emptyExpected = "Unknown";
    describe("when input is 'Y'", () => {
      it("returns 'Good standing'", () => {
        expect(goodStanding("Y")).toEqual(yExpected);
      });
    });
    describe("when input is empty", () => {
      it.each([[""], [undefined], [null]])("returns 'Unknown' for %j", (input) => {
        expect(goodStanding(input)).toEqual(emptyExpected);
      });
    });
    describe("when input is 'N'", () => {
      it("returns something else", () => {
        const nValue = goodStanding("N");

        // is a non-empty string
        expect(nValue).toEqual(expect.any(String));
        expect(nValue.length).toBeGreaterThan(0);

        // doesn't overlap with "Y" or empty values
        expect(nValue).not.toEqual(yExpected);
        expect(nValue).not.toEqual(emptyExpected);
      });
    });
  });

  describe("formatPhoneNumber", () => {
    it("formats 10-digit phone numbers properly", () => {
      const result = formatPhoneNumber("1234567890");
      expect(result).toEqual("(123) 456-7890");
    });

    it("doesn't crash when phone numbers has more than 10 digits", () => {
      const result = formatPhoneNumber("1234567890123");
      expect(result).toEqual("(123) 456-7890123");
    });

    it("doesn't crash when phone numbers has less than 10 digits", () => {
      const result = formatPhoneNumber("12345678");
      expect(result).toEqual("(123) 456-78");
    });

    it("returns an empty string if phone number is undefined", () => {
      const result = formatPhoneNumber(undefined);
      expect(result).toEqual("");
    });
  });

  describe("getPrevailingRole", () => {
    it("returns CLIENT_ADMIN if included in the input", () => {
      const input: UserRole[] = [
        "CLIENT_VIEWER",
        "CLIENT_SUSPEND",
        "CLIENT_ADMIN",
        "CLIENT_EDITOR",
      ];
      const result = getPrevailingRole(input);
      expect(result).toBe("CLIENT_ADMIN");
    });

    it("returns CLIENT_SUSPEND if the input includes it and doesn't include CLIENT_ADMIN", () => {
      const input: UserRole[] = ["CLIENT_VIEWER", "CLIENT_SUSPEND", "CLIENT_EDITOR"];
      const result = getPrevailingRole(input);
      expect(result).toBe("CLIENT_SUSPEND");
    });

    it("returns CLIENT_EDITOR if the input includes it and doesn't include CLIENT_ADMIN or CLIENT_SUSPEND", () => {
      const input: UserRole[] = ["CLIENT_VIEWER", "CLIENT_EDITOR"];
      const result = getPrevailingRole(input);
      expect(result).toBe("CLIENT_EDITOR");
    });

    it("returns CLIENT_VIEWER if the input includes it and doesn't include higher options", () => {
      const input: UserRole[] = ["CLIENT_VIEWER"];
      const result = getPrevailingRole(input);
      expect(result).toBe("CLIENT_VIEWER");
    });

    it("returns null if the input is empty", () => {
      const input = [];
      const result = getPrevailingRole(input);
      expect(result).toBe(null);
    });

    it("returns null if the input includes none of the roles sought", () => {
      const input = ["ANYTHING"];
      const result = getPrevailingRole(input);
      expect(result).toBe(null);
    });
  });

  describe("includesAnyOf", () => {
    it("returns true when the haystack includes one of the needles", () => {
      const haystack = ["a", "b", "c", "d"];
      const needles = ["x", "c", "z"];
      const result = includesAnyOf(haystack, needles);
      expect(result).toBe(true);
    });

    it("returns true when the haystack includes more than one of the needles", () => {
      const haystack = ["a", "b", "c", "d"];
      const needles = ["x", "c", "z", "a"];
      const result = includesAnyOf(haystack, needles);
      expect(result).toBe(true);
    });

    it("returns false when the haystack includes none of the needles", () => {
      const haystack = ["a", "b", "c", "d"];
      const needles = ["x", "y", "z"];
      const result = includesAnyOf(haystack, needles);
      expect(result).toBe(false);
    });
  });

  describe("keepOnlyNumbersAndLetters", () => {
    it("strips off any characters that are not a number or a simple letter", () => {
      const result = keepOnlyNumbersAndLetters("A@b(1)2-C 3");
      expect(result).toEqual("Ab12C3");
    });
  });

  describe("locationToCreateFormat", () => {
    const location = {
      clientLocnName: "Mailing address",
      clientLocnCode: "00",
      addressOne: "886 Richmond Ave",
      addressTwo: "C/O Tony Pineda",
      addressThree: "Sample additional info",
      countryCode: "CA",
      countryDesc: "Canada",
      provinceCode: "SK",
      provinceDesc: "Saskatchewan",
      city: "Hampton",
      postalCode: "A1B2C3",
      emailAddress: "contact@mail.com",
      businessPhone: "2502863767",
      cellPhone: "2505553700",
      homePhone: "2505553101",
      faxNumber: "2502863768",
      cliLocnComment: "Sample location 00 comment",
      locnExpiredInd: "N",
    } as ClientLocation;

    it("converts from ClientLocation format to Address format properly", () => {
      const address = locationToCreateFormat(location);
      expect(address.streetAddress).toEqual(location.addressThree);
      expect(address.complementaryAddressOne).toEqual(location.addressOne);
      expect(address.complementaryAddressTwo).toEqual(location.addressTwo);
      expect(address.country).toStrictEqual({
        value: location.countryCode,
        text: location.countryDesc,
      });
      expect(address.province).toEqual({
        value: location.provinceCode,
        text: location.provinceDesc,
      });
      expect(address.city).toEqual(location.city);
      expect(address.postalCode).toEqual(location.postalCode);
      expect(address.businessPhoneNumber).toEqual(formatPhoneNumber(location.businessPhone));
      expect(address.secondaryPhoneNumber).toEqual(formatPhoneNumber(location.cellPhone));
      expect(address.tertiaryPhoneNumber).toEqual(formatPhoneNumber(location.homePhone));
      expect(address.faxNumber).toEqual(formatPhoneNumber(location.faxNumber));
      expect(address.emailAddress).toEqual(location.emailAddress);
      expect(address.notes).toEqual(location.cliLocnComment);
      expect(address.index).toEqual(Number(location.clientLocnCode));
      expect(address.locationName).toEqual(location.clientLocnName);
    });
  });

  describe("locationToEditFormat", () => {
    const address = {
      streetAddress: "2975 Jutland Rd",
      complementaryAddressOne: "C/O Mary Anne",
      complementaryAddressTwo: "Call Mary",
      country: {
        value: "US",
        text: "United States of America",
      },
      province: {
        value: "PA",
        text: "Pennsylvania",
      },
      city: "Bethlehem",
      postalCode: "12345-1234",
      businessPhoneNumber: "(121) 212-1212",
      secondaryPhoneNumber: "(343) 434-3434",
      tertiaryPhoneNumber: "(565) 656-5656",
      faxNumber: "(787) 878-7878",
      emailAddress: "john@company.com",
      notes: "Updated notes",

      // This is not supposed to be changed, but that's not this function's responsibility
      index: 1,

      locationName: "Updated mailing address",
    } as Address;

    const baseLocation: ClientLocation = {
      clientNumber: "121314",
      clientLocnName: "Mailing address",
      clientLocnCode: "00",
      addressOne: "123 Richmond Ave",
      addressTwo: "C/O Tony Pineda",
      addressThree: "Sample additional info",
      countryCode: "CA",
      countryDesc: "Canada",
      provinceCode: "SK",
      provinceDesc: "Saskatchewan",
      city: "Hampton",
      postalCode: "A1B2C3",
      emailAddress: "contact@mail.com",
      businessPhone: "2502863767",
      cellPhone: "2505553700",
      homePhone: "2505553101",
      faxNumber: "2502863768",
      cliLocnComment: "Sample location 00 comment",
      locnExpiredInd: "N",
      returnedMailDate: "2000-01-02",
      trustLocationInd: "Y",
      createdBy: "peter",
      updatedBy: "mike",
    };

    it("converts from Address format to ClientLocation format properly", () => {
      const location = locationToEditFormat(address, baseLocation);

      expect(location.addressOne).toEqual(address.complementaryAddressOne);
      expect(location.addressTwo).toEqual(address.complementaryAddressTwo);
      expect(location.addressThree).toEqual(address.streetAddress);
      expect(location.countryCode).toEqual(address.country.value);
      expect(location.countryDesc).toEqual(address.country.text);
      expect(location.provinceCode).toEqual(address.province.value);
      expect(location.provinceDesc).toEqual(address.province.text);
      expect(location.city).toEqual(address.city);
      expect(location.postalCode).toEqual(address.postalCode);
      expect(location.businessPhone).toEqual(
        keepOnlyNumbersAndLetters(address.businessPhoneNumber),
      );
      expect(location.cellPhone).toEqual(keepOnlyNumbersAndLetters(address.secondaryPhoneNumber));
      expect(location.homePhone).toEqual(keepOnlyNumbersAndLetters(address.tertiaryPhoneNumber));
      expect(location.faxNumber).toEqual(keepOnlyNumbersAndLetters(address.faxNumber));
      expect(location.emailAddress).toEqual(address.emailAddress);
      expect(location.cliLocnComment).toEqual(address.notes);
      expect(location.clientLocnCode).toEqual(String(address.index).padStart(2, "0"));
      expect(location.clientLocnName).toEqual(address.locationName);

      // Information that doesn't exist on the Address format
      const missingProperties: (keyof ClientLocation)[] = [
        "clientNumber",
        "locnExpiredInd",
        "returnedMailDate",
        "trustLocationInd",
        "createdBy",
        "updatedBy",
      ];

      for (const key in location) {
        if (!missingProperties.includes(key as keyof ClientLocation)) {
          /*
          This assertion makes sure data comes primarily from the input address instead of the
          baseLocation.
          */
          expect(location[key]).not.toEqual(baseLocation[key]);
        }
      }
      for (const propName of missingProperties) {
        /*
        While this one makes sure the remaining data is properly copied from the baseLocation.
        */
        expect(location[propName]).toEqual(baseLocation[propName]);
      }
    });
  });

  describe("contactToCreateFormat", () => {
    const clientContact = {
      contactId: 10,
      locationCodes: ["00"],
      contactName: "Cheryl Bibby",
      contactTypeCode: "BL",
      contactTypeDesc: "Billing",
      businessPhone: "2502863767",
      secondaryPhone: "2505553700",
      faxNumber: "2502863768",
      emailAddress: "cheryl@ktb.com",
    } as ClientContact;

    const allLocations = [
      {
        clientLocnCode: "00",
        clientLocnName: "Headquarters",
      },
      {
        clientLocnCode: "01",
        clientLocnName: "Town office",
      },
    ] as ClientLocation[];

    it("converts from ClientContact format to Contact format properly", () => {
      const contact = contactToCreateFormat(clientContact, allLocations);
      expect(contact.index).toEqual(clientContact.contactId);
      expect(contact.fullName).toEqual(clientContact.contactName);
      expect(contact.locationNames).toStrictEqual([
        {
          value: "00",
          text: "Headquarters",
        },
      ]);
      expect(contact.contactType).toEqual({
        value: clientContact.contactTypeCode,
        text: clientContact.contactTypeDesc,
      });
      expect(contact.phoneNumber).toEqual(formatPhoneNumber(clientContact.businessPhone));
      expect(contact.secondaryPhoneNumber).toEqual(formatPhoneNumber(clientContact.secondaryPhone));
      expect(contact.faxNumber).toEqual(formatPhoneNumber(clientContact.faxNumber));
      expect(contact.email).toEqual(clientContact.emailAddress);
    });
  });

  describe("contactToEditFormat", () => {
    const contact = {
      contactType: {
        value: "DI",
        text: "Director",
      },
      fullName: "Julia Smith",
      phoneNumber: "(121) 212-1212",
      secondaryPhoneNumber: "(343) 434-3434",
      faxNumber: "(787) 878-7878",
      email: "julia@company.com",
      index: 0,
      locationNames: [
        {
          value: "00",
          text: "Headquarters",
        },
        {
          value: "01",
          text: "Town office",
        },
      ],
    } as Contact;

    const baseContact: ClientContact = {
      clientNumber: "121314",
      contactId: 10,
      locationCodes: ["00"],
      contactName: "Cheryl Bibby",
      contactTypeCode: "BL",
      contactTypeDesc: "Billing",
      businessPhone: "2502863767",
      secondaryPhone: "2505553700",
      faxNumber: "2502863768",
      emailAddress: "cheryl@ktb.com"
    };

    it("converts from Contact format to ClientContact format properly", () => {
      const clientContact = contactToEditFormat(contact, baseContact);

      expect(clientContact.contactId).toEqual(contact.index);
      expect(clientContact.locationCodes).toEqual(["00", "01"]);
      expect(clientContact.contactName).toEqual(contact.fullName);
      expect(clientContact.contactTypeCode).toEqual(contact.contactType.value);
      expect(clientContact.contactTypeDesc).toEqual(contact.contactType.text);
      expect(clientContact.businessPhone).toEqual(keepOnlyNumbersAndLetters(contact.phoneNumber));
      expect(clientContact.secondaryPhone).toEqual(
        keepOnlyNumbersAndLetters(contact.secondaryPhoneNumber),
      );
      expect(clientContact.faxNumber).toEqual(keepOnlyNumbersAndLetters(contact.faxNumber));
      expect(clientContact.emailAddress).toEqual(contact.email);

      // Information that doesn't exist on the Contact format
      const missingProperties: (keyof ClientContact)[] = ["clientNumber"];

      for (const key in clientContact) {
        if (!missingProperties.includes(key as keyof ClientContact)) {
          /*
          This assertion makes sure data comes primarily from the input contact instead of the
          baseContact.
          */
          expect(clientContact[key]).not.toEqual(baseContact[key]);
        }
      }
      for (const propName of missingProperties) {
        /*
        While this one makes sure the remaining data is properly copied from the baseContact.
        */
        expect(clientContact[propName]).toEqual(baseContact[propName]);
      }
    });
  });
});

describe("getEnumKeyByEnumValue", () => {
  const sampleEnum = {
    ACTIVE: "ACT",
    DEACTIVATED: "DEC",
    SUSPENDED: "SPN",
  };

  it("returns the correct key for a known value", () => {
    const result = getEnumKeyByEnumValue(sampleEnum, "ACT");
    expect(result).toEqual("ACTIVE");

    const result2 = getEnumKeyByEnumValue(sampleEnum, "DEC");
    expect(result2).toEqual("DEACTIVATED");
  });

  it("returns 'Unknown' for an unknown value", () => {
    const result = getEnumKeyByEnumValue(sampleEnum, "UNKNOWN");
    expect(result).toEqual("Unknown");
  });

  it("returns 'Unknown' when passed a null or undefined value", () => {
    const result = getEnumKeyByEnumValue(sampleEnum, null);
    expect(result).toEqual("Unknown");

    const result2 = getEnumKeyByEnumValue(sampleEnum, undefined);
    expect(result2).toEqual("Unknown");
  });
});

describe("getFormattedHtml", () => {
  it("correctly formats text with newline characters", () => {
    const input = "Hello\nWorld";
    const expectedOutput = "Hello<br>World";
    expect(getFormattedHtml(input)).toEqual(expectedOutput);
  });

  it("returns an empty string if input is empty", () => {
    const input = "";
    const expectedOutput = "";
    expect(getFormattedHtml(input)).toEqual(expectedOutput);
  });

  it("returns an empty string if input is null", () => {
    const input = null;
    const expectedOutput = "";
    expect(getFormattedHtml(input)).toEqual(expectedOutput);
  });

  it("returns an empty string if input is undefined", () => {
    const input = undefined;
    const expectedOutput = "";
    expect(getFormattedHtml(input)).toEqual(expectedOutput);
  });

  it("doesn't alter the input if there are no newline characters", () => {
    const input = "No newlines here";
    const expectedOutput = "No newlines here";
    expect(getFormattedHtml(input)).toEqual(expectedOutput);
  });
});

describe("toSentenceCase", () => {
  it.each([
    ["hello", "Hello"],        // Normal lowercase word
    ["HELLO", "Hello"],        // All uppercase
    ["hELLo", "Hello"],        // Mixed case
    ["hello world", "Hello world"], // Sentence case for multiple words
    ["123abc", "123abc"],      // Starts with a number
    ["", ""],                  // Empty string
  ])("converts '%s' to '%s'", (input, expectedOutput) => {
    expect(toSentenceCase(input)).toEqual(expectedOutput);
  });

  it("returns an empty string for undefined input", () => {
    expect(toSentenceCase(undefined as unknown as string)).toEqual("");
  });

  it("returns an empty string for null input", () => {
    expect(toSentenceCase(null as unknown as string)).toEqual("");
  });

  it("handles special characters correctly", () => {
    expect(toSentenceCase("!hello")).toEqual("!hello");
    expect(toSentenceCase("@TEST")).toEqual("@test");
  });
});

describe("Reason Fields Handling", () => {
  const originalData: ClientDetails = {
    client: {
      clientNumber: "12345",
      clientName: "Doe",
      legalFirstName: "John",
      legalMiddleName: "A.",
      clientStatusCode: "ACT",
      clientStatusDesc: "",
      clientTypeCode: "I",
      clientTypeDesc: "Individual",
      clientIdTypeCode: "",
      clientIdTypeDesc: "",
      clientIdentification: "12345",
      registryCompanyTypeCode: "",
      corpRegnNmbr: "",
      clientAcronym: "",
      wcbFirmNumber: "",
      ocgSupplierNmbr: "",
      clientComment: "",
      clientCommentUpdateDate: "",
      clientCommentUpdateUser: "",
      goodStandingInd: "",
      birthdate: "",
    },
    doingBusinessAs: "",
    addresses: [
      {
        clientNumber: "12345",
        clientLocnCode: "1",
        clientLocnName: "",
        addressOne: "123 Main St",
        addressTwo: "Apt 4",
        addressThree: "",
        city: "Metropolis",
        provinceCode: "BC",
        provinceDesc: "",
        postalCode: "V1V1V1",
        countryCode: "CA",
        countryDesc: "",
        businessPhone: "",
        homePhone: "",
        cellPhone: "",
        faxNumber: "",
        emailAddress: "",
        locnExpiredInd: "",
        returnedMailDate: "",
        trustLocationInd: "",
        cliLocnComment: "",
        createdBy: "",
        updatedBy: "",
      },
    ],
    contacts: [],
    reasons: [],
  };

  it("extracts reason fields from patch data", () => {
    const patchData: jsonpatch.Operation[] = [
      { op: "replace", path: "/client/legalFirstName", value: "Johan" },
      { op: "replace", path: "/client/clientIdentification", value: "67890" },
      { op: "replace", path: "/client/clientStatusCode", value: "DAC" },
      { op: "replace", path: "/addresses/1/city", value: "Gotham" },
    ];

    const result = extractReasonFields(patchData, originalData);
    expect(result).toEqual([
      { field: "/client/name", action: "NAME" },
      { field: "/client/id", action: "ID" },
      { field: "/client/clientStatusCode", action: "DAC" },
      { field: "/addresses/1", action: "ADDR" }, // an address group
    ]);
  });

  it("returns empty array if no reason-required fields are changed", () => {
    const patchData: jsonpatch.Operation[] = [
      { op: "replace", path: "/someOtherField", value: "New Value" },
    ];

    const result = extractReasonFields(patchData, originalData);
    expect(result).toEqual([]);
  });

  it("handles unknown status transition with empty action", () => {
    const patchData: jsonpatch.Operation[] = [
      { op: "replace", path: "/client/clientStatusCode", value: "XYZ" },
    ];
  
    const result = extractReasonFields(patchData, {
      ...originalData,
      client: {
        ...originalData.client,
        clientStatusCode: "ABC",
      },
    });
  
    expect(result).toEqual([]);
  });

  describe("getAction", () => {
    it("returns correct action for client status changes", () => {
      expect(getAction("/clientStatusCode", "ACT", "DAC")).toEqual("DAC");
      expect(getAction("/clientStatusCode", "DEC", "ACT")).toEqual("RACT");
      expect(getAction("/clientStatusCode", "SPN", "REC")).toBeNull(); // No reason needed
    });

    it("returns correct field mapping for other fields", () => {
      expect(getAction("/clientIdentification")).toEqual(["ID"]);
      expect(getAction("/city")).toEqual(["ADDR"]);
      expect(getAction("/unknownField")).toBeNull();
    });
  });

  describe("getActionLabel", () => {
    it("returns correct label for known actions", () => {
      expect(getActionLabel("RACT")).toEqual('Reason for "reactivated" status');
      expect(getActionLabel("DAC")).toEqual('Reason for "deactivated" status');
      expect(getActionLabel("ADDR")).toEqual("Reason for address change");
    });

    it("returns 'Unknown' for an unrecognized action", () => {
      expect(getActionLabel("UNKNOWN_ACTION")).toEqual("Unknown");
    });
  });
});

describe("indexToLocationCode", () => {
  it("pads with zeros to the left", () => {
    expect(indexToLocationCode(1)).toEqual("01");
  });
  it("returns null when parameter is null", () => {
    expect(indexToLocationCode(null)).toEqual(null);
  });
});

describe("formatLocation", () => {
  it("returns code and name separated by a dash", () => {
    expect(formatLocation("00", "Name")).toEqual("00 - Name");
  });

  it("returns only the code when the name is empty", () => {
    expect(formatLocation("00", "")).toEqual("00");
  });

  it("returns only the name when the code is empty", () => {
    expect(formatLocation("", "Name")).toEqual("Name");
  });
});

describe("preserveUnchangedData", () => {
  describe("when data was null", () => {
    const original = {
      a: "1",
      b: null,
      c: "3",
    };
    it("replaces empty string with null", () => {
      const data = JSON.parse(JSON.stringify(original));
      data.b = "";
      const result = preserveUnchangedData(data, original);
      expect(result.b).toEqual(null);
    });
  });

  describe("when data was an empty string", () => {
    const original = {
      a: "1",
      b: "",
      c: "3",
    };
    it("replaces null with empty string", () => {
      const data = JSON.parse(JSON.stringify(original));
      data.b = null;
      const result = preserveUnchangedData(data, original);
      expect(result.b).toEqual("");
    });
  });

  describe("when data was not null nor an empty string", () => {
    const original = {
      a: "1",
      b: "2",
      c: "3",
    };
    it("replaces empty string with null", () => {
      const data = JSON.parse(JSON.stringify(original));
      data.b = "";
      const result = preserveUnchangedData(data, original);
      expect(result.b).toEqual(null);
    });
  });

  describe('when a key (`b`) is "empty" in the original data', () => {
    describe("and the current data contains an object under that key, which in turn has some key with a null value", () => {
      describe.each([
        ["missing", {}],
        ["null", { b: null }],
        ["an empty string", { b: "" }],
      ])("(the key is %s in the original data)", (_, mergeParam) => {
        const original = {
          a: "1",
          c: "3",
          ...mergeParam,
        };
        const data = JSON.parse(JSON.stringify(original));
        data.b = { nested: null };
        it("doesn't change the value", () => {
          const result = preserveUnchangedData(data, original);
          expect(result.b).toEqual({ nested: null });
          expect(result).toEqual(data);
        });
      });
    });
  });
});

describe("formatAddress", () => {
  const location: ClientLocation = {
    addressOne: "123 Main St",
    addressTwo: null,
    addressThree: null,
    city: "Metropolis",
    provinceCode: "BC",
    provinceDesc: "British Columbia",
    countryCode: "CA",
    countryDesc: "Canada",
    postalCode: "V1V1V1",
  } as ClientLocation;

  it("returns a formatted address string", () => {
    const result = formatAddress(location);
    expect(result).toEqual("123 Main St, Metropolis, BC, Canada, V1V1V1");
  });

  it("uses addressTwo as the street address when it's the last non-empty address part", () => {
    const location: ClientLocation = {
      addressOne: "Skipped",
      addressTwo: "123 Main St",
      addressThree: null,
      city: "Metropolis",
      provinceCode: "BC",
      provinceDesc: "British Columbia",
      countryCode: "CA",
      countryDesc: "Canada",
      postalCode: "V1V1V1",
    } as ClientLocation;
    const result = formatAddress(location);
    expect(result).toEqual("123 Main St, Metropolis, BC, Canada, V1V1V1");
    expect(result).not.toContain("Skipped");
  });

  it("uses addressThree as the street address when it's non-empty", () => {
    const location: ClientLocation = {
      addressOne: "Skipped",
      addressTwo: "Skipped",
      addressThree: "123 Main St",
      city: "Metropolis",
      provinceCode: "BC",
      provinceDesc: "British Columbia",
      countryCode: "CA",
      countryDesc: "Canada",
      postalCode: "V1V1V1",
    } as ClientLocation;
    const result = formatAddress(location);
    expect(result).toEqual("123 Main St, Metropolis, BC, Canada, V1V1V1");
    expect(result).not.toContain("Skipped");
  });
});

describe("compareAny", () => {
  it.each([
    [25, 25], // number
    ["bird", "bird"], // string
    [true, true], // boolean
  ])("returns 0 when values are equal (%s and %s)", (a, b) => {
    expect(compareAny(a, b)).toEqual(0);
  });

  it.each([
    [25, 30], // number
    ["bird", "house"], // string
    [false, true], // boolean
  ])("returns -1 when the first value is less than the second one (%s and %s)", (a, b) => {
    expect(compareAny(a, b)).toEqual(-1);
  });

  it.each([
    [25, 20], // number
    ["bird", "air"], // string
    [true, false], // boolean
  ])("returns 1 when the first value is greater than the second one (%s and %s)", (a, b) => {
    expect(compareAny(a, b)).toEqual(1);
  });
});

describe("buildRelatedClientIndex", () => {
  it("returns a string that includes both the location code and the index", () => {
    const locationCode = "01";
    const relationshipIndex = 5;
    const result = buildRelatedClientIndex(locationCode, relationshipIndex);
    expect(result).toEqual("01,5");
  });
});

describe("buildRelatedClientCombination", () => {
  it("returns a string that combines the client code, the location code, the relationship code, the related client code and its location code, in that order", () => {
    const entry: RelatedClientEntry = {
      client: {
        client: { code: "1" } as CodeNameType,
        location: { code: "2" } as CodeNameType,
      },
      relationship: { code: "3" } as CodeNameType,
      relatedClient: {
        client: { code: "4" } as CodeNameType,
        location: { code: "5" } as CodeNameType,
      },
      percentageOwnership: null,
      hasSigningAuthority: null,
      isMainParticipant: true,
    };
    const result = buildRelatedClientCombination(entry);
    expect(result).toEqual("1,2,3,4,5");
  });
});
