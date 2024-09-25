import { Then, Step, BeforeStep } from "@badeball/cypress-cucumber-preprocessor";

let idir = true;

BeforeStep({ tags: "@loginAsBCeID or @loginAsBCSC" }, function () {
  idir = false;
});
BeforeStep({ tags: "@loginAsEditor or @loginAsViewer or @loginAsAdmin" }, function () {
  idir = true;
});

/* Autocomplete Input Steps */

Then('I type {string} and select {string} from the {string} form autocomplete', (search: string, value: string, input: string) => {

  if(input === 'Client name' || input === 'BC registered business name') {
    cy.intercept('GET',  `**/api/clients/**`).as('autocomplete');
    cy.intercept('GET',  `**/api/opendata/**`).as('autocomplete');
  } else if(input === 'Street address or PO box') {
    cy.intercept('GET',  `**/api/address**`).as('autocomplete');
  }

  cy.contains('label', input).then(($label) => {
    const parentShadow = $label[0].getRootNode();
    cy.wrap(parentShadow)
      .find('input')
      .type(search, { delay: 150 })
      .then(() => {
        cy.wait('@autocomplete');
        cy.wrap(parentShadow)
          .parent()
          .find(`cds-combo-box-item[data-value="${value}"], cds-combo-box-item[data-value^="${value}"]`)
          .first()
          .then(($item) => {
            if ($item.length) {
              cy.wrap($item).click();
            } else {
              throw new Error(`Item with value "${value}" not found.`);
            }
          })
          .then(() => {
            cy.wait('@autocomplete');
          });
      });
  });
});

Then(
  'I type {string} and select {string} from the {string} form autocomplete for the {string}',
  (search: string, value: string, fieldLabel: string, sectionTitle: string) => {

    if (sectionTitle === 'Primary location' || sectionTitle === 'Primary contact') {
      cy.get('div.frame-01:first').within(() => {
        Step(this, `I type "${search}" and select "${value}" from the "${fieldLabel}" form autocomplete`);        
      });
    } else {
      cy.get(`[data-text="${sectionTitle}"]`).within(() => {
        Step(this, `I type "${search}" and select "${value}" from the "${fieldLabel}" form autocomplete`);        
      });      
    }

});