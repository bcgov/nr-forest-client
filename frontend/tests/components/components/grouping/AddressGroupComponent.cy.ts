import AddressGroupComponent from '@/components/grouping/AddressGroupComponent.vue'

describe('<AddressGroupComponent />', () => {
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
    cy.intercept(
      'GET',
      '/api/clients/activeCountryCodes/CA?page=0&size=250',
      { fixture: 'provinces.json' }
    ).as('getProvinces')

    cy.intercept(
      'GET',
      '/api/clients/activeCountryCodes/US?page=0&size=250',
      { fixture: 'states.json' }
    ).as('getStates')

    cy.fixture('address.json').as('addressFixture')
    cy.fixture('countries.json').as('countriesFixture')
  })

  it('should render the component', () => {
    cy.get('@addressFixture').then((address) => {
      cy.get('@countriesFixture').then((countries) => {
        cy.mount(AddressGroupComponent, {
          props: {
            id: 0,
            modelValue: address,
            countryList: countries,
            validations: []
          }
        })
      })
    })

    cy.wait('@getProvinces')

    cy.get('#country_0').should('be.visible').and('have.value', 'CA')

    cy.get('bx-input')
      .should('be.visible')
      .and('have.id', 'addr_0')
      .and('have.value', '2975 Jutland Rd')

    cy.get('#city_0').should('be.visible').and('have.value', 'Victoria')

    cy.get('#province_0').should('be.visible').and('have.value', 'BC')

    cy.get('#postalCode_0').should('be.visible').and('have.value', 'V8T5J9')
  })

  it('should render the component with validation', () => {
    cy.get('@addressFixture').then((address:any) => {
      cy.get('@countriesFixture').then((countries) => {
        cy.mount(AddressGroupComponent, {
          props: {
            id: 0,
            modelValue: {
              ...address,
              streetAddress: address.streetAddress + ' fault'
            },
            countryList: countries,
            validations: [dummyValidation()]
          }
        })
      })
    })

    cy.wait('@getProvinces')

    cy.get('@addressFixture').then((address:any) => {
      cy.get('#addr_0')
        .should('be.visible')
        .and('have.value', address.streetAddress + ' fault')
    })

    cy.get('#postalCode_0')
      .shadow()
      .find('.bx--form-requirement')
      .should('be.visible')
      .find('slot')
      .and('have.text', ' Error ')
  })

  it('should render the component and set focus on street address input', () => {
    cy.get('@addressFixture').then((address) => {
      cy.get('@countriesFixture').then((countries) => {
        cy.mount(AddressGroupComponent, {
          props: {
            id: 0,
            modelValue: address,
            countryList: countries,
            validations: []
          }
        })
      })
    })

    cy.wait('@getProvinces')

    cy.get('#addr_0')
      .should('be.visible')
      .and('have.focus')
  })

  it('should render the component and show the address name if id is bigger than 0', () => {
    cy.get('@addressFixture').then((address) => {
      cy.get('@countriesFixture').then((countries) => {
        cy.mount(AddressGroupComponent, {
          props: {
            id: 1,
            modelValue: address,
            countryList: countries,
            validations: []
          }
        })
      })
    })

    cy.wait('@getProvinces')

    cy.get('#name_1')
      .should('be.visible')
      .and('have.value', 'Mailing Address')
      .and('have.focus')
  })

  it('should render the component and reset province when country changes', () => {
    cy.get('@addressFixture').then((address) => {
      cy.get('@countriesFixture').then((countries) => {
        cy.mount(AddressGroupComponent, {
          props: {
            id: 0,
            modelValue: address,
            countryList: countries,
            validations: []
          }
        })
      })
    })

    cy.wait('@getProvinces')

    cy.get('#postalCode_0')
      .should('be.visible')
      .shadow()
      .find('label')
      .and('have.text', '  Postal code  ')

    cy.get('#country_0')
      .should('be.visible')
      .and('have.value', 'CA')
      .click()
      .find('[data-item="US"]')
      .click()
      .and('have.value', 'US')

    cy.wait('@getStates')

    cy.get('#province_0')
      .should('be.visible')
      .and('have.value', '')
      .click()
      .find('[data-item="IL"]')
      .click()
      .and('have.value', 'IL')

    cy.get('#postalCode_0')
      .should('be.visible')
      .shadow()
      .find('label')
      .and('have.text', '  Zip code  ')
  })
})
