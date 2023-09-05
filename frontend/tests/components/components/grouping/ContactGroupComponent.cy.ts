import ContactGroupComponent from '@/components/grouping/ContactGroupComponent.vue'
import type { Contact } from '@/dto/ApplyClientNumberDto'

describe('<ContactGroupComponent />', () => {
  const dummyValidation = (): ((key: string, field: string) => (value: string) => string) => {
    return (key: string, fieldId: string) => (value: string) => {
      if (value.includes('fault')) return 'Error'
      return ''
    }
  }

  beforeEach(() => {
    cy.fixture('contact.json').as('contactFixture')
    cy.fixture('roles.json').as('rolesFixture')
    cy.fixture('addresses.json').as('addressesFixture')
  })

  it('should render the component', () => {
    cy.get('@contactFixture').then((contact: Contact) => {
      cy.get('@rolesFixture').then((roles) => {
        cy.get('@addressesFixture').then((addresses) => {
          cy.mount(ContactGroupComponent, {
            props: {
              id: 0,
              modelValue: contact,
              enabled: true,
              roleList: roles,
              addressList: addresses,
              validations: [],
            },
          })
        })
      })
    })

    cy.get('@contactFixture').then((contact: Contact) => {
      cy.get('#firstName_0').should('be.visible').and('have.value', contact.firstName)

      cy.get('#lastName_0').should('be.visible').and('have.value', contact.lastName)

      cy.get('#email_0').should('be.visible').and('have.value', '')

      cy.get('#phoneNumber_0').should('be.visible').and('have.value', '')
    })
  })

  it('should render the component with validation', () => {
    cy.get('@contactFixture').then((contact: Contact) => {
      cy.get('@rolesFixture').then((roles) => {
        cy.get('@addressesFixture').then((addresses) => {
          cy.mount(ContactGroupComponent, {
            props: {
              id: 0,
              modelValue: {
                ...contact,
                firstName: contact.firstName + ' fault',
              },
              enabled: true,
              roleList: roles,
              addressList: addresses,
              validations: [dummyValidation()],
            },
          })
        })
      })
    })

    cy.get('@contactFixture').then((contact: Contact) => {
      cy.get('#firstName_0')
        .should('be.visible')
        .and('have.value', contact.firstName + ' fault')

      cy.get('#firstName_0')
        .shadow()
        .find('.cds--form-requirement')
        .should('be.visible')
        .find('slot')
        .and('include.text', 'Error')

      cy.get('#lastName_0').should('be.visible').and('have.value', contact.lastName)

      cy.get('#lastName_0')
        .shadow()
        .find('.cds--form-requirement')
        .should('be.visible')
        .find('slot')
        .and('include.text', 'Error')

      cy.get('#email_0').should('be.visible').and('have.value', '')

      cy.get('#phoneNumber_0').should('be.visible').and('have.value', '')
    })
  })

  it('should render the component and select first address and show on component', () => {
    cy.get('@contactFixture').then((contact: Contact) => {
      cy.get('@rolesFixture').then((roles) => {
        cy.get('@addressesFixture').then((addresses) => {
          cy.mount(ContactGroupComponent, {
            props: {
              id: 0,
              modelValue: {
                ...contact,
                addresses: [addresses[0], addresses[1]],
              },
              enabled: true,
              roleList: roles,
              addressList: addresses,
              validations: [],
            },
          })
        })
      })
    })

    cy.get('#addressname_0').should('be.visible').and('have.value', '')

    cy.get('#addressname_0')
      .click()
      .find('cds-multi-select-item[data-value="Mailing address"]')
      .should('exist')
      .and('be.visible')
      .click()

    cy.get('#addressname_0').should('be.visible').and('have.value', 'Mailing address')
  })

  it('should render the component and select both addresses and show it as value', () => {
    cy.get('@contactFixture').then((contact: Contact) => {
      cy.get('@rolesFixture').then((roles) => {
        cy.get('@addressesFixture').then((addresses) => {
          cy.mount(ContactGroupComponent, {
            props: {
              id: 0,
              modelValue: {
                ...contact,
                addresses: [addresses[0], addresses[1]],
              },
              enabled: true,
              roleList: roles,
              addressList: addresses,
              validations: [],
            },
          })
        })
      })
    })

    cy.get('#addressname_0').should('be.visible').and('have.value', '')

    cy.get('#addressname_0')
      .click()
      .find('cds-multi-select-item[data-value="Mailing address"]')
      .should('exist')
      .and('be.visible')
      .click()

    cy.get('#addressname_0')
      .click()
      .find('cds-multi-select-item[data-value="Jutland office"]')
      .should('exist')
      .and('be.visible')
      .click()

    cy.get('#addressname_0')
      .should('be.visible')
      .and('have.value', 'Mailing address,Jutland office')
  })

  it('should render the component and select first address and show it then remove it', () => {
    cy.get('@contactFixture').then((contact: Contact) => {
      cy.get('@rolesFixture').then((roles) => {
        cy.get('@addressesFixture').then((addresses) => {
          cy.mount(ContactGroupComponent, {
            props: {
              id: 0,
              modelValue: {
                ...contact,
                addresses: [addresses[0], addresses[1]],
              },
              enabled: true,
              roleList: roles,
              addressList: addresses,
              validations: [],
            },
          })
        })
      })
    })

    cy.get('#addressname_0').should('be.visible').and('have.value', '')

    cy.get('#addressname_0')
      .click()
      .find('cds-multi-select-item[data-value="Mailing address"]')
      .should('exist')
      .and('be.visible')
      .click()

    cy.get('#addressname_0').should('be.visible').and('have.value', 'Mailing address')

    cy.get('#addressname_0')
      .click()
      .find('cds-multi-select-item[data-value="Mailing address"]')
      .should('exist')
      .and('be.visible')
      .click()

    cy.get('#addressname_0').should('be.visible').and('have.value', '')
  })
})
