/// <reference types="cypress-get-by-label" />
import { Given, When, Then } from "@badeball/cypress-cucumber-preprocessor";
import "cypress-get-by-label/commands";

interface CustomWorld extends Mocha.Context {
  businessNameInputValue: string;
}

Given("I navigate to the client application form", () => {
  cy.visit("/");
  cy.wait(500);
  cy.login("uattest@forest.client", "Uat Test", "bcsc");
});

Then(
  "the links for all the next steps presented in the breadcrumbs are all disabled",
  () => {
    cy.get(".bx--progress-step").should("have.lengthOf", 4);

    cy.get(".bx--progress-step").each(($el, index) => {
      if (index === 0) {
        // There is already a link for the first step.
        cy.wrap($el).find("a").should("exist");
        return;
      }
      // The other steps have no links yet.
      cy.wrap($el).find("a").should("not.exist");
    });
  }
);

Then("the button Next is disabled", () => {
  // The web component does not have a property called disabled, only a corresponding attribute.
  cy.contains("bx-btn", "Next").should("have.attr", "disabled");
});

When("I select the option that says I have a BC registered business", () => {
  cy.contains("I have a BC registered business").click();
});

Then("a field to type in the business name is displayed", () => {
  cy.getByLabel("BC registered business name")
    .filter("input[type=text]") // just makes sure this is a simple text field
    .should("exist");
});

Then("a notification about 'Registered business name' is displayed", () => {
  cy.contains("span", "BC registered business name");
});

When(
  "I type in the first 3 characters of the business name",
  function (this: CustomWorld) {
    const businessNameFirst3Characters = "lum";
    cy.getByLabel("BC registered business name")
      .filter("input[type=text]")
      .type(businessNameFirst3Characters);

    this.businessNameInputValue = businessNameFirst3Characters;
  }
);

Then(
  "I am presented a list of business names filtered by the text typed in the business name field",
  function (this: CustomWorld) {
    cy.get("div.autocomplete-items-cell")
      .should("be.visible")
      .should("have.length.above", 1);
  }
);
