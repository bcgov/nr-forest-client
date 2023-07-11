import { Then, Given } from "@badeball/cypress-cucumber-preprocessor";

Given("I am on the form page", () => {
  cy.visit('/');
});

Then("I can see the title", () => {
  cy.contains('New client application')
});
