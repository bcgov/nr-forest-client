/* eslint-disable no-undef */
/// <reference types="cypress" />

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
    domain: '127.0.0.1',
    path: '/',
    httpOnly: false,
    secure: false,
    expiry: Date.now() + 86400000
  })
})

Cypress.Commands.add('expireCookie', (name: string) => {
  cy.setCookie(name, '', {
    domain: '127.0.0.1',
    path: '/',
    httpOnly: false,
    secure: false,
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
  const jwtBody = {
    'custom:idp_display_name': name,
    'custom:idp_name': 'idir',
    'custom:idp_user_id': generateRandomHex(32),
    email,
    firstName: 'UAT',
    lastName: 'Test'
  }

  const payloadString = btoa(JSON.stringify(jwtBody))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '')
  cy.addCookie('idToken', `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.${payloadString}.`)
  cy.reload()
  cy.wait(1000)
})

Cypress.Commands.add('logout', () => {
  cy.get('[data-id=logout-btn]').should('be.visible')
  cy.expireCookie('idToken')
  cy.reload()
  cy.wait(1000)
})
