import IndividualClientInformationWizardStep from "@/pages/staffform/IndividualClientInformationWizardStep.vue";
import type { FormDataDto } from "@/dto/ApplyClientNumberDto";

describe("<individual-client-information-wizard-step />", () => {
  beforeEach(() => {
    cy.viewport(1280, 720);
    cy.intercept("GET", "/api/codes/countries/CA/provinces?page=0&size=250", {
      fixture: "provinces.json",
    }).as("getProvinces");

    cy.intercept("GET", "/api/codes/countries/US/provinces?page=0&size=250", {
      fixture: "states.json",
    }).as("getStates");

    cy.intercept("GET", "/api/codes/identification-types", {
      fixture: "identificationTypes.json",
    }).as("getIdentificationTypes");
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
    userRoles: ["CLIENT_EDITOR"],
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

    describe('and ID type is changed to "US driver\'s licence"', () => {
      beforeEach(() => {
        cy.get("#identificationType")
          .should("be.visible")
          .and("have.value", "Canadian driver's licence") // initial value
          .find("[part='trigger-button']")
          .click();

        cy.get("#identificationType")
          .find('cds-combo-box-item[data-id="USDL"]')
          .should("be.visible")
          .and("have.value", "US driver's licence") // new value
          .click();
      });
      it("should clear the value on identificationProvince", () => {
        cy.get("#identificationProvince").should("be.visible").and("have.value", "");
      });
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
    describe("Date of birth", () => {
      beforeEach(() => {
        mount();
      });

      it("requires a Date of birth to be entered (by default)", () => {
        cy.clearFormEntry("#birthdateYear");
        cy.get("#birthdateYear").should("have.attr", "invalid");
        cy.get("#birthdate").should("contain", "You must enter a date of birth");
      });

      describe("when user is admin", () => {
        beforeEach(() => {
          const props = getDefaultProps();
          props.userRoles = ["CLIENT_ADMIN"];
          mount(props);
        });

        it("doesn't require a Date of birth to be entered", () => {
          cy.clearFormEntry("#birthdateYear");
          cy.get("#birthdateYear").should("not.have.attr", "invalid");
          cy.get("#birthdate").should("not.contain", "You must enter a date of birth");
        });
      });
    });
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
          cy.wait(1);

          isValid();
        });
        it("displays error message if the value contains letters", () => {
          cy.get("#clientIdentification").shadow().find("input").type(valueAlphanumeric13);

          cy.get("#clientIdentification").shadow().find("input").blur();
          cy.wait(1);

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
          cy.wait(1);

          isInvalid();
        });
        it("allows numbers and letters", () => {
          cy.get("#clientIdentification").shadow().find("input").type(valueAlphanumeric8);

          cy.get("#clientIdentification").shadow().find("input").blur();
          cy.wait(1);

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
            cy.wait(1);

            isInvalid();
          });
          it("displays error message if the value contains letters", () => {
            cy.get("#clientIdentification").shadow().find("input").type(valueAlphanumeric8);

            cy.get("#clientIdentification").shadow().find("input").blur();
            cy.wait(1);

            isInvalid();
          });
          it("allows 8-digit numeric value", () => {
            cy.get("#clientIdentification").shadow().find("input").type(valueNumeric8);

            cy.get("#clientIdentification").shadow().find("input").blur();
            cy.wait(1);

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
            cy.wait(1);

            isValid();
          });
          it("allows numbers and letters", () => {
            cy.get("#clientIdentification").shadow().find("input").type(valueAlphanumeric8);

            cy.get("#clientIdentification").shadow().find("input").blur();
            cy.wait(1);

            isValid();
          });
          it("displays error message if the value has more than 20 digits", () => {
            cy.get("#clientIdentification").shadow().find("input").type("123456789012345678901");

            cy.get("#clientIdentification").shadow().find("input").blur();
            cy.wait(1);

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

  describe("when all required fields are properly filled in", () => {
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
      cy.wait(1);
    });
    it("emits valid true", () => {
      cy.get("@vueWrapper").should((vueWrapper) => {
        const lastValid = vueWrapper.emitted("valid").slice(-1)[0];

        // Last event (in)"valid" emitted
        expect(lastValid[0]).to.equal(true);
      });
    });
    describe("when the middle name is also filled in", () => {
      beforeEach(() => {
        cy.get("#middleName").shadow().find("input").type("Michael");

        cy.get("#middleName").shadow().find("input").blur();
        cy.wait(1);
      });
      it("should not emit valid false even if the middle name gets cleared", () => {
        cy.get("@vueWrapper").should((vueWrapper) => {
          const lastValid = vueWrapper.emitted("valid").slice(-1)[0];

          // Last event (in)"valid" emitted
          expect(lastValid[0]).to.equal(true);
        });

        cy.get("#middleName").shadow().find("input").clear();

        cy.get("#middleName").shadow().find("input").blur();
        cy.wait(1);

        cy.get("@vueWrapper").should((vueWrapper) => {
          const lastValid = vueWrapper.emitted("valid").slice(-1)[0];

          // Last event (in)"valid" emitted
          expect(lastValid[0]).to.equal(true);
        });
      });
    });
    describe("when the step is invalid due to an invalid middle name", () => {
      beforeEach(() => {
        cy.get("#middleName").shadow().find("input").type("é");

        cy.get("#middleName").shadow().find("input").blur();
        cy.wait(1);

        // Asserting the step is currently invalid
        cy.get("@vueWrapper").should((vueWrapper) => {
          const lastValid = vueWrapper.emitted("valid").slice(-1)[0];

          // Last event (in)"valid" emitted
          expect(lastValid[0]).to.equal(false);
        });
      });
      describe("and the middle name gets cleared", () => {
        beforeEach(() => {
          cy.get("#middleName").shadow().find("input").clear();
        });
        it("should emit valid true even without blurring the input", () => {
          cy.get("@vueWrapper").should((vueWrapper) => {
            const lastValid = vueWrapper.emitted("valid").slice(-1)[0];

            // Last event (in)"valid" emitted
            expect(lastValid[0]).to.equal(true);
          });
        });
      });
    });

    describe('when ID type is "Canadian driver\'s licence"', () => {
      beforeEach(() => {
        cy.get("#identificationType").should("be.visible").find("[part='trigger-button']").click();

        cy.get("#identificationType")
          .find('cds-combo-box-item[data-id="CDDL"]')
          .should("be.visible")
          .click();
      });

      describe("and the Issuing province gets cleared", () => {
        beforeEach(() => {
          cy.get("#identificationProvince")
            .should("be.visible")
            .and("have.value", "British Columbia")
            .find("#selection-button") // The X clear button
            .click();
        });

        it("should emit valid false", () => {
          cy.get("@vueWrapper").should((vueWrapper) => {
            const lastValid = vueWrapper.emitted("valid").slice(-1)[0];

            // Last event (in)"valid" emitted
            expect(lastValid[0]).to.equal(false);
          });
        });

        describe("and the ID type gets changed to one that does not display the Issuing province/state", () => {
          beforeEach(() => {
            cy.get("#identificationType")
              .should("be.visible")
              .and("have.value", "Canadian driver's licence") // initial value
              .find("[part='trigger-button']")
              .click();

            cy.get("#identificationType")
              .find('cds-combo-box-item[data-id="PASS"]')
              .and("have.value", "Canadian passport") // new value
              .should("be.visible")
              .click();
          });
          describe("and the ID number is properly filled in", () => {
            beforeEach(() => {
              cy.clearFormEntry("#clientIdentification");
              cy.get("#clientIdentification").shadow().find("input").type("12345678");

              cy.get("#clientIdentification").shadow().find("input").blur();
              cy.wait(1);
            });
            it("should emit valid true", () => {
              /*
              This test makes sure the impact of clearing the province (and thus making the
              province field invalid) does not matter anymore after we get to a situation where the
              province field is not used.
              */
              cy.get("@vueWrapper").should((vueWrapper) => {
                const lastValid = vueWrapper.emitted("valid").slice(-1)[0];

                // Last event (in)"valid" emitted
                expect(lastValid[0]).to.equal(true);
              });
            });
          });
        });
      });

      describe("and the ID number gets filled in", () => {
        beforeEach(() => {
          cy.clearFormEntry("#clientIdentification");
          cy.fillFormEntry("#clientIdentification", "1234567");
          cy.wait(1);
        });

        describe("and the Issuing province is changed", () => {
          beforeEach(() => {
            cy.selectFormEntry("#identificationProvince", "Manitoba", false);
            cy.wait(1);
          });

          it("should emit valid true since the supplied driver's licence ID is also valid for Manitoba", () => {
            cy.get("@vueWrapper").should((vueWrapper) => {
              const lastValid = vueWrapper.emitted("valid").slice(-1)[0];

              // Last event (in)"valid" emitted
              expect(lastValid[0]).to.equal(true);
            });
          });
        });
      });

      describe("and the Issuing province is changed to Manitoba", () => {
        beforeEach(() => {
          cy.selectFormEntry("#identificationProvince", "Manitoba", false);
        });

        describe("and the ID number gets filled in with numbers and letters", () => {
          beforeEach(() => {
            cy.clearFormEntry("#clientIdentification");
            cy.fillFormEntry("#clientIdentification", "1234567A");
            cy.wait(1);
          });

          it("should emit valid true", () => {
            cy.get("@vueWrapper").should((vueWrapper) => {
              const lastValid = vueWrapper.emitted("valid").slice(-1)[0];

              // Last event (in)"valid" emitted
              expect(lastValid[0]).to.equal(true);
            });
          });

          describe("and the Issuing province is changed to BC", () => {
            beforeEach(() => {
              cy.selectFormEntry("#identificationProvince", "British Columbia", false);
              cy.wait(1);
            });

            it("should emit valid false because letters are not allowed for BC driver's licence", () => {
              cy.get("@vueWrapper").should((vueWrapper) => {
                const lastValid = vueWrapper.emitted("valid").slice(-1)[0];

                // Last event (in)"valid" emitted
                expect(lastValid[0]).to.equal(false);
              });
            });
          });
        });
      });
    });
  });
});
