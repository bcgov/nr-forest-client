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
} from "@/services/ForestClientService";
import type { Contact, Address } from "@/dto/ApplyClientNumberDto";
import type { UserRole } from "@/dto/CommonTypesDto";

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
      ["Active", "green"], // existing value #1
      ["Deactivated", "purple"], // existing value #2
      ["Bogus", ""], // non-existent value
    ])("gets the expected result from input '%s': '%s'", (input, expectedOutput) => {
      expect(getTagColorByClientStatus(input)).toEqual(expectedOutput);
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
