import type { ClientDetails } from "@/dto/CommonTypesDto";
import RegistrationNumber from "@/pages/client-details/RegistrationNumber.vue";

describe("<registration-number />", () => {
  const getDefaultData = () =>
    ({
      client: {
        registryCompanyTypeCode: "BRT",
        corpRegnNmbr: "12345",
        clientTypeCode: "A",
      },
    }) as ClientDetails;

  const getDefaultProps = (dataBlueprint = getDefaultData()) => ({
    modelValue: structuredClone(dataBlueprint),
    originalValue: structuredClone(dataBlueprint),
  });

  let currentProps: ReturnType<typeof getDefaultProps> = null;

  let calls: boolean[] = [];
  const onValid = (valid: boolean) => {
    calls.push(valid);
  };

  const mount = (props = getDefaultProps()) => {
    calls = [];
    currentProps = props;
    return cy
      .mount(RegistrationNumber, {
        props: {
          onValid,
          ...props,
        },
      })
      .its("wrapper")
      .as("vueWrapper");
  };

  const testInputTag = (inputTag: string, rawSelector: string, value?: string) => {
    const selector = `${inputTag}${rawSelector}`;
    cy.get(selector).should("be.visible");
    if (value !== undefined) {
      cy.get(selector).should("have.value", value);
    }
  };

  const testTextInput = (rawSelector: string, value?: string) =>
    testInputTag("cds-text-input", rawSelector, value);

  const testComboBox = (rawSelector: string, value?: string) =>
    testInputTag("cds-combo-box", rawSelector, value);

  beforeEach(() => {
    cy.intercept("GET", "/api/codes/registry-types/*", {
      fixture: "registryTypes.json",
    }).as("getRegistryTypes");
  });

  const registryTypeDesc = "Bogus Registry Type";

  const checkLastValid = (check: (valid: boolean) => void) => {
    cy.wrap(calls).then((value) => {
      const last = value.pop();
      check(last);
    });
  };

  it("displays the Type and the Number inputs", () => {
    mount();
    const formattedTypeName = `${currentProps.modelValue.client.registryCompanyTypeCode} - ${registryTypeDesc}`;
    testComboBox("#input-registryType", formattedTypeName);
    testTextInput("#input-registryNumber", currentProps.modelValue.client.corpRegnNmbr);
  });

  it("validates successfully when both inputs get filled in", () => {
    const data = {
      client: {
        registryCompanyTypeCode: "",
        corpRegnNmbr: "",
        clientTypeCode: "A",
      },
    } as ClientDetails;
    const props = getDefaultProps(data);
    mount(props);

    cy.selectFormEntry("#input-registryType", "BRT - Bogus Registry Type");
    cy.fillFormEntry("#input-registryNumber", "123");

    cy.get(".field-error").should("have.text", "");

    checkLastValid((valid) => {
      expect(valid).to.equal(true);
    });
  });

  it("returns an error when both inputs become empty", () => {
    mount();

    cy.get("#input-registryType").shadow().find("div#selection-button").click();
    cy.clearFormEntry("#input-registryNumber");

    // the message will either refer to the type or the number, but both start with "You must".
    cy.get(".field-error").contains("You must");

    checkLastValid((valid) => {
      expect(valid).to.equal(false);
    });
  });

  describe("when the original value is incomplete", () => {
    beforeEach(() => {
      const data = {
        client: {
          registryCompanyTypeCode: "", // Type is missing
          corpRegnNmbr: "123456",
          clientTypeCode: "A",
        },
      } as ClientDetails;
      const props = getDefaultProps(data);
      mount(props);
    });
    it("validates successfully when the inputs are not changed", () => {
      cy.get(".field-error").should("have.text", "");

      checkLastValid((valid) => {
        expect(valid).to.equal(true);
      });
    });

    it("validates successfully when the inputs are changed back to their original value", () => {
      cy.fillFormEntry("#input-registryNumber", "7");

      // Raised an error because the value got changed
      cy.contains(".field-error", "You must select a type");

      checkLastValid((valid) => {
        expect(valid).to.equal(false);
      });

      cy.fillFormEntry("#input-registryNumber", "{backspace}");

      // Removed the error just because the value got changed back to its original value
      cy.get(".field-error").should("have.text", "");

      checkLastValid((valid) => {
        expect(valid).to.equal(true);
      });
    });
  });

  describe("when the original value has both fields filled in", () => {
    beforeEach(() => {
      const data = {
        client: {
          registryCompanyTypeCode: "BRT",
          corpRegnNmbr: "123456",
          clientTypeCode: "A",
        },
      } as ClientDetails;
      const props = getDefaultProps(data);
      mount(props);
    });
    it("returns an error when the Number becomes empty", () => {
      cy.clearFormEntry("#input-registryNumber");

      cy.contains(".field-error", "You must provide a number");

      checkLastValid((valid) => {
        expect(valid).to.equal(false);
      });
    });

    it("returns an error when the Type becomes empty", () => {
      cy.get("#input-registryType").shadow().find("div#selection-button").click();

      cy.contains(".field-error", "You must select a type");

      checkLastValid((valid) => {
        expect(valid).to.equal(false);
      });
    });
  });

  describe("when the original value has both fields empty", () => {
    beforeEach(() => {
      const data = {
        client: {
          registryCompanyTypeCode: "",
          corpRegnNmbr: "",
          clientTypeCode: "A",
        },
      } as ClientDetails;
      const props = getDefaultProps(data);
      mount(props);
    });

    it("returns an error when the Number gets filled in", () => {
      cy.fillFormEntry("#input-registryNumber", "123");

      cy.contains(".field-error", "You must select a type");

      checkLastValid((valid) => {
        expect(valid).to.equal(false);
      });
    });

    it("returns an error when the Type gets filled in", () => {
      cy.selectFormEntry("#input-registryType", "BRT - Bogus Registry Type");

      cy.contains(".field-error", "You must provide a number");

      checkLastValid((valid) => {
        expect(valid).to.equal(false);
      });
    });
  });
});
