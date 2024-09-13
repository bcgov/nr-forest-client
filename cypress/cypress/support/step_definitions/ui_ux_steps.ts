import { Then, Step, BeforeStep } from "@badeball/cypress-cucumber-preprocessor";

let idir = true;

BeforeStep({ tags: "@loginAsBCeID or @loginAsBCSC" }, function () {
  idir = false;
});
BeforeStep({ tags: "@loginAsEditor or @loginAsViewer or @loginAsAdmin" }, function () {
  idir = true;
});

/* UX and UI checks */

Then('The {string} component is using the font {string}', (component: string, font: string) => {
  cy.contains('label', component).should('exist').then(($label) => {
    const parentShadow = $label[0].getRootNode() as ShadowRoot;

    if (parentShadow.host) {
      const parentComponent = parentShadow.host as HTMLElement; // Host HTMLElement
      cy.wrap(parentComponent).then((element) => {
        checkComputedStyle(element, 'font-family', font); // Reuse the common function
      });
    } else {
      throw new Error('No host element found for the given shadow root.');
    }
  });
});

Then('The {string} component is using the font {string} for the {string}', (component: string, font: string, scope: string) => {

  if (scope === 'Primary location' || scope === 'Primary contact') {      
    cy.get('div.frame-01:first').within(() => {
      Step(this, `The "${component}" component is using the font "${font}"`);
    });
  } else if(idir){
      cy.get('cds-accordion cds-accordion-item')
      .shadow()
      .contains('div', scope).parent().parent().within(() => {
        Step(this, `The "${component}" component is using the font "${font}"`);
      });
    } else {
      cy.get(`div.frame-01[data-text="${scope}"]`).within(() => {
        Step(this, `The "${component}" component is using the font "${font}"`);
      });
    }  
});

Then('The {string} has weight {string}',(text: string, weight: string) => {
  checkForCssProperty(text, 'font-weight', weight);
});

Then('The {string} has weight {string} inside the {string}',(text: string, weight: string, scope: string) => {
  let computedScope = scope === 'form' ? 'div[role="main"]' : scope;
  computedScope = scope === 'top' ? 'div[role="header"]' : computedScope;
  computedScope = scope === 'bottom' ? 'div[role="footer"]' : computedScope;
  checkForCssProperty(text, 'font-weight', weight, computedScope);
});

Then('The {string} size is {string}',(text: string, size: string) => {
  checkForCssProperty(text, 'font-size', size);
});

Then('The {string} size is {string} inside the {string}',(text: string, size: string,scope: string) => {
  let computedScope = scope === 'form' ? 'div[role="main"]' : scope;
  computedScope = scope === 'top' ? 'div[role="header"]' : computedScope;
  computedScope = scope === 'bottom' ? 'div[role="footer"]' : computedScope;
  checkForCssProperty(text, 'font-size', size, computedScope);
});

// Helper functions to check computed CSS properties

const checkComputedStyle = (
  element: JQuery<HTMLElement>,
  property: string,
  expectedValue: string
) => {
  cy.window().then((win) => {
    const computedStyle = win.getComputedStyle(element[0]); // Get the native DOM element
    const styleValue = computedStyle.getPropertyValue(property); // Get the computed CSS property
    // Assert that the style matches the expected value
    expect(styleValue.trim().toLowerCase()).to.match(new RegExp(`^${expectedValue.toLowerCase()}`));
  });
};

const checkForCssProperty = (text: string, property: string, value: string, scope: string = 'body') => {
  cy.get(scope).find('*').each(($el) => {
    cy.wrap($el).then((el) => {
      const elementText = el.contents().filter(function () {
        return this.nodeType === 3;
      }).text().trim();

      if (elementText === text) {
        checkComputedStyle(el, property, value); // Reuse the common function
      }
    });
  });
};
