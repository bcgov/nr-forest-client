import { Before, Step } from '@badeball/cypress-cucumber-preprocessor';

const doLogin = (kind: string) => {

  const username = Cypress.env(`${kind}_username`);
  const password = Cypress.env(`${kind}_password`);

  if(!username || !password) {
    throw new Error(`Username or password for ${kind} not found.`);
  }

  cy.session(
    `${kind}-${username}`,
    () => {
      // Visit the landing page
      Step(this, 'I visit "/landing"');
      cy.waitForPageLoad('img');

      // Click on the login button
      Step(this, 'I click on the "Log in with IDIR" button');

      // Log into the application, not using a step here to prevent password spillage
      cy.get("#user").type(username, { log: false });
      cy.get("#password").type(password, { log: false });
      Step(this, 'I click on the "Continue" button');

      // Validate the login for session purposes
      cy.url().should('include', '/submissions');      
      cy.getCookies().then((cookies) => {
        cookies.forEach((cookie) => cy.setCookie(cookie.name, cookie.value));
      });
    },
    {
      validate: () => {
        cy.request('/submissions').its('status').should('eq', 200);
        cy.visit('/submissions');        
      },
    });
    cy.visit('/submissions');
    
}

Before({ tags: '@loginAsEditor' }, () => {  
  doLogin('editor');
  cy.waitForPageLoad('cds-header');
});

Before({ tags: '@loginAsAdmin' }, () => {  
  doLogin('admin');
  cy.waitForPageLoad('cds-header');
});

Before({ tags: '@loginAsViewer' }, () => {  
  doLogin('viewer');
  cy.waitForPageLoad('cds-header');
});
