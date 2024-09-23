import { Then, Step, BeforeStep } from "@badeball/cypress-cucumber-preprocessor";

let idir = true;

BeforeStep({ tags: "@loginAsBCeID or @loginAsBCSC" }, function () {
  idir = false;
});
BeforeStep({ tags: "@loginAsEditor or @loginAsViewer or @loginAsAdmin" }, function () {
  idir = true;
});

/* Text Input Steps */

Then('I type {string} into the {string} form input', (text: string, input: string) => {
  cy.contains('label', input).then(($label) => {
    cy.wrap($label.parent().parent())
      .find('input[id*="input"]')
      .then(($input) => {
        cy.wrap($input)
          .type(text)
          .should('have.value', text)
          .focus();
      });
  });
});

Then('I clear the {string} form input', (input: string) => {
  cy.contains('label', input).then(($label) => {
    cy.wrap($label.parent().parent())
      .find('input[id*="input"]')
      .clear();
  });
});

Then(
  'I type {string} into the {string} form input for the {string}',
  (value: string, fieldLabel: string, sectionTitle: string) => {

    if (sectionTitle === 'Primary location' || sectionTitle === 'Primary contact') {      
      cy.get('div.frame-01:first').within(() => {
        Step(this, `I type "${value}" into the "${fieldLabel}" form input`);        
      });
    } else {
      cy.get(`div.frame-01[data-text="${sectionTitle}"]`).within(() => {
        Step(this, `I type "${value}" into the "${fieldLabel}" form input`);        
      });
    }
});

Then('I replace the {string} with {string} form input', (input: string, text: string) => {
  cy.contains('label', input).then(($label) => {
    cy.wrap($label.parent().parent())
      .find('input[id*="input"]')
      .then(($input) => {
        cy.wrap($input)
          .clear()
          .type(text)
          .should('have.value', text)
          .focus();
      });
  });
});

Then(
  'I replace the {string} with {string} form input for the {string}',
  (fieldLabel: string, value: string, sectionTitle: string) => {

    if (sectionTitle === 'Primary location' || sectionTitle === 'Primary contact') {      
      cy.get('div.frame-01:first').within(() => {
        Step(this, `I replace the "${fieldLabel}" with "${value}" form input`);        
      });
    } else {      
      cy.get(`div.frame-01[data-text="${sectionTitle}"]`).within(() => {
        Step(this, `I replace the "${fieldLabel}" with "${value}" form input`);        
      });      
    }
});
