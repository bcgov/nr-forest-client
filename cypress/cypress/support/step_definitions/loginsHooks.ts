import { Before, Step } from '@badeball/cypress-cucumber-preprocessor';

const doLogin = (kind: string, afterLoginLocation: string, extraLandingParam: string = null) => {

  const username = Cypress.env(`${kind}_username`);
  const password = Cypress.env(`${kind}_password`);

  if(!username || !password) {
    throw new Error(`Username or password for ${kind} not found.`);
  }
  
  cy.session(
    `${kind}-${username}`,
    () => {
      const landingPage = extraLandingParam ? `/landing?${extraLandingParam}` : '/landing';
      // Visit the landing page
      Step(this, `I visit "${landingPage}"`);
      
      // Click on the login button
      if(kind !== 'bceid' && kind !== 'bcsc') {
        cy.waitForPageLoad('img');
        Step(this, 'I click on the "Log in with IDIR" button');
      } else if(kind === 'bceid') {
        cy.waitForPageLoad('span#bceidLogo');
      }

      // Log into the application, not using a step here to prevent password spillage
      cy.get("#user").type(username, { log: false });
      cy.get("#password").type(password, { log: false });
      Step(this, 'I click on the "Continue" button');

      // Validate the login for session purposes
      cy.url().should('include', afterLoginLocation);      
      cy.getCookies().then((cookies) => {
        cookies.forEach((cookie) => cy.setCookie(cookie.name, cookie.value));
      });
    },
    {
      validate: () => {
        cy.request(afterLoginLocation).its('status').should('eq', 200);
        cy.visit(afterLoginLocation);        
      },
    });
    cy.visit(afterLoginLocation);
    
}

Before({ tags: '@loginAsEditor' }, () => {  
  doLogin('editor','/search');
  cy.waitForPageLoad('cds-header');
});

Before({ tags: '@loginAsAdmin' }, () => {  
  doLogin('admin','/search');
  cy.waitForPageLoad('cds-header');
});

Before({ tags: '@loginAsViewer' }, () => {  
  doLogin('viewer','/search');
  cy.waitForPageLoad('cds-header');
});

Before({ tags: '@loginAsBCeID' }, () => {  
  doLogin('bceid','/new-client','ref=external');
  cy.waitForPageLoad('cds-header');
});

Before({ tags: '@loginAsBCSC' }, () => {  
  doLogin('bcsc','/new-client-bcsc','ref=individual');
  cy.waitForPageLoad('cds-header');
});