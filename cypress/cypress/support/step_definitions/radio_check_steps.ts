import { Then, BeforeStep } from "@badeball/cypress-cucumber-preprocessor";

let idir = true;

BeforeStep({ tags: "@loginAsBCeID or @loginAsBCSC" }, function () {
  idir = false;
});
BeforeStep({ tags: "@loginAsEditor or @loginAsViewer or @loginAsAdmin" }, function () {
  idir = true;
});

/* Radio and Check Input Steps */

Then('I mark {string} on the {string} {string} input', (value: string, input: string, kind: string) => {
  cy.get(`cds-radio-button-group[legend-text="${input}"]`)
    .find(`cds-radio-button[label-text="${value}"]`)
    .click();
});
