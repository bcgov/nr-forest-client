declare namespace Cypress {
  interface Chainable<Subject> {
    addCookie(name: string, value: string): Chainable<void>;
    addToLocalStorage(key: string, value: any): Chainable<void>;
    expireLocalStorage(key: string): Chainable<void>;
    addToSessionStorage(key: string, value: any): Chainable<void>;
    expireSessionStorage(key: string): Chainable<void>;
    expireCookie(name: string): Chainable<void>;
    login(email: string, name: string, provider: string, extras: any = "{}"): Chainable<void>;
    logout(): Chainable<void>;
    getMany(names: string[]): Chainable<any[]>;
    fillFormEntry(field: string, value: string, delayMS: number = 10, area: boolean = false): Chainable<void>;
    clearFormEntry(field: string, area: boolean = false): Chainable<void>;
    selectFormEntry(field: string, value: string, box: boolean): Chainable<void>;
    markCheckbox(field: string): Chainable<void>;
    unmarkCheckbox(field: string): Chainable<void>;
    selectAutocompleteEntry(field: string, value: string, dataid: string, delayTarget: string =''): Chainable<void>;
    checkInputErrorMessage(field: string, message: string): Chainable<void>;
    checkAutoCompleteErrorMessage(field: string, message: string): Chainable<void>;
    checkAccordionItemState(additionalSelector: string, open: boolean): Chainable<void>;
  }
}
