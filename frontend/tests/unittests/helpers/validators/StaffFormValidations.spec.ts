import { describe, it, expect } from "vitest";
import { newFormDataDto } from "@/dto/ApplyClientNumberDto";
import { validate } from "@/helpers/validators/StaffFormValidations";

type Scenario = [any, boolean, string?];

const test = (target: any, key: string, setter: (value: any) => void, scenario: Scenario) => {
  const [value, expected, reason] = scenario;
  const name =
    `should return ${expected} when value is: ${JSON.stringify(value)}` +
    (reason ? ` (${reason})` : "");
  it(name, () => {
    setter(value);
    expect(validate([key], target)).toBe(expected);
  });
};

describe("validations", () => {
  describe.each(["businessInformation.birthdate"])("%s", (key) => {
    const formDataDto = newFormDataDto();
    const setter = (value: string) => {
      formDataDto.businessInformation.birthdate = value;
    };
    const today = new Date();
    const currentYear = today.getFullYear();
    const twentyYearsAgo = currentYear - 20;
    const tenYearsAgo = currentYear - 10;
    (<Scenario[]>[
      [`${twentyYearsAgo}-02-16`, true],
      [`${tenYearsAgo}-02-16`, false],
    ]).forEach((scenario) => {
      test(formDataDto, key, setter, scenario);
    });
  });

  describe.each(["businessInformation.middleName"])("%s", (key) => {
    const formDataDto = newFormDataDto();
    const setter = (value: string) => {
      formDataDto.businessInformation.middleName = value;
    };
    (<Scenario[]>[
      ["", true],
      ["Elizabeth", true],
      ["This is more than 30 characters for sure", false],
      ["Unallowed!", false],
      ["Unallowéd", false],
    ]).forEach((scenario) => {
      test(formDataDto, key, setter, scenario);
    });
  });

  describe.each(["idType.text"])("%s", (key) => {
    const data: any = {};
    const setter = (value: any) => {
      data.idType = value;
    };
    (<Scenario[]>[
      [{ text: "" }, false],
      [{ text: "" }, false],
      [{ text: "ABCD" }, true],
    ]).forEach((scenario) => {
      test(data, key, setter, scenario);
    });
  });

  describe.each(["issuingProvince.text"])("%s", (key) => {
    const data: any = {};
    const setter = (value: any) => {
      data.issuingProvince = value;
    };
    (<Scenario[]>[
      [{ text: "" }, false],
      [{ text: "" }, false],
      [{ text: "AB" }, true],
    ]).forEach((scenario) => {
      test(data, key, setter, scenario);
    });
  });

  describe.each(["businessInformation.idType"])("%s", (key) => {
    const formDataDto = newFormDataDto();
    const setter = (value: string) => {
      formDataDto.businessInformation.idType = value;
    };
    (<Scenario[]>[
      ["", false],
      [null, false],
      ["A", true],
    ]).forEach((scenario) => {
      test(formDataDto, key, setter, scenario);
    });
  });

  describe.each(["businessInformation.idNumber"])("%s", (key) => {
    const formDataDto = newFormDataDto();
    const setter = (value: string) => {
      formDataDto.businessInformation.idNumber = value;
    };
    (<Scenario[]>[
      ["", false],
      [null, false],
      ["1", true],
    ]).forEach((scenario) => {
      test(formDataDto, key, setter, scenario);
    });
  });

  describe("idNumber variations", () => {
    const data: any = {
      businessInformation: {},
    };

    describe.each(["businessInformation.idNumber-BCDL"])("%s", (key) => {
      const setter = (value: string) => {
        data.businessInformation["idNumber-BCDL"] = value;
      };
      (<Scenario[]>[
        ["1234ABCD", false, "contains letters"],
        ["123456", false, "less than 7 digits"],
        ["1234567", true],
        ["12345678", true],
        ["123456789", false, "more than 8 digits"],
      ]).forEach((scenario) => {
        test(data, key, setter, scenario);
      });
    });

    describe.each(["businessInformation.idNumber-nonBCDL"])("%s", (key) => {
      const setter = (value: string) => {
        data.businessInformation["idNumber-nonBCDL"] = value;
      };
      (<Scenario[]>[
        ["1234ABC!", false, "contains special character"],
        ["1234ABCa", false, "contains lower-case letter"],
        ["1234 ABC", false, "contains space"],
        ["123456", false, "less than 7 digits"],
        ["1234ABC", true],
        ["12345678901234567890", true],
        ["12345678901234567890A", false, "more than 20 digits"],
      ]).forEach((scenario) => {
        test(data, key, setter, scenario);
      });
    });

    describe.each(["businessInformation.idNumber-BRTH"])("%s", (key) => {
      const setter = (value: string) => {
        data.businessInformation["idNumber-BRTH"] = value;
      };
      (<Scenario[]>[
        ["12345678901", false, "less than 12 digits"],
        ["12345678901A", false, "contains letters"],
        ["123456789012", true],
        ["1234567890123", true],
        ["12345678901234", false, "more than 13 digits"],
      ]).forEach((scenario) => {
        test(data, key, setter, scenario);
      });
    });

    describe.each(["businessInformation.idNumber-PASS"])("%s", (key) => {
      const setter = (value: string) => {
        data.businessInformation["idNumber-PASS"] = value;
      };
      (<Scenario[]>[
        ["1234ABC!", false, "contains special character"],
        ["1234ABCa", false, "contains lower-case letter"],
        ["1234 ABC", false, "contains space"],
        ["1234567", false, "less than 8 digits"],
        ["1234ABCD", true],
        ["1234ABCD9", false, "more than 8 digits"],
      ]).forEach((scenario) => {
        test(data, key, setter, scenario);
      });
    });

    describe.each(["businessInformation.idNumber-CITZ"])("%s", (key) => {
      const setter = (value: string) => {
        data.businessInformation["idNumber-CITZ"] = value;
      };
      (<Scenario[]>[
        ["1234ABC!", false, "contains special character"],
        ["1234ABCa", false, "contains lower-case letter"],
        ["1234 ABC", false, "contains space"],
        ["1234567", false, "less than 8 digits"],
        ["1234ABCD", true],
        ["1234ABCD9", false, "more than 8 digits"],
      ]).forEach((scenario) => {
        test(data, key, setter, scenario);
      });
    });

    describe.each(["businessInformation.idNumber-FNID"])("%s", (key) => {
      const setter = (value: string) => {
        data.businessInformation["idNumber-FNID"] = value;
      };
      (<Scenario[]>[
        ["123456789", false, "less than 10 digits"],
        ["123456789A", false, "contains letters"],
        ["1234567890", true],
        ["12345678901", false, "more than 10 digits"],
      ]).forEach((scenario) => {
        test(data, key, setter, scenario);
      });
    });

    describe.each(["businessInformation.idNumber-OTHR"])("%s", (key) => {
      const setter = (value: string) => {
        data.businessInformation["idNumber-OTHR"] = value;
      };
      (<Scenario[]>[
        ["12345", false, "No colon"],
        ["Name:", false, "missing Id number value"],
        [":1234", false, "missing Id type value"],
        ["A:123", true],
        ["A: 123", true],
        ["A :123", true],
        ["A : 123", true],
        ["A:  123", false, "more than 1 space after colon"],
        ["Name With Spaces: 123", true],
        ["Name: 1234ABC!", false, "Id number part contains special character"],
        ["Name: 1234ABCa", false, "Id number part contains lower-case letter"],
        ["Name: 1234 ABC", false, "Id number part contains space"],
        ["Name: 12", false, "Id number part is less than 3 digits"],
        ["Name: 123", true],
        ["Name: 7890123456789012345678901234567890", true],
        [
          "Name: 78901234567890123456789012345678901",
          false,
          "the whole value has more than 40 digits",
        ],
      ]).forEach((scenario) => {
        test(data, key, setter, scenario);
      });
    });
  });
});
