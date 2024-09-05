/* eslint-disable no-undef */
/// <reference types="cypress" />

Cypress.Commands.add("logout", () => {
  cy.get("[data-id=logout-btn]").should("be.visible");
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