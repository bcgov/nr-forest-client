/// <reference types="cypress-get-by-label" />
import { When, Then } from "@badeball/cypress-cucumber-preprocessor";
import "cypress-get-by-label/commands";
import "./common";

When("I select the option that says I have a BC registered business", () => {
  cy.contains("I have a BC registered business").click();
});

When("I type in the first 3 characters of the business name", function () {
  const businessNameFirst3Characters = "lum";
  cy.getByLabel("BC registered business name")
    .filter("input[type=text]")
    .type(businessNameFirst3Characters);

  this.businessNameInputValue = businessNameFirst3Characters;
});

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
    cy.get("div.autocomplete-items-cell").first().click();
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
