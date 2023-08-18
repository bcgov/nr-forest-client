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
  function (name: string) {
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
