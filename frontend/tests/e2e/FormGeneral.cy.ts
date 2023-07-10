describe('General Form', () => {
  beforeEach(() => {
    cy.intercept('GET', '/api/clients/name/*', [
      {
        code: 'XX9016140',
        name: 'Totally Valid Company',
        status: 'ACTIVE',
        legalType: 'SP'
      }
    ]).as('searchCompany')

    cy.intercept('GET', '/api/clients/XX9016140', {
      name: 'The Good Company',
      id: 'XX9016140',
      goodStanding: true,
      addresses: [
        {
          streetAddress: '123 Fake DR',
          country: {
            value: 'CA',
            text: 'Canada'
          },
          province: {
            value: 'BC',
            text: 'British Columbia'
          },
          city: 'Fakest City',
          postalCode: 'A0A0A0',
          index: 0,
          locationName: 'Mailing Address'
        }
      ],
      contacts: [
        {
          contactType: {
            value: 'P',
            text: 'Person'
          },
          firstName: 'Jhon',
          lastName: 'Wick',
          phoneNumber: '(111) 222-3344',
          email: 'babayaga@theguild.ca',
          index: 0,
          locationNames: [
            {
              value: '0',
              text: 'Mailing Address'
            }
          ]
        }
      ]
    }).as('selectCompany')
  })

  it('should render the component', () => {
    cy.visit('/')
    cy.get('.wizard-head-text > .bx--title').should(
      'contain',
      'New client application'
    )

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
      .type('Valid Business Name')

    cy.wait('@searchCompany')

    cy.get('div.autocomplete-items-ct')
      .should('be.visible')
      .find('div.autocomplete-items-cell')
      .should('be.visible')
      .click()

    cy.wait('@selectCompany')

    cy.get('div.wizard-wrap')
      .should('be.visible')
      .find('[data-test="wizard-next-button"]')
      .should('be.visible')
      .click()
  })
})
