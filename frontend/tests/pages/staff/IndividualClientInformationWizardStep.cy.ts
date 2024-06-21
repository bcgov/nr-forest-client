import IndividualClientInformationWizardStep from "@/pages/staffform/IndividualClientInformationWizardStep.vue";
import type { FormDataDto } from "@/dto/ApplyClientNumberDto";

describe("<individual-client-information-wizard-step />", () => {
  beforeEach(() => {
    cy.intercept("GET", "/api/countries/CA/provinces?page=0&size=250", {
      fixture: "provinces.json",
    }).as("getProvinces");

    cy.intercept("GET", "/api/countries/US/provinces?page=0&size=250", {
      fixture: "states.json",
    }).as("getStates");
  });

  const getDefaultProps = () => ({
    data: {
      businessInformation: {
        businessType: "",
        legalType: "",
        clientType: "",
        registrationNumber: "",
        businessName: "",
        goodStandingInd: "",
        birthdate: "",
        address: "",
      },
      location: {
        contacts: [
          {
            firstName: "",
            lastName: "",
          },
        ],
      },
    } as unknown as FormDataDto,
  });

  let currentProps = null;
  const mount = (props = getDefaultProps()) => {
    currentProps = props;
    return cy
      .mount(IndividualClientInformationWizardStep, {
        props,
      })
      .its("wrapper")
      .as("vueWrapper");
  };

  it("renders the IndividualClientInformationWizardStep component", () => {
    mount();

    cy.get("#firstName").should("be.visible");
    cy.get("#middleName").should("be.visible");
    cy.get("#lastName").should("be.visible");
    cy.get("#birthdate").should("be.visible");
    cy.get("#identificationType").should("be.visible");
    cy.get("#clientIdentification").should("be.visible");
  });

  describe('when ID type is "Canadian driver\'s licence"', () => {
    beforeEach(() => {
      mount();

      cy.get("#identificationType")
        .should("be.visible")
        .and("have.value", "")
        .find("[part='trigger-button']")
        .click();

      cy.get("#identificationType")
        .find('cds-combo-box-item[data-id="CDDL"]')
        .should("be.visible")
        .click();
    });
    it("displays the Issuing province field with label 'Issuing province'", () => {
      cy.get("#identificationProvince").contains("Issuing province");
    });
    it("displays the Issuing province field with option 'British Columbia' selected by default", () => {
      cy.get("#identificationProvince").should("be.visible").and("have.value", "British Columbia");
    });
  });

  describe('when ID type is "US driver\'s licence"', () => {
    beforeEach(() => {
      mount();

      cy.get("#identificationType")
        .should("be.visible")
        .and("have.value", "")
        .find("[part='trigger-button']")
        .click();

      cy.get("#identificationType")
        .find('cds-combo-box-item[data-id="USDL"]')
        .should("be.visible")
        .click();
    });

    it("displays the Issuing province field with label 'Issuing state'", () => {
      cy.get("#identificationProvince").contains("Issuing state");
    });
    it("displays the Issuing province field with option 'British Columbia' selected by default", () => {
      cy.get("#identificationProvince").should("be.visible").and("have.value", "");
    });
  });

  describe("validation", () => {
    describe("ID number according to the current ID type (and identificationProvince)", () => {
      beforeEach(() => {
        mount();
      });
      const valueNumeric13 = "1234567890123";
      const valueAlphanumeric13 = "1234567890ABC";
      const valueNumeric8 = "12345678";
      const valueAlphanumeric8 = "12345ABC";

      const isValid = () => {
        cy.get("#clientIdentification").shadow().find("[name='invalid-text']").should("not.exist");
      };

      const isInvalid = () => {
        cy.get("#clientIdentification")
          .shadow()
          .find("[name='invalid-text']")
          .invoke("text")
          .should("not.be.empty");
      };

      describe("When ID type is 'BRTH'", () => {
        beforeEach(() => {
          cy.get("#identificationType")
            .should("be.visible")
            .and("have.value", "")
            .find("[part='trigger-button']")
            .click();

          cy.get("#identificationType")
            .find('cds-combo-box-item[data-id="BRTH"]')
            .should("be.visible")
            .click();
        });
        it("allows up to 13 digits", () => {
          cy.get("#clientIdentification").shadow().find("input").type(valueNumeric13);

          cy.get("#clientIdentification").shadow().find("input").blur();

          isValid();
        });
        it("displays error message if the value contains letters", () => {
          cy.get("#clientIdentification").shadow().find("input").type(valueAlphanumeric13);

          cy.get("#clientIdentification").shadow().find("input").blur();

          isInvalid();
        });
      });
      describe("when ID type is 'PASS'", () => {
        beforeEach(() => {
          cy.get("#identificationType")
            .should("be.visible")
            .and("have.value", "")
            .find("[part='trigger-button']")
            .click();

          cy.get("#identificationType")
            .find('cds-combo-box-item[data-id="PASS"]')
            .should("be.visible")
            .click();
        });
        it("displays error message if the value has more than 8 digits", () => {
          cy.get("#clientIdentification").shadow().find("input").type(valueNumeric13);

          cy.get("#clientIdentification").shadow().find("input").blur();

          isInvalid();
        });
        it("allows numbers and letters", () => {
          cy.get("#clientIdentification").shadow().find("input").type(valueAlphanumeric8);

          cy.get("#clientIdentification").shadow().find("input").blur();

          isValid();
        });
      });
      describe("when ID type is 'CDDL'", () => {
        beforeEach(() => {
          cy.get("#identificationType")
            .should("be.visible")
            .and("have.value", "")
            .find("[part='trigger-button']")
            .click();

          cy.get("#identificationType")
            .find('cds-combo-box-item[data-id="CDDL"]')
            .should("be.visible")
            .click();
        });
        describe("and issuing province is 'BC' (default value)", () => {
          it("displays error message if the value has more than 8 digits", () => {
            cy.get("#clientIdentification").shadow().find("input").type(valueNumeric13);

            cy.get("#clientIdentification").shadow().find("input").blur();

            isInvalid();
          });
          it("displays error message if the value contains letters", () => {
            cy.get("#clientIdentification").shadow().find("input").type(valueAlphanumeric8);

            cy.get("#clientIdentification").shadow().find("input").blur();

            isInvalid();
          });
          it("allows 8-digit numeric value", () => {
            cy.get("#clientIdentification").shadow().find("input").type(valueNumeric8);

            cy.get("#clientIdentification").shadow().find("input").blur();

            isValid();
          });
        });
        describe("and issuing province is something other than 'BC' (for example, 'AB')", () => {
          beforeEach(() => {
            cy.get("#identificationProvince")
              .should("be.visible")
              .and("have.value", "British Columbia")
              .find("[part='trigger-button']")
              .click();

            cy.get("#identificationProvince")
              .find('cds-combo-box-item[data-id="AB"]')
              .should("be.visible")
              .click();
          });
          it("allows up to 20 digits", () => {
            cy.get("#clientIdentification").shadow().find("input").type("12345678901234567890");

            cy.get("#clientIdentification").shadow().find("input").blur();

            isValid();
          });
          it("allows numbers and letters", () => {
            cy.get("#clientIdentification").shadow().find("input").type(valueAlphanumeric8);

            cy.get("#clientIdentification").shadow().find("input").blur();

            isValid();
          });
          it("displays error message if the value has more than 20 digits", () => {
            cy.get("#clientIdentification").shadow().find("input").type("123456789012345678901");

            cy.get("#clientIdentification").shadow().find("input").blur();

            isInvalid();
          });
        });
      });
    });
  });

  describe("businessInformation.businessName", () => {
    beforeEach(() => {
      mount();

      cy.get("#firstName").shadow().find("input").type("John");
      cy.get("#lastName").shadow().find("input").type("Silver");
    });
    describe("when middleName is not provided", () => {
      it("sets the businessName in the businessInformation to '<firstName> <lastName>'", () => {
        cy.wrap(currentProps.data.businessInformation)
          .its("businessName")
          .should("equal", "John Silver");
      });
    });
    describe("when middleName is provided", () => {
      beforeEach(() => {
        cy.get("#middleName").shadow().find("input").type("Michael");
      });
      it("sets the businessName in the businessInformation to '<firstName> <middleName> <lastName>'", () => {
        cy.wrap(currentProps.data.businessInformation)
          .its("businessName")
          .should("equal", "John Michael Silver");
      });
    });
  });

  describe("when all required fields are properly filled", () => {
    beforeEach(() => {
      mount();

      cy.get("#firstName").shadow().find("input").type("John");

      cy.get("#lastName").shadow().find("input").type("Silver");

      cy.get("#birthdateYear").shadow().find("input").type("2001");
      cy.get("#birthdateMonth").shadow().find("input").type("05");
      cy.get("#birthdateDay").shadow().find("input").type("30");

      cy.get("#identificationType").find("[part='trigger-button']").click();

      cy.get("#identificationType").find('cds-combo-box-item[data-id="BRTH"]').click();

      cy.get("#clientIdentification").shadow().find("input").type("1234567890123");

      cy.get("#clientIdentification").shadow().find("input").blur();
    });
    it("emits valid true", () => {
      cy.get("@vueWrapper").should((vueWrapper) => {
        const lastValid = vueWrapper.emitted("valid").slice(-1)[0];

        // The last valid event was emitted with true.
        expect(lastValid[0]).to.equal(true);
      });
    });
    describe("when the middle name is also filled", () => {
      beforeEach(() => {
        cy.get("#middleName").shadow().find("input").type("Michael");

        cy.get("#middleName").shadow().find("input").blur();
      });
      it("should not emit valid false even if the middle name gets cleared", () => {
        cy.get("@vueWrapper").should((vueWrapper) => {
          const lastValid = vueWrapper.emitted("valid").slice(-1)[0];

          // The last valid event was emitted with true.
          expect(lastValid[0]).to.equal(true);
        });

        cy.get("#middleName").shadow().find("input").clear();

        cy.get("#middleName").shadow().find("input").blur();

        cy.get("@vueWrapper").should((vueWrapper) => {
          const lastValid = vueWrapper.emitted("valid").slice(-1)[0];

          // The last valid event was emitted with true.
          expect(lastValid[0]).to.equal(true);
        });
      });
    });
  });
});
