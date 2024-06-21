import type { StaticResponse } from "../../node_modules/cypress/types/net-stubbing";

/* eslint-disable no-undef */
describe("Staff Form", () => {
  
  beforeEach(() => {

    cy.visit("/");
    cy.wait(500);
    cy.get("#landing-title").should("contain", "Forests Client Management System");
    cy.get("#landing-subtitle").should(
      "contain",
      "Create and manage client accounts"
    );

    cy.viewport(1920, 1080);
  });

  it("CLIENT_EDITOR should be able to see the button", () => {

    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_EDITOR"],
    });

    // Check if the Create client button is visible
    cy.get('#menu-list-staff-form')
      .should('be.visible')
      .find('span')
      .should('contain', 'Create client');
    
  });

  it("CLIENT_ADMIN should be able to see the button", () => {

    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_ADMIN"],
    });

    // Check if the Create client button is visible
    cy.get('#menu-list-staff-form')
      .should('be.visible')
      .find('span')
      .should('contain', 'Create client');
    
  });

  it("CLIENT_VIEWER should not be able to see the button", () => {

    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_VIEWER"],
    });

    // Check if the Create client button is not visible and cannot be found
    cy.get('#menu-list-staff-form').should('not.exist');
    
  });

  it("CLIENT_EDITOR should be able to click the button", () => {

    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_EDITOR"],
    });

    // Check if the Create client button is visible
    cy.get('#menu-list-staff-form')
      .should('be.visible')
      .click();

      cy.get('h1')
      .should('be.visible')      
      .should('contain', ' Create client ');
    
  });

  it("CLIENT_ADMIN should be able to click the button", () => {

    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_ADMIN"],
    });

    // Check if the Create client button is visible
    cy.get('#menu-list-staff-form')
      .should('be.visible')
      .click();

      cy.get('h1')
      .should('be.visible')      
      .should('contain', ' Create client ');
    
  });

  describe("when the user clicks the Create client button", () => {
    beforeEach(() => {
      cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
        given_name: "James",
        family_name: "Baxter",
        "cognito:groups": ["CLIENT_ADMIN"],
      });

      // Check if the Create client button is visible
      cy.get('#menu-list-staff-form')
        .should('be.visible')
        .click();
    });

    it("should display the Client type input field", () => {
      cy.get("#clientType").should("be.visible").and("have.value", "");
    });

    it("should not display any Client type specific input fields", () => {
      cy.get("#firstName").should("not.exist");
    });

    describe("when option Individual gets selected", () => {
      beforeEach(() => {
        cy.get("#clientType")
          .should("be.visible")
          .and("have.value", "")
          .find("[part='trigger-button']")
          .click();

        cy.get("#clientType")
          .find('cds-combo-box-item[data-id="I"]')
          .should("be.visible")
          .click()
          .and("have.value", "Individual");
      });
      it("should display the Individual information input fields", () => {
        cy.contains("h2", "Client information");
        cy.get("#firstName").should("be.visible");
        cy.get("#middleName").should("be.visible");
        cy.get("#lastName").should("be.visible");
        cy.get("#birthdate").should("be.visible");
        cy.get("#identificationType").should("be.visible");
        cy.get("#clientIdentification").should("be.visible");
      });

      describe("when all the required information is filled in", () => {
        const baseData = {
          firstName: "John",
          middleName: "Michael",
          lastName: "Silver",
          birthdateYear: "2001",
          birthdateMonth: "05",
          birthdateDay: "30",
          identificationTypeValue: "Canadian passport",
          identificationProvinceValue: undefined,
          clientIdentification: "AB345678",
        };
        const scenarios = [
          {
            name: "and the selected ID type doesn't require Issuing province",
            data: {
              ...baseData,
            },
          },
          {
            name: "and the selected ID type requires Issuing province",
            data: {
              ...baseData,
              identificationTypeValue: "Canadian driver's licence",
              identificationProvinceValue: "Nova Scotia",
            },
          },
        ];
        scenarios.forEach(({ name, data }) => {
          describe(name, () => {
            beforeEach(() => {
              cy.get("#firstName").shadow().find("input").type(data.firstName);

              cy.get("#middleName").shadow().find("input").type(data.middleName);

              cy.get("#lastName").shadow().find("input").type(data.lastName);

              cy.get("#birthdateYear").shadow().find("input").type(data.birthdateYear);
              cy.get("#birthdateMonth").shadow().find("input").type(data.birthdateMonth);
              cy.get("#birthdateDay").shadow().find("input").type(data.birthdateDay);

              cy.get("#identificationType").find("[part='trigger-button']").click();
              cy.get("#identificationType")
                .find(`cds-combo-box-item[data-value="${data.identificationTypeValue}"]`)
                .click();

              if (data.identificationProvinceValue) {
                cy.get("#identificationProvince").find("[part='trigger-button']").click();
                cy.get("#identificationProvince")
                  .find(`cds-combo-box-item[data-value="${data.identificationProvinceValue}"]`)
                  .click();
              }

              cy.get("#clientIdentification")
                .shadow()
                .find("input")
                .type(data.clientIdentification);

              cy.get("#clientIdentification").shadow().find("input").blur();
            });
            it("enables the button Next", () => {
              cy.get("[data-test='wizard-next-button']")
                .shadow()
                .find("button")
                .should("be.enabled");
            });

            describe("and the button Next is clicked", () => {
              beforeEach(() => {
                cy.get("[data-test='wizard-next-button']").click();
              });
              it("hides the Client information section", () => {
                cy.contains("h2", "Client information").should("not.exist");
              });
              describe("and the button Back is clicked", () => {
                beforeEach(() => {
                  cy.get("[data-test='wizard-back-button']").click();
                });
                it("renders the Individual input fields with the same data", () => {
                  cy.get("#firstName").shadow().find("input").should("have.value", data.firstName);

                  cy.get("#middleName")
                    .shadow()
                    .find("input")
                    .should("have.value", data.middleName);

                  cy.get("#lastName").shadow().find("input").should("have.value", data.lastName);

                  cy.get("#birthdateYear")
                    .shadow()
                    .find("input")
                    .should("have.value", data.birthdateYear);
                  cy.get("#birthdateMonth")
                    .shadow()
                    .find("input")
                    .should("have.value", data.birthdateMonth);
                  cy.get("#birthdateDay")
                    .shadow()
                    .find("input")
                    .should("have.value", data.birthdateDay);

                  cy.get("#identificationType").should("have.value", data.identificationTypeValue);

                  if (data.identificationProvinceValue) {
                    cy.get("#identificationProvince").should(
                      "have.value",
                      data.identificationProvinceValue,
                    );
                  }

                  cy.get("#clientIdentification")
                    .shadow()
                    .find("input")
                    .should("have.value", data.clientIdentification);
                });
              });
            });
          });
        });
      });
    });
  });
});
