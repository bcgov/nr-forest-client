import { Then, Step, BeforeStep } from "@badeball/cypress-cucumber-preprocessor";

let idir = true;

BeforeStep({ tags: "@loginAsBCeID or @loginAsBCSC" }, function () {
  idir = false;
});
BeforeStep({ tags: "@loginAsEditor or @loginAsViewer or @loginAsAdmin" }, function () {
  idir = true;
});

/* Area Input Steps */

Then('I type {string} into the {string} form input area', (text: string, input: string) => {
  cy.contains('div.cds-text-input-label span', input).then(($label) => {
    cy.wrap($label.parent().parent().parent())
      .find('textarea[id*="input"]')
      .type(text);
  });
});

Then('I clear the {string} form input area', (input: string) => {
  cy.contains('div.cds-text-input-label span', input).then(($label) => {
    cy.wrap($label.parent().parent().parent())
      .find('textarea[id*="input"]')
      .clear();
  });
});

Then(
  'I type {string} into the {string} form input area for the {string}',
  (value: string, fieldLabel: string, sectionTitle: string) => {

    if (sectionTitle === 'Primary location' || sectionTitle === 'Primary contact') {      
      cy.get('div.frame-01:first').within(() => {
        Step(this, `I type "${value}" into the "${fieldLabel}" form input area`);        
      });
    } else {
      if(idir){
        cy.get('cds-accordion cds-accordion-item')
        .shadow()
        .contains('div', sectionTitle).parent().parent().within(() => {
          Step(this, `I type "${value}" into the "${fieldLabel}" form input area`);        
        });
      } else {
        cy.get(`div.frame-01[data-text="${sectionTitle}"]`).within(() => {
          Step(this, `I type "${value}" into the "${fieldLabel}" form input area`);        
        });
      }
    }

});
