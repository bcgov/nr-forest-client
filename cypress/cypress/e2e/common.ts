/// <reference types="cypress-get-by-label" />
import { Given, Then, When } from "@badeball/cypress-cucumber-preprocessor";
import "cypress-get-by-label/commands";

Given("I navigate to the client application form", () => {
  cy.visit("/");
  cy.wait(500);
  cy.login("uattest@forest.client", "Uat Test", "bceidbusiness");
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

When("I click the button 'End application and logout'", () => {
  cy.contains("bx-btn", "End application and logout").click();
});

When("I click the button 'Receive email and logout'", () => {
  cy.contains("bx-btn", "Receive email and logout").click();
});

Then("I am redirected to the landing page", () => {
  // allow some time to follow two redirections
  cy.wait(100);

  cy.location("pathname").should("equal", "/landing");
});
