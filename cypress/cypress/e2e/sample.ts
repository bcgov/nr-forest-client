import { Then, Given, Step } from "@badeball/cypress-cucumber-preprocessor";
//This file is left intentionally empty

Then('I car read an env var', () => {
  const envVar = Cypress.env('random_var');
  Step(this, 'I click on the "Log in with IDIR" button');
  cy.get("#user").type(envVar, { log: false });
  cy.logAndScreenshot(envVar);
  console.log(`Random var: ${envVar}`);
});