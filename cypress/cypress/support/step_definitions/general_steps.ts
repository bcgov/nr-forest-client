import { Given, Then, BeforeStep } from "@badeball/cypress-cucumber-preprocessor";
BeforeStep(() => { cy.wait(10); });

Given('I visit {string}', (url: string) => {
  cy.visit(url).then(() => {
    cy.window().then((win) => {
      return new Cypress.Promise((resolve) => {
        if (win.document.readyState === 'complete') {
          resolve();
        } else {
          win.addEventListener('load', resolve);
        }
      });
    });
  });
});

Then('I can read {string}', (title: string) => {
  cy.contains(title).should('be.visible');
});

Then('I cannot see {string}', (button: string) => {
  cy.contains(button).should('not.exist');
});

Then('I wait for the text {string} to appear', (text: string) => {
  cy.contains(text).should('be.visible');
});

Then('I wait for the text {string} to appear after {string}', (text: string,waitFor: string) => {
  cy.wait(`@${waitFor}`,{ timeout: 10 * 1000 });
  cy.contains(text).should('be.visible');
});

Then('I am a {string} user', (userType: string) => {});
