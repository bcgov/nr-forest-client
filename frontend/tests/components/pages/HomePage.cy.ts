import HomePage from '@/pages/HomePage.vue'

describe('<HomePage />', () => {
  beforeEach(() => {
    cy.mount(HomePage)
  })
  it('should contain two links', () => {
    cy.get('router-link').should('have.length', 3)
  })

  it('should contain a link with text "New Client"', () => {
    cy.contains('router-link', 'New Client').should('be.visible')
  })

  it('should contain a link with text "Form Submission"', () => {
    cy.contains('router-link', 'Form Submission').should('be.visible')
  })
})
