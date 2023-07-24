/* eslint-disable no-undef */
/// <reference types="cypress" />
// ***********************************************
// This example commands.ts shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })
//
// declare global {
//   namespace Cypress {
//     interface Chainable {
//       login(email: string, password: string): Chainable<void>
//       drag(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
//       dismiss(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
//       visit(originalFn: CommandOriginalFn, url: string, options: Partial<VisitOptions>): Chainable<Element>
//     }
//   }
// }

const generateRandomHex = (length: number): string => {
  const characters = '0123456789abcdef'
  let result = ''
  for (let i = 0; i < length; i++) {
    const randomIndex = Math.floor(Math.random() * characters.length)
    result += characters.charAt(randomIndex)
  }
  return result
}

Cypress.Commands.add('addCookie', (name: string, value: string) => {
  cy.setCookie(name, value, {
    domain: 'localhost',
    path: '/',
    httpOnly: true,
    secure: true,
    expiry: Date.now() + 86400000
  })
})

Cypress.Commands.add('expireCookie', (name: string) => {
  cy.setCookie(name, '', {
    domain: 'localhost',
    path: '/',
    httpOnly: true,
    secure: true,
    expiry: Date.now() - 86400000 * 2
  })
})

Cypress.Commands.add('addToSessionStorage', (key: string, value: any) => {
  cy.window().then((win) => {
    win.sessionStorage.setItem(key, JSON.stringify(value))
  })
})

Cypress.Commands.add('expireSessionStorage', (key: string) => {
  cy.window().then((win) => {
    win.sessionStorage.removeItem(key)
  })
})

Cypress.Commands.add('addToLocalStorage', (key: string, value: any) => {
  cy.window().then((win) => {
    win.localStorage.setItem(key, JSON.stringify(value))
  })
})

Cypress.Commands.add('expireLocalStorage', (key: string) => {
  cy.window().then((win) => {
    win.localStorage.removeItem(key)
  })
})

Cypress.Commands.add('login', (email: string, name: string) => {
  cy.get('.landing-button').should('be.visible')
  cy.addToSessionStorage('user', {
    name,
    provider: 'idir',
    userId: generateRandomHex(32),
    email,
    firstName: 'UAT',
    lastName: 'Test'
  })
  cy.reload()
  cy.wait(1000)
})
