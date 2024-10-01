declare namespace Cypress {
  interface Chainable<Subject> {
    logout(): Chainable<void>;    
    checkAutoCompleteErrorMessage(field: string, message: string): Chainable<void>;
    checkAccordionItemState(additionalSelector: string, open: boolean): Chainable<void>;
    waitForPageLoad(element: string): Chainable<void>;
    logAndScreenshot(message: string): Chainable<void>;
  }
}