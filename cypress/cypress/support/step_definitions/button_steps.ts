import { When, BeforeStep } from "@badeball/cypress-cucumber-preprocessor";

let idir = true;

BeforeStep({ tags: "@loginAsBCeID or @loginAsBCSC" }, function () {
  idir = false;
});
BeforeStep({ tags: "@loginAsEditor or @loginAsViewer or @loginAsAdmin" }, function () {
  idir = true;
});

/* Button Step */

When('I click on the {string} button', (name: string) => {
  buttonClick(name, ['input', 'button', 'cds-button', 'cds-modal-footer-button', 'cds-side-nav-link']);
});

When('I click on next', () => {  
  
  if (!idir) {
    cy.get('cds-button[data-text="Next"]').click().then(() => {cy.wait(15);});
  } else {
    cy.intercept('POST',  `**/api/clients/matches`).as('matches');  
    cy.get('cds-button[data-text="Next"]').click().then(() => {cy.wait('@matches',{ timeout: 10 * 1000 });});
  }
  
});

When('I submit', () => {
  if(idir){
  cy.intercept('POST',  `**/api/clients/submissions/staff`).as('submit');
  } else {
    cy.intercept('POST',  `**/api/clients/submissions`).as('submit');
  }
  cy.get('cds-button[data-text="Submit"]').scrollIntoView().click().then(() => {cy.wait('@submit',{ timeout: 60 * 1000 });});  
});


const buttonClick = (
  name: string, 
  kinds: string[], 
  waitForIntercept: string = null, 
  waitForTime : number = 15,  
  selector: string = 'body'
) => {
  if (kinds.length === 0) {
    throw new Error(`Button with label "${name}" not found.`);
  }

   // Build a selector string that matches any of the button kinds
  const kindSelector = kinds.join(',');

  cy.get(selector)
    .find(kindSelector)
    .filter(':visible') // Only consider visible buttons
    .filter((index, element) => {
      // Check for the button label in various places
      return Cypress.$(element).attr('data-text')?.includes(name) ||
            Cypress.$(element).html().includes(name) ||
            Cypress.$(element).text().includes(name) ||
            Cypress.$(element).val()?.toString().includes(name);
    })
    .first() // Get the first matching, visible button
    .should('be.visible') // Ensure it's visible before clicking
    .click() // Click the button
    .then(() => {
      // Handle waiting for intercept or time after clicking
      if (waitForIntercept) {
        cy.wait(`@${waitForIntercept}`, { timeout: waitForTime * 1000 });
      } else if (waitForTime) {
        cy.wait(waitForTime);
      }
    });
}
