import { Then, Given, When, DataTable, Step, BeforeStep } from "@badeball/cypress-cucumber-preprocessor";

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

/* Button Step */

When('I click on the {string} button', (name: string) => {
  buttonClick(name, ['input', 'button', 'cds-button', 'cds-side-nav-link']);
});

When('I submit', () => {
  buttonClick('Create client', ['input', 'button', 'cds-button', 'cds-side-nav-link'],'div.form-footer-group-buttons');
});

/* Text Input Steps */

Then('I type {string} into the {string} form input', (text: string, input: string) => {
  cy.contains('label', input).then(($label) => {
    cy.wrap($label.parent().parent())
      .find('input[id*="input"]')
      .then(($input) => {
        cy.wrap($input)
          .type(text)
          .should('have.value', text)
          .focus();
      });
  });
});

Then('I clear the {string} form input', (input: string) => {
  cy.contains('label', input).then(($label) => {
    cy.wrap($label.parent().parent())
      .find('input[id*="input"]')
      .clear();
  });
});

Then(
  'I type {string} into the {string} form input for the {string}',
  (value: string, fieldLabel: string, sectionTitle: string) => {

    if (sectionTitle === 'Primary location' || sectionTitle === 'Primary contact') {      
      cy.get('div.frame-01:first').within(() => {
        Step(this, `I type "${value}" into the "${fieldLabel}" form input`);        
      });
    } else {      
      cy.get('cds-accordion cds-accordion-item')
      .shadow()
      .contains('div', sectionTitle).parent().parent().within(() => {
        Step(this, `I type "${value}" into the "${fieldLabel}" form input`);        
      });
    }

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
      cy.get('cds-accordion cds-accordion-item')
      .shadow()
      .contains('div', sectionTitle).parent().parent().within(() => {
        Step(this, `I type "${value}" into the "${fieldLabel}" form input area`);        
      });
    }

});

/* Select Input Steps */

Then('I select {string} from the {string} form input', (value: string, input: string) => {
  cy.contains('label', input).then(($label) => {
    const parentShadow = $label[0].getRootNode();
    cy.wrap(parentShadow).find("[part='trigger-button']").click();
    cy.wrap(parentShadow).parent().find(`cds-combo-box-item[data-value="${value}"]`).click();
  });
});

Then(
  'I select {string} from the {string} form input for the {string}',
  (value: string, fieldLabel: string, sectionTitle: string) => {

    if (sectionTitle === 'Primary location' || sectionTitle === 'Primary contact') {      
      cy.get('div.frame-01:first').within(() => {
        Step(this, `I select "${value}" from the "${fieldLabel}" form input`);
      });
    } else {      
      cy.get('cds-accordion cds-accordion-item')
      .shadow()
      .contains('div', sectionTitle).parent().parent().within(() => {
        Step(this, `I select "${value}" from the "${fieldLabel}" form input`);
      });
    }

});

Then('I select {string} from the {string} multiselect', (value: string, input: string) => {
  cy.contains('label', input).then(($label) => {
    const parentShadow = $label[0].getRootNode();
    cy.wrap(parentShadow).find("[part='trigger-button']").click();
    cy.wrap(parentShadow).parent().find(`cds-multi-select-item[data-value="${value}"]`).click();
  });
});

Then(
  'I select {string} from the {string} multiselect for the {string}',
  (value: string, fieldLabel: string, sectionTitle: string) => {

    if (sectionTitle === 'Primary location' || sectionTitle === 'Primary contact') {      
      cy.get('div.frame-01:first').within(() => {
        Step(this, `I select "${value}" from the "${fieldLabel}" multiselect`);
      });
    } else {      
      cy.get('cds-accordion cds-accordion-item')
      .shadow()
      .contains('div', sectionTitle).parent().parent().within(() => {
        Step(this, `I select "${value}" from the "${fieldLabel}" multiselect`);
      });
    }

});

/* Autocomplete Input Steps */

Then('I type {string} and select {string} from the {string} form autocomplete', (search: string, value: string, input: string) => {
  cy.contains('label', input).then(($label) => {
    const parentShadow = $label[0].getRootNode();
    cy.wrap(parentShadow)
      .find('input')
      .type(search, { delay: 0 })
      .then(() => {
        cy.wrap(parentShadow)
          .parent()
          .find(`cds-combo-box-item[data-value="${value}"], cds-combo-box-item[data-value^="${value}"]`)
          .then(($item) => {
            if ($item.length) {
              cy.wrap($item).click();
            } else {
              throw new Error(`Item with value "${value}" not found.`);
            }
          })
          .then(() => {
            cy.wait(2000);
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
      cy.get('cds-accordion cds-accordion-item')
      .shadow()
      .contains('div', sectionTitle).parent().parent().within(() => {
        Step(this, `I type "${search}" and select "${value}" from the "${fieldLabel}" form autocomplete`);        
      });
    }

});

/* Data tables */

Then('I fill the form as follows', (table: DataTable) => {
  table.rows().forEach(row => {
    const [fieldName, value, kind] = row;
    if (kind === 'text') {
      Step(this, `I type "${value}" into the "${fieldName.trim()}" form input`);
    } else if (kind === 'select') {
      Step(this, `I select "${value}" from the "${fieldName.trim()}" form input`);
    } else if (kind === 'autocomplete') {
      Step(this, `I type "${value}" and select "${value}" from the "${fieldName.trim()}" form autocomplete`);
    } else if (kind === 'multiselect') {
      Step(this, `I select "${value}" from the "${fieldName.trim()}" multiselect`);
    }
  });
});

Then('I fill the {string} address with the following', (location: string, table: DataTable) => {
  table.rows().forEach(row => {
    const [fieldName, value, kind] = row;
    if (kind === 'text') {
      Step(this, `I type "${value}" into the "${fieldName.trim()}" form input for the "${location}"`);
    } else if (kind === 'select') {
      Step(this, `I select "${value}" from the "${fieldName.trim()}" form input for the "${location}"`);
    } else if (kind === 'autocomplete') {
      Step(this, `I type "${value}" and select "${value}" from the "${fieldName.trim()}" form autocomplete for the "${location}"`);
    } else if (kind === 'multiselect') {
      Step(this, `I select "${value}" from the "${fieldName.trim()}" multiselect`);
    }
  });
});

Then('I fill the {string} information with the following', (contactName: string, table: DataTable) => {
  table.rows().forEach(row => {
    const [fieldName, value, kind] = row;
    if (kind === 'text') {
      Step(this, `I type "${value}" into the "${fieldName.trim()}" form input for the "${contactName.trim()}"`);
    } else if (kind === 'select') {
      Step(this, `I select "${value}" from the "${fieldName.trim()}" form input for the "${contactName.trim()}"`);
    } else if (kind === 'autocomplete') {
      Step(this, `I type "${value}" and select "${value}" from the "${fieldName.trim()}" form autocomplete for the "${contactName.trim()}"`);
    } else if (kind === 'multiselect') {
      Step(this, `I select "${value}" from the "${fieldName.trim()}" multiselect`);
    }
  });
});

/* Extra Actions */

Then('I add a new location called {string}', (location: string) => {
  Step(this,'I click on the "Add another location" button');
  cy.wait(10);
  Step(this,`I type "${location}" into the "Location name" form input for the "Additional location"`);
  cy.wait(10);
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

const buttonClick = (name: string, kinds: string[], selector: string = 'body') => {
  if (kinds.length === 0) {
    throw new Error(`Button with label "${name}" not found.`);
  }

  let targetElement: JQuery<HTMLElement> = null;

  cy.get(selector).then((rootElement) => {
    kinds.forEach((kind) => {
      // Find all elements that match the selector type
      targetElement = rootElement.find(kind).filter((index, element) => {
        return Cypress.$(element).html().includes(name) ||
          Cypress.$(element).text().includes(name) ||
          Cypress.$(element).val().toString().includes(name);
      });

      if (targetElement.length > 0) {
        // If a matching element with the correct text is found, click it
        cy.wrap(targetElement).click();
        cy.wait(15);
      }
    })

  });
}

const checkForActionableNotification = (message: string, location: string, kind: string) => {
  let errorLookupTag = '';
  if(location.toLowerCase().includes("top"))
    errorLookupTag = `cds-actionable-notification[id="fuzzy-match-notification-global"][kind="${kind}"] div span.body-compact-01`;
  else
    errorLookupTag = location

  cy.get(errorLookupTag)
  .should('exist')
  .should('contain.text', message);
}
