/* eslint-disable no-undef */
describe('General Form', () => {
  beforeEach(() => {
    cy.intercept('http://localhost:8080/api/clients/name/*', {
      fixture: 'business.json'
    }).as('searchCompany')

    cy.intercept('GET', '/api/clients/XX9016140', {
      fixture: 'example.json'
    }).as('selectCompany')
  })

  it('should render the component', () => {
    cy.visit('/')
    cy.get('#landing-title').should('contain', 'Welcome to CLIENT')

    cy.get('#landing-subtitle').should(
      'contain',
      "The Ministry of Forests' client management system"
    )
  })

  it('should render the form', () => {
    cy.visit('/')
    cy.wait(500)

    cy.login('uattest@forest.client', 'Test, UAT WLRS:EX')

    cy.get('bx-form-item > #business').should('not.exist')

    cy.get('.display-block-wrapper-info').should('not.be.visible')

    cy.get(
      '[label-text="I have a BC registered business (corporation, sole proprietorship, society, etc.)"]'
    )
      .should('be.visible')
      .click()

    cy.get('.display-block-wrapper-info').should('be.visible')

    cy.get('bx-form-item > #business')
      .should('be.visible')
      .shadow()
      .find('input')
      .should('have.value', '')
      .type('Valid Company')
    cy.wait('@searchCompany')

    cy.get('div.autocomplete-items-cell[data-id="XX9016140"]')
      .click()

    cy.wait('@selectCompany')

    cy.get('[data-test="wizard-next-button"]').should('be.visible').click()

    cy.logout()
  })
})
