declare namespace Cypress {
  interface Chainable<Subject> {
    login(email: string, name: string): Chainable<void>;
      addCookie(name: string, value: string): Chainable<void>;
      addToLocalStorage(key: string, value: any): Chainable<void>;
      expireLocalStorage(key: string): Chainable<void>;
      addToSessionStorage(key: string, value: any): Chainable<void>;
      expireSessionStorage(key: string): Chainable<void>;
      expireCookie(name: string): Chainable<void>;
      login(email: string, name: string): Chainable<void>;
      logout(): Chainable<void>;
  }
}
