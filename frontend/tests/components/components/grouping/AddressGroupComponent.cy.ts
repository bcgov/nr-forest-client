import AddressGroupComponent from '@/components/grouping/AddressGroupComponent.vue'

// load app validations
import '@/helpers/validators/BCeIDFormValidations'

describe('<AddressGroupComponent />', () => {
  const dummyValidation = (): ((key: string, field: string) => (value: string) => string) => {
    return (key: string, fieldId: string) => (value: string) => {
      if (value.includes('fault')) return 'Error'
      return ''
    }
  }

  beforeEach(() => {
    cy.intercept('GET', '/api/clients/activeCountryCodes/CA?page=0&size=250', {
      fixture: 'provinces.json',
    }).as('getProvinces')

    cy.intercept('GET', '/api/clients/activeCountryCodes/US?page=0&size=250', {
      fixture: 'states.json',
    }).as('getStates')

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
            validations: [],
          },
        })
      })
    })

    cy.wait('@getProvinces')

    cy.get('#country_0').should('be.visible').and('have.value', 'Canada')

    cy.get('#addr_0')
      .should('be.visible')
      .and('have.id', 'addr_0')
      .and('have.value', '2975 Jutland Rd')

    cy.get('#city_0').should('be.visible').and('have.value', 'Victoria')

    cy.get('#province_0').should('be.visible').and('have.value', 'British Columbia')

    cy.get('#postalCode_0').should('be.visible').and('have.value', 'V8T5J9')
  })

  it('should render the component with validation', () => {
    cy.get('@addressFixture').then((address: any) => {
      cy.get('@countriesFixture').then((countries) => {
        cy.mount(AddressGroupComponent, {
          props: {
            id: 0,
            modelValue: {
              ...address,
              streetAddress: address.streetAddress + ' fault',
            },
            countryList: countries,
            validations: [dummyValidation()],
          },
        })
      })
    })

    cy.wait('@getProvinces')

    cy.get('@addressFixture').then((address: any) => {
      cy.get('#addr_0')
        .should('be.visible')
        .and('have.value', address.streetAddress + ' fault')
    })

    cy.get('#postalCode_0')
      .shadow()
      .find('.cds--form-requirement')
      .should('be.visible')
      .find('slot')
      .and('include.text', 'Error')
  })

  it('should render the component and set focus on street address input', () => {
    cy.get('@addressFixture').then((address) => {
      cy.get('@countriesFixture').then((countries) => {
        cy.mount(AddressGroupComponent, {
          props: {
            id: 0,
            modelValue: address,
            countryList: countries,
            validations: [],
          },
        })
      })
    })

    cy.wait('@getProvinces')

    cy.get('#addr_0').should('be.visible').and('have.focus')
  })

  it('should render the component and show the address name if id is bigger than 0', () => {
    cy.get('@addressFixture').then((address) => {
      cy.get('@countriesFixture').then((countries) => {
        cy.mount(AddressGroupComponent, {
          props: {
            id: 1,
            modelValue: address,
            countryList: countries,
            validations: [],
          },
        })
      })
    })

    cy.wait('@getProvinces')

    cy.get('#name_1').should('be.visible').and('have.value', 'Mailing address').and('have.focus')
  })

  it('should render the component and reset province when country changes', () => {
    cy.get('@addressFixture').then((address) => {
      cy.get('@countriesFixture').then((countries) => {
        cy.mount(AddressGroupComponent, {
          props: {
            id: 0,
            modelValue: address,
            countryList: countries,
            validations: [],
          },
        })
      })
    })

    cy.wait('@getProvinces')

    cy.get('#postalCode_0')
      .should('be.visible')
      .shadow()
      .find('label')
      .and('include.text', 'Postal code')

    cy.get('#country_0').should('be.visible').and('have.value', 'Canada').click()

    cy.get('#country_0')
      .find('cds-combo-box-item[data-id="US"]')
      .shadow()
      .find('div')
      .should('be.visible') // wait for the option's inner, standard HTML element to become visible before clicking it.

    cy.get('#country_0')
      .find('cds-combo-box-item[data-id="US"]')
      .click()
      .and('have.value', 'United States of America')

    cy.wait('@getStates')

    cy.get('#province_0')
      .should('be.visible')
      .and('have.value', '')
      .click()
      .find('cds-combo-box-item[data-id="IL"]')
      .click()
      .and('have.value', 'Illinois')

    cy.get('#postalCode_0')
      .should('be.visible')
      .shadow()
      .find('label')
      .and('include.text', 'Zip code')
  })

  describe('when it has last emitted "valid" with false due to a single, not empty, invalid field', () => {
    const genericTest = (
      fieldSelector: string,
      firstContent: string,
      additionalContent: string,
    ) => {
      const calls: boolean[] = []
      const onValid = (valid: boolean) => {
        calls.push(valid)
      }
      cy.get('@addressFixture').then((address) => {
        cy.get('@countriesFixture').then((countries) => {
          cy.mount(AddressGroupComponent, {
            props: {
              id: 1,
              modelValue: address,
              countryList: countries,
              validations: [],
              onValid,
            },
          })
        })
      })
      cy.wait('@getProvinces')

      // Prevents issues due to moving focus in the middle of the test.
      cy.get('#name_1').should('be.focused')

      cy.get(fieldSelector).shadow().find('input').clear() // emits false
      cy.get(fieldSelector).blur() // (doesn't emit)
      cy.get(fieldSelector).shadow().find('input').type(firstContent) // emits true before blurring
      cy.get(fieldSelector).blur() // emits false
      cy.get(fieldSelector).should('be.visible').and('have.value', firstContent)
      cy.get(fieldSelector).shadow().find('input').type(additionalContent) // emits true (last2)
      cy.get(fieldSelector).blur() // emits false (last1)
      cy.get(fieldSelector)
        .should('be.visible')
        .and('have.value', `${firstContent}${additionalContent}`)

      // For some reason on Electron we need to wait a millisecond now.
      // Otherwise this test either fails or can't be trusted on Electron.
      cy.wait(1)

      return calls
    }
    const checkValidFalseAgain = (calls: boolean[]) => {
      cy.wrap(calls).then((value) => {
        const last1 = value.pop()
        const last2 = value.pop()
        const last3 = value.pop()
        expect(last3).to.equal(false)
        expect(last2).to.equal(true)
        expect(last1).to.equal(false)
      })
    }
    const checkValidTrue = (calls: boolean[]) => {
      cy.wrap(calls).then((value) => {
        const last1 = value.pop()
        const last2 = value.pop()
        expect(last2).to.equal(false)
        expect(last1).to.equal(true)
      })
    }
    describe('Location name', () => {
      it('should emit "valid" with false again', () => {
        const calls = genericTest('#name_1', '1', '2')

        checkValidFalseAgain(calls)
      })
      it('should emit "valid" with true', () => {
        const calls = genericTest('#name_1', '12', '3')

        checkValidTrue(calls)
      })
    })
    describe('City', () => {
      it('should emit "valid" with false again', () => {
        const calls = genericTest('#city_1', 'A', 'b')

        checkValidFalseAgain(calls)
      })
      it('should emit "valid" with true', () => {
        const calls = genericTest('#city_1', 'Ab', 'c')

        checkValidTrue(calls)
      })
    })
    describe('Postal code', () => {
      it('should emit "valid" with false again', () => {
        const calls = genericTest('#postalCode_1', 'A1B', '2C')

        checkValidFalseAgain(calls)
      })
      it('should emit "valid" with true', () => {
        const calls = genericTest('#postalCode_1', 'A1B', '2C3')

        checkValidTrue(calls)
      })
    })
  })
  // TODO: FSADT1-915
  // it('should update the province when changed manually', () => {
  // })
  // it('should update the province when a Street address option gets selected', () => {
  // })
  // describe('when Province is cleared by the user', () => {
  //   /**
  //    * @see FSADT1-914
  //    */
  //   it('should update the Province when a Street address in the same province gets selected', () => {
  //   })

  //   it('should update the Province when a Street address in a different province gets selected', () => {
  //   })
  // })
  // describe('when the following fields are displayed as invalid: City, Province, Postal code', () => {
  //   it('should display them as valid when they get filled by selecting a Street address option', () => {
  //   })
  // })
})
