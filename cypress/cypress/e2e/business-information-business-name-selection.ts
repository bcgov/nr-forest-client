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
  // cy.contains("bx-btn", "Next").should("have.attr", "disabled");

  cy.contains("bx-btn", "Next").find("button").should("be.disabled");
});

Then("the button Next is enabled", () => {
  // The web component does not have a property called disabled, only a corresponding attribute.
  // cy.contains("bx-btn", "Next").should("not.have.attr", "disabled");

  cy.contains("bx-btn", "Next").find("button").should("be.enabled");
});

Then("the button Next is hidden", () => {
  cy.contains("bx-btn", "Next").should("not.exist");
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

When("I select the option that says I have a BC registered business", () => {
  cy.contains("I have a BC registered business").click();
});

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
