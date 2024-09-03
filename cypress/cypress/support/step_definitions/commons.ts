import { Then, Given, When } from "@badeball/cypress-cucumber-preprocessor";

Given('I visit {string}', (url: string) => {
  cy.visit(url);
});

Then('I can see the title {string}', (title: string) => {
  cy.contains(title)
});

When('I click on the {string} button', (button: string) => {
  cy.contains(button).click();
});

When('I click on the {string} cds-button', (button: string) => {
  cy.contains('cds-button', button).should('exist').click();
});

Then('I cannot see the {string} cds-button', (button: string) => {  
  cy.contains('cds-button', button).should('not.exist');
});

Then('I see the {string} cds-button', (button: string) => {  
  cy.contains('cds-button', button).should('exist');
});

Then('I navigate to {string}', (url: string) => {
  cy.visit(url);
});

Then('I type {string} into the {string} input', (text: string, input: string) => {
  cy.get(input).type(text);
});

Then('I type {string} secretly into the {string} input', (text: string, input: string) => {
  cy.get(input).type(text, { log: false });
});

Then('I type {string} into the {string} form input', (text: string, input: string) => {
  cy.fillFormEntry(input, text, 10, false);
});

Then('I type {string} into the {string} form input area', (text: string, input: string) => {
  cy.fillFormEntry(input, text, 10, true);
});

Then('I clear the {string} form input', (input: string) => {
  cy.clearFormEntry(input, false);
});

Then('I clear the {string} form input area', (input: string) => {
  cy.clearFormEntry(input, true);
});

Then('I select {string} from the {string} form input', (value: string, input: string) => {
  cy.selectFormEntry(input, value, false);
});