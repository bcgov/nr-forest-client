/* eslint-disable no-undef */
/// <reference types="cypress" />


Cypress.Commands.add("logout", () => {
  cy.get("[data-id=logout-btn]").should("be.visible");
});

Cypress.Commands.add("getMany", (names: string[]): Cypress.Chainable<any[]> => {
  const values: any[] = [];

  for (const arg of names) {
    cy.get(arg).then((value) => values.push(value));
  }

  return cy.wrap(values);
});

Cypress.Commands.add("fillFormEntry",(field: string, value: string, delayMS: number = 10, area: boolean = false) =>{
  cy.get(field)
  .should("exist")
  .shadow()
  .find(area ? "textarea" : "input")  
  .type(value,{ delay: delayMS });
});

Cypress.Commands.add("clearFormEntry",(field: string, area: boolean = false) =>{
  cy.get(field)
  .should("exist")
  .shadow()
  .find(area ? "textarea" : "input")
  .focus()
  .clear()
  .blur();
});

Cypress.Commands.add("selectFormEntry", (field: string, value: string, box: boolean) => {
  cy.get(field).find("[part='trigger-button']").click();

  if (!box) {
    cy.get(field).find(`cds-combo-box-item[data-value="${value}"]`).click();
  } else {
    cy.get(field)
      .find(`cds-multi-select-item[data-value="${value}"]`)
      .click();
    cy.get(field).click();
  }
});

Cypress.Commands.add("selectAutocompleteEntry", (field: string, value: string, dataid: string,delayTarget: string = '') => {
  cy.get(field).should("exist").shadow().find("input").type(value);
  if(delayTarget)
    cy.wait(delayTarget);
  else
    cy.wait(10);
  cy.get(field).find(`cds-combo-box-item[data-id="${dataid}"]`).click();
});

Cypress.Commands.add("checkInputErrorMessage", (field: string, message: string) => {
  cy.get(field)
  .shadow()
  .find('#invalid-text')
  .invoke('text')
  .should('contains',message);
});

Cypress.Commands.add("checkAutoCompleteErrorMessage", (field: string, message: string) => {
  cy.get(field)          
      .should('have.attr', 'aria-invalid', 'true')
      .should('have.attr', 'invalid-text', message);

      cy.get(field)
      .shadow()
      .find('svg').should('exist');

      cy.get(field)
      .shadow()
      .find('div.cds--form__helper-text > slot#helper-text')
      .invoke('text')
      .should('contains', message);
});

Cypress.Commands.add("checkAccordionItemState", (additionalSelector: string, open: boolean) => {
  cy.get(`cds-accordion-item${additionalSelector}`).should(
    `${open ? "" : "not."}have.attr`,
    "open",
  );
});

Cypress.Commands.add('waitForPageLoad', (element: string) => {
  cy.get(element).should('be.visible').then(() => {
    cy.log('Page loaded');
  });
});