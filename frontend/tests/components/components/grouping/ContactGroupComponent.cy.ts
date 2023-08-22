import ContactGroupComponent from '@/components/grouping/ContactGroupComponent.vue'
import type { Contact } from '@/dto/ApplyClientNumberDto'

describe('<ContactGroupComponent />', () => {
  const dummyValidation = (): ((
    key: string,
    field: string
  ) => (value: string) => string) => {
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
    cy.get('@contactFixture').then((contact:Contact) => {
      cy.get('@rolesFixture').then((roles) => {
        cy.get('@addressesFixture').then((addresses) => {
          cy.mount(ContactGroupComponent, {
            props: {
              id: 0,
              modelValue: contact,
              enabled: true,
              roleList: roles,
              addressList: addresses,
              validations: []
            }
          })
        })
      })
    })

    cy.get('@contactFixture')
      .then((contact:Contact) => {
        cy.get('#firstName_0')
          .should('be.visible')
          .and('have.value', contact.firstName)

        cy.get('#lastName_0')
          .should('be.visible')
          .and('have.value', contact.lastName)

        cy.get('#email_0')
          .should('be.visible')
          .and('have.value', '')

        cy.get('#phoneNumber_0')
          .should('be.visible')
          .and('have.value', '')
      })
  })

  it('should render the component with validation', () => {
    cy.get('@contactFixture').then((contact:Contact) => {
      cy.get('@rolesFixture').then((roles) => {
        cy.get('@addressesFixture').then((addresses) => {
          cy.mount(ContactGroupComponent, {
            props: {
              id: 0,
              modelValue: {
                ...contact,
                firstName: contact.firstName + ' fault'
              },
              enabled: true,
              roleList: roles,
              addressList: addresses,
              validations: [dummyValidation()]
            }
          })
        })
      })
    })

    cy.get('@contactFixture')
      .then((contact:Contact) => {
        cy.get('#firstName_0')
          .should('be.visible')
          .and('have.value', contact.firstName + ' fault')

        cy.get('#firstName_0')
          .shadow()
          .find('.bx--form-requirement')
          .should('be.visible')
          .find('slot')
          .and('have.text', ' Error ')

        cy.get('#lastName_0')
          .should('be.visible')
          .and('have.value', contact.lastName)

        cy.get('#lastName_0')
          .shadow()
          .find('.bx--form-requirement')
          .should('be.visible')
          .find('slot')
          .and('have.text', ' Error ')

        cy.get('#email_0')
          .should('be.visible')
          .and('have.value', '')

        cy.get('#phoneNumber_0')
          .should('be.visible')
          .and('have.value', '')
      })
  })

  it('it should render the component and set focus on address input', () => {
    cy.get('@contactFixture').then((contact:Contact) => {
      cy.get('@rolesFixture').then((roles) => {
        cy.get('@addressesFixture').then((addresses) => {
          cy.mount(ContactGroupComponent, {
            props: {
              id: 0,
              modelValue: contact,
              enabled: true,
              roleList: roles,
              addressList: addresses,
              validations: []
            }
          })
        })
      })
    })

    cy.get('#address_0')
      .should('be.visible')
      .and('have.focus')
  })

  it('should render the component and select first address and show it as tag', () => {
    cy.get('@contactFixture').then((contact:Contact) => {
      cy.get('@rolesFixture').then((roles) => {
        cy.get('@addressesFixture').then((addresses) => {
          cy.mount(ContactGroupComponent, {
            props: {
              id: 0,
              modelValue: {
                ...contact,
                addresses: [addresses[0], addresses[1]]
              },
              enabled: true,
              roleList: roles,
              addressList: addresses,
              validations: []
            }
          })
        })
      })
    })

    cy.get('#address_0')
      .should('be.visible')
      .and('have.value', '')

    cy.get('#address_0')
      .click()
      .find('[data-item="00"]')
      .click()

    cy.get('bx-tag#tag_address_0_0.bx-tag')
      .should('be.visible')
      .and('have.text', 'Mailing Address ')
  })

  it('should render the component and select both addresses and show it as tag', () => {
    cy.get('@contactFixture').then((contact:Contact) => {
      cy.get('@rolesFixture').then((roles) => {
        cy.get('@addressesFixture').then((addresses) => {
          cy.mount(ContactGroupComponent, {
            props: {
              id: 0,
              modelValue: {
                ...contact,
                addresses: [addresses[0], addresses[1]]
              },
              enabled: true,
              roleList: roles,
              addressList: addresses,
              validations: []
            }
          })
        })
      })
    })

    cy.get('#address_0')
      .should('be.visible')
      .and('have.value', '')

    cy.get('#address_0')
      .click()
      .find('[data-item="00"]')
      .click()

    cy.get('#address_0')
      .click()
      .find('[data-item="01"]')
      .click()

    cy.get('bx-tag#tag_address_0_0.bx-tag')
      .should('be.visible')
      .and('have.text', 'Mailing Address ')

    cy.get('bx-tag#tag_address_0_1.bx-tag')
      .should('be.visible')
      .and('have.text', 'Jutland office ')
  })

  it('should render the component and select first address and show it as tag then remove it', () => {
    cy.get('@contactFixture').then((contact:Contact) => {
      cy.get('@rolesFixture').then((roles) => {
        cy.get('@addressesFixture').then((addresses) => {
          cy.mount(ContactGroupComponent, {
            props: {
              id: 0,
              modelValue: {
                ...contact,
                addresses: [addresses[0], addresses[1]]
              },
              enabled: true,
              roleList: roles,
              addressList: addresses,
              validations: []
            }
          })
        })
      })
    })

    cy.get('#address_0')
      .should('be.visible')
      .and('have.value', '')

    cy.get('#address_0')
      .click()
      .find('[data-item="00"]')
      .click()

    cy.get('bx-tag#tag_address_0_0.bx-tag')
      .should('be.visible')
      .and('have.text', 'Mailing Address ')
      .find('svg')
      .click()

    cy.get('div.bx-tag-box')
      .should('not.be.visible')
      .and('have.text', '')
  })
})
