declare namespace Cypress {
  interface Chainable<Subject> {
    logout(): Chainable<void>;
    getMany(names: string[]): Chainable<any[]>;
    fillFormEntry(field: string, value: string, delayMS: number, area: boolean): Chainable<void>;
    clearFormEntry(field: string, area: boolean): Chainable<void>;
    selectFormEntry(field: string, value: string, box: boolean): Chainable<void>;
    selectAutocompleteEntry(field: string, value: string, dataid: string, delayTarget: string): Chainable<void>;
    checkInputErrorMessage(field: string, message: string): Chainable<void>;
    checkAutoCompleteErrorMessage(field: string, message: string): Chainable<void>;
    checkAccordionItemState(additionalSelector: string, open: boolean): Chainable<void>;
    waitForPageLoad(element: string): Chainable<void>;
  }
}