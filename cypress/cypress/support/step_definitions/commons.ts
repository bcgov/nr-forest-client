import { Then, Step, BeforeStep } from "@badeball/cypress-cucumber-preprocessor";

let idir = true;

BeforeStep({ tags: "@loginAsBCeID or @loginAsBCSC" }, function () {
  idir = false;
});
BeforeStep({ tags: "@loginAsEditor or @loginAsViewer or @loginAsAdmin" }, function () {
  idir = true;
});

/* Extra Actions */

Then('I add a new location called {string}', (location: string) => {
  Step(this,'I click on the "Add another location" button');
  cy.get(`[data-text="Additional location"]`).should('be.visible');
  Step(this,`I type "${location}" into the "Location name" form input for the "Additional location"`);  
  cy.get(`[data-text="${location}"]`).should('be.visible');
});

Then('I add a new contact called {string}', (contactName: string) => {
  Step(this,'I click on the "Add another contact" button');

    cy.get('[data-text="Additional contact"]').should('be.visible');
    const [firstName, ...lastName] = contactName.split(' ');
    Step(this,`I type "${firstName}" into the "First name" form input for the "Additional contact"`);  
    cy.get(`[data-text="${firstName} "]`).should('be.visible');
    Step(this,`I type "${lastName.join(' ')}" into the "Last name" form input for the "${firstName} "`);  

});

/* Error messages */

Then('I should see the {string} message {string} on the {string}', (kind: string, message: string, location: string) => {
  checkForActionableNotification(message, location, kind);
});

Then(
  'The field {string} should have the {string} message {string}', 
  (field: string, kind: string, message: string) => {

    cy.contains('label', field).then(($label) => {      
      console.log('Shadow root: ', $label[0].getRootNode());
      if(kind === 'error') {
      cy.wrap($label[0].getRootNode())
        .find('#invalid-text')
        .invoke('text')
        .should('contains', message);
      } else {
        cy.wrap($label[0].getRootNode())
        .find('.cds--form-requirement')
        .invoke('text')
        .should('contains', message);
      }
    });

});

/* This block is dedicated to the actual code */

const checkForActionableNotification = (message: string, location: string, kind: string) => {
  let errorLookupTag = '';
  if(location.toLowerCase().includes("top")){
    errorLookupTag = `cds-actionable-notification[id="fuzzy-match-notification-global"][kind="${kind}"] div span.body-compact-01`;
  } else if(idir) {
    errorLookupTag = `cds-inline-notification[data-text="${location}"][kind="${kind}"] div span.body-compact-01`;
  } else {
    errorLookupTag = `cds-inline-notification[data-text="${location}"][kind="${kind}"]`;
  }

  cy.get(errorLookupTag)
  .should('exist')
  .should('contain.text', message);
}
