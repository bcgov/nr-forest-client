import { Then, Given, When, BeforeAll } from "@badeball/cypress-cucumber-preprocessor";

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
  cy.contains(title)
});

const buttonClick = (name: string, kinds: string[]) => {
  if (kinds.length === 0) {
    throw new Error(`Button with label "${name}" not found.`);
  }

  let targetElement : JQuery<HTMLBodyElement> = null;

  cy.get('body').then((body) => {
    kinds.forEach((kind) => {
      // Find all elements that match the selector type
      targetElement = body.find(kind).filter((index, element) =>{
        return Cypress.$(element).html().includes(name) || 
        Cypress.$(element).text().includes(name) || 
        Cypress.$(element).val().toString().includes(name);
      });
      
      if (targetElement.length > 0) {
        // If a matching element with the correct text is found, click it
        cy.wrap(targetElement).click();
      }
    })

  });
}

When('I click on the {string} button', (name: string) => {
  buttonClick(name, ['input', 'button', 'cds-button', 'cds-side-nav-link']);
});

Then('I cannot see {string}', (button: string) => {
  cy.contains(button).should('not.exist');
});

Then('I clear the {string} form input area', (input: string) => {
  cy.contains('div.cds-text-input-label span', input).then(($label) => {
    cy.wrap($label.parent().parent().parent())
      .find('textarea[id*="input"]')
      .clear();
  });
});

Then('I type {string} into the {string} form input area', (text: string, input: string) => {
  cy.contains('div.cds-text-input-label span', input).then(($label) => {
    cy.wrap($label.parent().parent().parent())
      .find('textarea[id*="input"]')
      .type(text);
  });
});

Then('I type {string} into the {string} form input', (text: string, input: string) => {
  cy.contains('label', input).then(($label) => {
    cy.wrap($label.parent().parent())
      .find('input[id*="input"]')
      .type(text);
  });
});

Then('I clear the {string} form input', (input: string) => {
  cy.contains('label', input).then(($label) => {
    cy.wrap($label.parent().parent())
      .find('input[id*="input"]')
      .clear();
  });
});

Then('I select {string} from the {string} form input', (value: string, input: string) => {
  cy.contains('label', input).then(($label) => {
    const parentShadow = $label[0].getRootNode();
    cy.wrap(parentShadow).find("[part='trigger-button']").click();
    cy.wrap(parentShadow).parent().find(`cds-combo-box-item[data-value="${value}"]`).click();
  });
});

Then('I type {string} and select {string} from the {string} form autocomplete', (search: string, value: string, input: string) => {

  cy.contains('label', input).then(($label) => {
    const parentShadow = $label[0].getRootNode();
    cy.wrap(parentShadow).find('input').type(search);
    cy.wrap(parentShadow).parent().find(`cds-combo-box-item[data-value="${value}"]`).click();
  });

});

Then('I wait for the text {string} to appear', (text: string) => {  
  cy.get(text).should('be.visible');
});