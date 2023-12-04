declare namespace Cypress {
  interface Chainable<Subject> {
    addCookie(name: string, value: string): Chainable<void>;
    addToLocalStorage(key: string, value: any): Chainable<void>;
    expireLocalStorage(key: string): Chainable<void>;
    addToSessionStorage(key: string, value: any): Chainable<void>;
    expireSessionStorage(key: string): Chainable<void>;
    expireCookie(name: string): Chainable<void>;
    login(email: string, name: string, provider: string, extras: any): Chainable<void>;
    logout(): Chainable<void>;
    getMany(names: string[]): Chainable<any[]>;
  }
}
