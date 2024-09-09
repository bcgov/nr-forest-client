import { Then, Given } from "@badeball/cypress-cucumber-preprocessor";
//This file is left intentionally empty

Then('I car read an env var', () => {
  const envVar = Cypress.env('random_var');
  cy.log(envVar);
  console.log(envVar);
});