import AddressGroupComponent from '@/components/grouping/AddressGroupComponent.vue'

describe('<AddressGroupComponent />', () => {
  const countries = [
    { code: 'CA', name: 'Canada' },
    { code: 'US', name: 'United States of America' }
  ]
  const provinces = [
    { code: 'NL', name: 'Newfoundland and Labrador' },
    { code: 'PE', name: 'Prince Edward Island' },
    { code: 'NS', name: 'Nova Scotia' },
    { code: 'NB', name: 'New Brunswick' },
    { code: 'QC', name: 'Quebec' },
    { code: 'ON', name: 'Ontario' },
    { code: 'MB', name: 'Manitoba' },
    { code: 'SK', name: 'Saskatchewan' },
    { code: 'AB', name: 'Alberta' },
    { code: 'BC', name: 'British Columbia' },
    { code: 'YT', name: 'Yukon' },
    { code: 'NT', name: 'Northwest Territories' },
    { code: 'NU', name: 'Nunavut' }
  ]

  const address = {
    streetAddress: '2975 Jutland Rd',
    country: {
      value: 'CA',
      text: 'Canada'
    },
    province: {
      value: 'BC',
      text: 'British Columbia'
    },
    city: 'Victoria',
    postalCode: 'V8T5J9',
    locationName: 'Mailing Address'
  }
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
      provinces
    ).as('getProvinces')
  })

  it('should render the component', () => {
    cy.mount(AddressGroupComponent, {
      props: {
        id: 0,
        modelValue: address,
        countryList: countries,
        validations: []
      }
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

    cy.wait('@getProvinces')

    cy.get('#addr_0')
      .should('be.visible')
      .and('have.value', address.streetAddress + ' fault')

    cy.get('#postalCode_0')
      .shadow()
      .find('.bx--form-requirement')
      .should('be.visible')
      .find('slot')
      .and('have.text', ' Error ')
  })
})
