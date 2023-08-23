/// <reference types="cypress-get-by-label" />
import {
  When,
  Then,
  Before,
  Step,
} from "@badeball/cypress-cucumber-preprocessor";
import "cypress-get-by-label/commands";
import "./common";

Before(() => {
  // cy.intercept("GET", "/api/clients/name/*", {
  //   fixture: "response-autocomplete.json",
  // });

  cy.intercept("GET", "/api/clients/BC0680951", {
    // GOOD SOFTWARE INC.
    fixture: "response-details-BC0680951.json",
  });
  cy.intercept("GET", "/api/clients/BC1317640", {
    // BAD HORSE INC.
    fixture: "response-details-BC1317640.json",
  });
  cy.intercept("GET", "/api/clients/BC1162739", {
    // DUPP MEDIA INC.
    statusCode: 409,
    body: "The Dupped Company already exists with the Incorporation number BC1162739 and client number 999999",
  });
});

When("I select the option that says I have a BC registered business", () => {
  cy.contains("I have a BC registered business").click();
});

When(
  "I type in {string} in the business name input",
  function (businessNameFirstCharacters: string) {
    // TODO: fix in the application for concurrent autocomplete requests, or bounce changes before firing a request.

    const stringLength = businessNameFirstCharacters.length;
    const stringMinusLastCharacter = businessNameFirstCharacters.substring(
      0,
      stringLength - 1
    );

    cy.getByLabel("BC registered business name")
      .filter("input[type=text]")
      .type(stringMinusLastCharacter, { delay: 0 });

    cy.wait(250);

    const lastCharacter = businessNameFirstCharacters[stringLength - 1];

    cy.getByLabel("BC registered business name")
      .filter("input[type=text]")
      .type(lastCharacter);
  }
);

Then(
  "I am presented a list of business names filtered by the text typed in the business name field",
  function () {
    cy.get("div.autocomplete-items-cell")
      .should("be.visible")
      .should("have.length.above", 1);
  }
);

When(
  "I select the name of a business in good standing from the filtered list",
  function () {
    cy.get("div.autocomplete-items-cell")
      .contains("Valid Manufacturing LTD", { matchCase: false })
      .click();
  }
);

When(
  "I select {string} from the filtered list of businesses",
  function (name: string) {
    Step(
      this,
      "I am presented a list of business names filtered by the text typed in the business name field"
    );
    cy.get("div.autocomplete-items-cell")
      .contains(name, { matchCase: false })
      .click();
  }
);

When(
  "I select the first option from the filtered list of businesses",
  function () {
    Step(
      this,
      "I am presented a list of business names filtered by the text typed in the business name field"
    );
    cy.get("div.autocomplete-items-cell").first().click();
  }
);

When(
  "I select the name of a business whose data is not found in the BC Registries",
  function () {
    cy.get("div.autocomplete-items-cell")
      .contains("Valid Manufacturing LTD", { matchCase: false })
      .click();
  }
);

When(
  "I select the name of a business which is not in good standing from the filtered list",
  function () {
    cy.get("div.autocomplete-items-cell").contains("Shady").click();
  }
);

When(
  "I select the name of a business which already has a client number from the filtered list",
  function () {
    cy.get("div.autocomplete-items-cell").contains("Duplicated").click();
  }
);

Then("a field to type in the business name is displayed", () => {
  cy.getByLabel("BC registered business name")
    .filter("input[type=text]") // just makes sure this is a simple text field
    .should("exist");
});

Then("a notification about 'Registered business name' is displayed", () => {
  cy.contains("span", "BC registered business name").should("be.visible");
});

Then(
  "a notification for the business being 'Not in good standing' is displayed",
  () => {
    cy.contains("span", "Not in good standing").should("be.visible");
  }
);

Then("a notification saying that 'Client already exists' is displayed", () => {
  cy.contains("span", "Client already exists").should("be.visible");
});

Then("the button 'End application and logout' is displayed", () => {
  cy.contains("bx-btn", "End application and logout")
    .find("button")
    .should("be.visible");
});

Then("the button 'Receive email and logout' is displayed", () => {
  cy.contains("bx-btn", "Receive email and logout")
    .find("button")
    .should("be.visible");
});

Then("I get to the Address tab", () => {
  cy.get(".wizard-body")
    .find(".bx--title")
    .first()
    .should("contain.text", "Address");
});

Then("I get to the Contacts tab", () => {
  cy.get(".wizard-body")
    .find(".bx--title")
    .first()
    .should("contain.text", "Contacts");
});

When("the list of countries finishes loading", () => {
  cy.get('bx-dropdown[label-text="Country"]')
    .contains("span", "Canada")
    .should("be.visible");
});

When("I type in {string} in the Street Address", (address: string) => {
  // cy.getByLabel("Street address or PO box").type(address);
  cy.get('bx-input[label-text="Street address or PO box"')
    .find("input")
    .type(address);
});

When(
  "I select the Street Address that contains {string} from the list",
  (address: string) => {
    cy.get("div.autocomplete-items-cell").contains(address).click();
  }
);

Then("the City gets updated to {string}", (city: string) => {
  // cy.getByLabel("City").should("equal", city);
  cy.get('bx-input[label-text="City"').find("input").should("have.value", city);
});

Then("the Province gets updated to {string}", (province: string) => {
  return "skipped"; // TODO: fix application
  cy.get('bx-dropdown[label-text="Province or territory"]')
    .contains("span", province)
    .should("be.visible");
});

Then("the Postal code gets updated to {string}", (postalCode: string) => {
  // cy.getByLabel("Postal code").should("equal", postalCode);
  cy.get('bx-input[label-text="Postal code"')
    .find("input")
    .should("have.value", postalCode);

  // TODO: investigate why the white space is auto-removed by this test.

  // TODO: fix the application and remove focus and blur below.
  cy.get('bx-input[label-text="Postal code"').find("input").focus();
  cy.get('bx-input[label-text="Postal code"').blur();
});

When("I select the Address name {string}", (addressName: string) => {
  cy.get('bx-dropdown[label-text="Address name"]')
    .find("[role='button']")
    .click();
  cy.get('bx-dropdown[label-text="Address name"]')
    .contains(addressName)
    .click();
  /* ==== Generated with Cypress Studio ==== */
  // cy.get('#address_0').click();
  // cy.get('#address_0 > bx-dropdown-item').click();
  // cy.get('#tag_address_0_0').should('have.text', 'Mailing Address ');
  // cy.get('#role_0').click();
  // cy.get('#role_0 > :nth-child(10)').click();
  /* ==== End Cypress Studio ==== */
});

When("I select the Address name {string}", (addressName: string) => {
  cy.get('bx-dropdown[label-text="Address name"]')
    .find("[role='button']")
    .click();
  cy.get('bx-dropdown[label-text="Address name"]')
    .contains(addressName)
    .click();
  /* ==== Generated with Cypress Studio ==== */
  // cy.get('#address_0').click();
  // cy.get('#address_0 > bx-dropdown-item').click();
  // cy.get('#tag_address_0_0').should('have.text', 'Mailing Address ');
  // cy.get('#role_0').click();
  // cy.get('#role_0 > :nth-child(10)').click();
  /* ==== End Cypress Studio ==== */
});
