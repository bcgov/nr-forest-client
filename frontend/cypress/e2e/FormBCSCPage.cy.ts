/* eslint-disable no-undef */
describe("BCSC Form", () => {
  beforeEach(() => {
    cy.intercept("http://localhost:8080/api/clients/name/*", {
      fixture: "business.json",
    }).as("searchCompany");

    cy.intercept("GET", "/api/clients/XX9016140", {
      fixture: "example.json",
    }).as("selectCompany");

    cy.intercept("GET","/api/clients/getCountryByCode/CA",{
      fixture: "countryCodeCA.json",
    }).as("getCanadaByCode");

    cy.intercept("POST", "/api/clients/submissions", {
      statusCode: 201,
      headers: { 
        location: "http://localhost:8080/api/clients/submissions/1",
        "x-sub-id": "123456" 
      },
    }).as("submitForm");

    cy.visit("/");
    cy.wait(500);
  });

  it("should submit the form with a phone number", () => {

    cy.get("#landing-title").should("contain", "Client Management System");

    cy.get("#landing-subtitle").should(
      "contain",
      "Create and manage client accounts"
    );


    cy.login("uattest@gov.bc.ca", "Uat Test", "ca.bc.gov.flnr.fam.dev",
    {
      address: {
        formatted: "{\"street_address\":\"4000 SEYMOUR PLACE\",\"country\":\"CA\",\"locality\":\"VICTORIA\",\"region\":\"BC\",\"postal_code\":\"V8Z 1C8\"}"
      },
      given_name: "James",
      family_name: "Baxter",
      birthdate: "1986-11-12"
    }
    );

    cy
    .get('#phoneNumberId')
    .should("be.visible")
      .shadow()
      .find("input")
      .should("have.value", "")
      .type('2503008326');

      cy
    .get('#phoneNumberId')
    .blur();

    cy.get('[data-test="wizard-submit-button"]')
    .click();

    cy.get('.fluid-heading-05').should('contain', 'Application submitted!');
  });
});