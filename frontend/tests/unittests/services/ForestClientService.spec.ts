import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { ref } from 'vue';
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
  getOldValue,
  getEnumKeyByEnumValue,
  getFormattedHtml,
  toSentenceCase,
} from "@/services/ForestClientService";
import type { Contact, Address } from "@/dto/ApplyClientNumberDto";
import type { UserRole, ClientDetails } from "@/dto/CommonTypesDto";
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

    it("returns a string containing the address' index", () => {
      const result = getAddressDescription({ locationName: "" } as Address, 7);
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
    clientNumber: "12345",
    clientName: "John Doe",
    legalFirstName: "John",
    legalMiddleName: "A.",
    clientStatusCode: "ACT",
    clientStatusDesc: "",
    clientTypeCode: "",
    clientTypeDesc: "",
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
    doingBusinessAs: [],
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
      { op: "replace", path: "/clientIdentification", value: "67890" },
      { op: "replace", path: "/clientStatusCode", value: "DEC" },
      { op: "replace", path: "/city", value: "Gotham" },
    ];

    const result = extractReasonFields(patchData, originalData);
    expect(result).toEqual([
      { field: "clientIdentification", action: "ID" },
      { field: "clientStatusCode", action: "ACDC" }, // Active -> Deceased
      { field: "city", action: "ADDR" },
    ]);
  });

  it("returns empty array if no reason-required fields are changed", () => {
    const patchData: jsonpatch.Operation[] = [
      { op: "replace", path: "/someOtherField", value: "New Value" },
    ];

    const result = extractReasonFields(patchData, originalData);
    expect(result).toEqual([]);
  });

  describe("getAction", () => {
    it("returns correct action for client status changes", () => {
      expect(getAction("/clientStatusCode", "ACT", "DAC")).toEqual("DAC");
      expect(getAction("/clientStatusCode", "DEC", "ACT")).toEqual("RACT");
      expect(getAction("/clientStatusCode", "SPN", "REC")).toBeNull(); // No reason needed
    });

    it("returns correct field mapping for other fields", () => {
      expect(getAction("/clientIdentification")).toEqual("ID");
      expect(getAction("/city")).toEqual("ADDR");
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

  describe("getOldValue", () => {
    it("returns old value from client details", () => {
      expect(getOldValue("/clientName", originalData)).toEqual("John Doe");
      expect(getOldValue("/provinceCode", originalData)).toEqual("BC");
    });

    it("returns 'N/A' for non-existent fields", () => {
      expect(getOldValue("/nonExistentField", originalData)).toEqual("N/A");
    });

    it("handles Vue ref correctly", () => {
      const clientRef = ref(originalData);
      expect(getOldValue("/clientName", clientRef)).toEqual("John Doe");
    });

    it("returns 'N/A' and logs a warning when data is undefined", () => {
      const consoleWarnSpy = vi.spyOn(console, "warn").mockImplementation(() => {});
    
      expect(getOldValue("/clientName", undefined as unknown as ClientDetails)).toEqual("N/A");
      expect(consoleWarnSpy).toHaveBeenCalledWith("Old value was called with undefined data!", "/clientName");
    
      consoleWarnSpy.mockRestore();
    });
    
  });

});
