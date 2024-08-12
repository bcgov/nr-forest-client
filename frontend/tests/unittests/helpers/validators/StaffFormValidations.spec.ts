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
  describe.each(["businessInformation.clientType"])("%s", (key) => {
    const formDataDto = newFormDataDto();
    const setter = (value: string) => {
      formDataDto.businessInformation.clientType = value;
    };
    (<Scenario[]>[
      ["", false],
      ["I", true],
    ]).forEach((scenario) => {
      test(formDataDto, key, setter, scenario);
    });
  });

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

  describe.each(["businessInformation.firstName"])("%s", (key) => {
    const formDataDto = newFormDataDto();
    const setter = (value: string) => {
      formDataDto.businessInformation.firstName = value;
    };
    (<Scenario[]>[
      ["", false],
      ["John", true],
      ["Name with more than 30 characters", false],
      ["Unallowed!", false],
      ["Unallowéd", false],
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
      ["", true, "not required"],
      ["Elizabeth", true],
      ["Name with more than 30 characters", false],
      ["Unallowed!", false],
      ["Unallowéd", false],
    ]).forEach((scenario) => {
      test(formDataDto, key, setter, scenario);
    });
  });

  describe.each(["businessInformation.clientAcronym"])("%s", (key) => {
    const formDataDto = newFormDataDto();
    const setter = (value: string) => {
      formDataDto.businessInformation.clientAcronym = value;
    };
    (<Scenario[]>[
      ["", true, "not required"],
      ["AT&T", true],
      ["Acronym with more than 30 characters", false],
      ["1", false],
    ]).forEach((scenario) => {
      test(formDataDto, key, setter, scenario);
    });
  });

  describe.each(["businessInformation.workSafeBcNumber"])("%s", (key) => {
    const formDataDto = newFormDataDto();
    const setter = (value: string) => {
      formDataDto.businessInformation.workSafeBcNumber = value;
    };
    (<Scenario[]>[
      ["", true, "not required"],
      ["123456", true],
      ["0123456789", false],
      ["Invalid Work Safe BC Number", false],
    ]).forEach((scenario) => {
      test(formDataDto, key, setter, scenario);
    });
  });

  describe.each(["businessInformation.lastName"])("%s", (key) => {
    const formDataDto = newFormDataDto();
    const setter = (value: string) => {
      formDataDto.businessInformation.lastName = value;
    };
    (<Scenario[]>[
      ["", false],
      ["Silver", true],
      ["Name with more than 30 characters", false],
      ["Unallowed!", false],
      ["Unallowéd", false],
    ]).forEach((scenario) => {
      test(formDataDto, key, setter, scenario);
    });
  });

  describe.each(["identificationType.text"])("%s", (key) => {
    const data: any = {};
    const setter = (value: any) => {
      data.identificationType = value;
    };
    (<Scenario[]>[
      [{ text: "" }, false],
      [{ text: "" }, false],
      [{ text: "ABCD" }, true],
    ]).forEach((scenario) => {
      test(data, key, setter, scenario);
    });
  });

  describe.each(["identificationProvince.text"])("%s", (key) => {
    const data: any = {};
    const setter = (value: any) => {
      data.identificationProvince = value;
    };
    (<Scenario[]>[
      [{ text: "" }, false],
      [{ text: null }, false],
      [{ text: "AB" }, true],
    ]).forEach((scenario) => {
      test(data, key, setter, scenario);
    });
  });

  describe.each(["businessInformation.identificationType"])("%s", (key) => {
    const formDataDto = newFormDataDto();
    const setter = (value: any) => {
      formDataDto.businessInformation.identificationType = value;
    };
    (<Scenario[]>[
      ["", false],
      [null, false],
      ["A", true],
    ]).forEach((scenario) => {
      test(formDataDto, key, setter, scenario);
    });
  });

  describe.each(["businessInformation.clientIdentification"])("%s", (key) => {
    const formDataDto = newFormDataDto();
    const setter = (value: string) => {
      formDataDto.businessInformation.clientIdentification = value;
    };
    (<Scenario[]>[
      ["", false],
      [null, false],
      ["1", true],
    ]).forEach((scenario) => {
      test(formDataDto, key, setter, scenario);
    });
  });

  describe("clientIdentification variations", () => {
    const data: any = {
      businessInformation: {},
    };

    describe.each(["businessInformation.clientIdentification-BCDL"])("%s", (key) => {
      const setter = (value: string) => {
        data.businessInformation["clientIdentification-BCDL"] = value;
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

    describe.each(["businessInformation.clientIdentification-nonBCDL"])("%s", (key) => {
      const setter = (value: string) => {
        data.businessInformation["clientIdentification-nonBCDL"] = value;
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

    describe.each(["businessInformation.clientIdentification-BRTH"])("%s", (key) => {
      const setter = (value: string) => {
        data.businessInformation["clientIdentification-BRTH"] = value;
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

    describe.each(["businessInformation.clientIdentification-PASS"])("%s", (key) => {
      const setter = (value: string) => {
        data.businessInformation["clientIdentification-PASS"] = value;
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

    describe.each(["businessInformation.clientIdentification-CITZ"])("%s", (key) => {
      const setter = (value: string) => {
        data.businessInformation["clientIdentification-CITZ"] = value;
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

    describe.each(["businessInformation.clientIdentification-FNID"])("%s", (key) => {
      const setter = (value: string) => {
        data.businessInformation["clientIdentification-FNID"] = value;
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

  });
});
