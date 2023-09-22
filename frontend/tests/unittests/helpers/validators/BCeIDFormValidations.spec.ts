import { describe, it, expect } from "vitest";

import {
  validate,
  addValidation,
  runValidation,
} from "@/helpers/validators/BCeIDFormValidations";

import {
  getFieldValue,
  getValidations,
} from "@/helpers/validators/GlobalValidators";

import type { ValidationMessageType } from "@/dto/CommonTypesDto";
import { useEventBus } from "@vueuse/core";

describe("BCeIDFormValidations.ts", () => {
  const formDataDto = {
    businessInformation: {
      businessType: "JJ",
      legalType: "",
      clientType: "",
      incorporationNumber: "",
      businessName: "The Test Client Corp",
      goodStandingInd: "",
    },
    location: {
      addresses: [
        {
          locationName: "Mailing address",
          streetAddress: "",
          country: { value: "CA", text: "Canada" },
          province: { value: "", text: "" },
          city: "",
          postalCode: "A1A1A1",
        },
      ],
      contacts: [
        {
          locationNames: [{ text: "Mailing address", value: "0" }],
        },
      ],
    },
  };

  const isGreaterThanZero = (value: number) => (value > 0 ? "" : "invalid");
  it("should get a list of functions for a field", () => {
    addValidation("test", () => "");
    expect(getValidations("test")).toEqual([expect.any(Function)]);
  });
  it("should return an empty array for invalid fields", () => {
    expect(getValidations("test2")).toEqual([]);
  });
  it("should return the value of a simple field", () => {
    expect(
      getFieldValue("businessInformation.businessType", formDataDto)
    ).toEqual("JJ");
  });
  it("should return the value of a nested field", () => {
    expect(
      getFieldValue("location.addresses.*.country.text", formDataDto)
    ).toEqual(["Canada"]);
  });
  it("should return the value of a nested field with a condition", () => {
    expect(
      getFieldValue(
        'location.addresses.*.postalCode(location.addresses.*.country.text === "CA")',
        formDataDto
      )
    ).toEqual(["A1A1A1"]);
  });
  it("should return the value of a nested field with a condition", () => {
    expect(
      getFieldValue(
        'location.addresses.*.postalCode(location.addresses.*.country.text === "US")',
        formDataDto
      )
    ).toEqual(["A1A1A1"]);
  });
  it("should return the value of a nested field within a nested field", () => {
    expect(
      getFieldValue("location.contacts.*.locationNames.*.text", formDataDto)
    ).toEqual([["Mailing address"]]);
  });
  it("should return true for a valid businessName", () => {
    expect(
      validate(["businessInformation.businessName"], formDataDto)
    ).toBeTruthy();
  });
  it("should return true for nested fields", () => {
    expect(
      validate(["location.addresses.*.locationName"], formDataDto)
    ).toBeTruthy();
  });
  it("should return true for nested fields with conditions", () => {
    expect(
      validate(
        [
          'location.addresses.*.postalCode(location.addresses.*.country.text === "CA")',
        ],
        formDataDto
      )
    ).toBeTruthy();
  });
  it("should return true for nested fields withing nested fields", () => {
    expect(
      validate(["location.contacts.*.locationNames.*.text"], formDataDto)
    ).toBeTruthy();
  });
  it("should return true for fields with no validations", () => {
    expect(
      validate(["businessInformation.businessType"], formDataDto)
    ).toBeTruthy();
  });
  it("should return true for nested fields with conditions", () => {
    expect(
      validate(
        [
          'location.addresses.*.postalCode(location.addresses.*.country.text === "CA")',
        ],
        formDataDto
      )
    ).toBeTruthy();
  });
  describe("runValidation", () => {
    describe("when target is an array", () => {
      it("should stop at the first error when exhaustive is false (default)", () => {
        const target = {
          foo: [10, 0, 5, 0, 2],
        };
        const notificationBus = useEventBus<ValidationMessageType | undefined>(
          "error-notification"
        );
        let count = 0;
        notificationBus.on(() => {
          count++;
        });
        const result = runValidation("foo", target, isGreaterThanZero, true);
        expect(result).toBe(false);
        expect(count).toEqual(2);
      });
      it("should validate every item in the array regardless of finding any error when exhaustive is true", () => {
        const target = {
          foo: [10, 0, 5, 0, 2],
        };
        const notificationBus = useEventBus<ValidationMessageType | undefined>(
          "error-notification"
        );
        let count = 0;
        notificationBus.on(() => {
          count++;
        });
        const result = runValidation(
          "foo",
          target,
          isGreaterThanZero,
          true,
          true
        );
        expect(result).toBe(false);
        expect(count).toEqual(5);
      });
      it("should emit the notification regardless of being valid or invalid", () => {
        const target = {
          foo: [10, 0],
        };
        const notificationBus = useEventBus<ValidationMessageType | undefined>(
          "error-notification"
        );
        let invalidCount = 0;
        let validCount = 0;
        notificationBus.on((event) => {
          if (event.errorMsg) {
            invalidCount++;
          } else {
            validCount++;
          }
        });
        const result = runValidation(
          "foo",
          target,
          isGreaterThanZero,
          true,
          true
        );
        expect(result).toBe(false);
        expect(invalidCount).toEqual(1);
        expect(validCount).toEqual(1);
      });
      it("should return true when every item is valid", () => {
        const target = {
          foo: [10, 20, 30],
        };
        const notificationBus = useEventBus<ValidationMessageType | undefined>(
          "error-notification"
        );
        let invalidCount = 0;
        let validCount = 0;
        notificationBus.on((event) => {
          if (event.errorMsg) {
            invalidCount++;
          } else {
            validCount++;
          }
        });
        const result = runValidation(
          "foo",
          target,
          isGreaterThanZero,
          true,
          true
        );
        expect(result).toBe(true);
        expect(invalidCount).toEqual(0);
        expect(validCount).toEqual(3);
      });
    });
  });
});
