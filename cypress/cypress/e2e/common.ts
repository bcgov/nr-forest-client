/// <reference types="cypress-get-by-label" />
import {
  Before,
  Given,
  Then,
  When,
} from "@badeball/cypress-cucumber-preprocessor";
import "cypress-get-by-label/commands";

export interface CustomWorld extends Mocha.Context {
  appLocation: Location;
}

/**
 * Utility to allow using cross environment URL as the backend if the current environment's backend is not available.
 * @param this
 * @param backendUrl - The backend URL whose responses are to be padded. Should match your VITE_BACKEND_URL configuration.
 */
function allowCrossEnvironments(
  this: CustomWorld,
  backendUrl = "https://nr-forest-client-test-backend.apps.silver.devops.gov.bc.ca"
) {
  cy.intercept(
    {
      url: `${backendUrl}/**`,
    },
    // Make CORS adjustment for all incoming responses
    (req) =>
      req.continue((res) => {
        res.headers["Access-Control-Allow-Origin"] = this.appLocation.origin;
      })
  );
}

Before(function (this: CustomWorld) {
  // allowCrossEnvironments.apply(this);
  cy.intercept("GET", "/logout").as("logout");
});

Given(
  "I navigate to the client application form",
  function (this: CustomWorld) {
    cy.visit("/");
    cy.location().then((location) => {
      this.appLocation = location;
    });
    cy.login("uattest@forest.client", "Uat Test", "bceidbusiness");
  }
);

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

Then("I am redirected to the logout route", () => {
  cy.wait("@logout"); // the route has been hit
});
