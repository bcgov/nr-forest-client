import AddressGroupComponent from "@/components/grouping/AddressGroupComponent.vue";
import type { Address } from "@/dto/ApplyClientNumberDto";

// load app validations
import "@/helpers/validators/BCeIDFormValidations";

describe("<AddressGroupComponent />", () => {
  const dummyValidation = (): ((
    key: string,
    field: string
  ) => (value: string) => string) => {
    return (key: string, fieldId: string) => (value: string) => {
      if (value.includes("fault")) return "Error";
      return "";
    };
  };

  beforeEach(() => {
    cy.intercept("GET", "/api/clients/activeCountryCodes/CA?page=0&size=250", {
      fixture: "provinces.json",
    }).as("getProvinces");

    cy.intercept("GET", "/api/clients/activeCountryCodes/US?page=0&size=250", {
      fixture: "states.json",
    }).as("getStates");

    cy.fixture("address.json").as("addressFixture");
    cy.fixture("countries.json").as("countriesFixture");

    cy.fixture("emptyAddress.json").as("emptyAddressFixture");

    cy.intercept(
      "GET",
      `/api/clients/addresses?country=CA&maxSuggestions=10&searchTerm=${encodeURI(
        "2975 Jutland Rd"
      )}*`,
      {
        fixture: "addressSearch.json",
      }
    ).as("searchAddress");

    cy.intercept(
      "GET",
      `/api/clients/addresses?country=CA&maxSuggestions=10&searchTerm=111`,
      [
        {
          code: "A1A1A1",
          name: "111 A St Victoria, BC, A1A 1A1",
        },
      ]
    ).as("searchAddress111");

    cy.intercept("GET", "/api/clients/addresses/V8T5J9", {
      fixture: "address.json",
    }).as("getAddress");
  });

  it("should render the component", () => {
    cy.get("@addressFixture").then((address) => {
      cy.get("@countriesFixture").then((countries) => {
        cy.mount(AddressGroupComponent, {
          props: {
            id: 0,
            modelValue: address,
            countryList: countries,
            validations: [],
          },
        });
      });
    });

    cy.wait("@getProvinces");

    cy.get("#country_0").should("be.visible").and("have.value", "Canada");

    cy.get("#addr_0")
      .should("be.visible")
      .and("have.id", "addr_0")
      .and("have.value", "2975 Jutland Rd");

    cy.get("#city_0").should("be.visible").and("have.value", "Victoria");

    cy.get("#province_0")
      .should("be.visible")
      .and("have.value", "British Columbia");

    cy.get("#postalCode_0").should("be.visible").and("have.value", "V8T5J9");
  });

  it("should render the component with validation", () => {
    cy.get("@addressFixture").then((address: any) => {
      cy.get("@countriesFixture").then((countries) => {
        cy.mount(AddressGroupComponent, {
          props: {
            id: 0,
            modelValue: {
              ...address,
              streetAddress: address.streetAddress + " fault",
            },
            countryList: countries,
            validations: [dummyValidation()],
          },
        });
      });
    });

    cy.wait("@getProvinces");

    cy.get("@addressFixture").then((address: any) => {
      cy.get("#addr_0")
        .should("be.visible")
        .and("have.value", address.streetAddress + " fault");
    });

    cy.get("#postalCode_0")
      .shadow()
      .find(".cds--form-requirement")
      .should("be.visible")
      .find("slot")
      .and("include.text", "Error");
  });

  it("should render the component and set focus on street address input", () => {
    cy.get("@addressFixture").then((address) => {
      cy.get("@countriesFixture").then((countries) => {
        cy.mount(AddressGroupComponent, {
          props: {
            id: 0,
            modelValue: address,
            countryList: countries,
            validations: [],
          },
        });
      });
    });

    cy.wait("@getProvinces");

    cy.get("#addr_0").should("be.visible").and("have.focus");
  });

  it("should render the component and show the address name if id is bigger than 0", () => {
    cy.get("@addressFixture").then((address) => {
      cy.get("@countriesFixture").then((countries) => {
        cy.mount(AddressGroupComponent, {
          props: {
            id: 1,
            modelValue: address,
            countryList: countries,
            validations: [],
          },
        });
      });
    });

    cy.wait("@getProvinces");

    cy.get("#name_1")
      .should("be.visible")
      .and("have.value", "Mailing address")
      .and("have.focus");
  });

  it("should render the component and reset province when country changes", () => {
    cy.get("@addressFixture").then((address) => {
      cy.get("@countriesFixture").then((countries) => {
        cy.mount(AddressGroupComponent, {
          props: {
            id: 0,
            modelValue: address,
            countryList: countries,
            validations: [],
          },
        });
      });
    });

    cy.wait("@getProvinces");

    /*cy.get("#postalCode_0")
      .should("be.visible")
      .shadow()
      .find("label")
      .and("include.text", "Postal code");*/

    // Wait for the option's inner, standard HTML element to exist before clicking the combo-box
    cy.get("#country_0")
      .find('cds-combo-box-item[data-id="US"]')
      .shadow()
      .find("div");

    cy.get("#country_0")
      .should("be.visible")
      .and("have.value", "Canada")
      .click();

    cy.get("#country_0")
      .find('cds-combo-box-item[data-id="US"]')
      .should("be.visible")
      .click()
      .and("have.value", "United States of America");

    cy.wait("@getStates");

    // Value effectively displayed has got cleared (see FSADT1-900).
    cy.get("#province_0").find("input").should("have.value", "");

    cy.get("#province_0")
      .should("be.visible")
      .and("have.value", "")
      .click()
      .find('cds-combo-box-item[data-id="IL"]')
      .should("be.visible")
      .click()
      .and("have.value", "Illinois");

    cy.get("#postalCode_0")
      .should("be.visible")
      .shadow()
      .find("label")
      .and("include.text", "Zip code");
  });

  const streetAddressMatchingScenarios = [
    {
      description: "when a partial matching address is typed in",
      value: "2975 Jutland Rd",
      identical: false,
    },
    {
      description: "when a complete matching address is typed in",
      value: "2975 Jutland Rd Victoria, BC, V8T 5J9",
      identical: true,
    },
  ];
  streetAddressMatchingScenarios.forEach((scenario) => {
    describe(scenario.description, () => {
      it("should update the address related fields", () => {
        cy.get("@emptyAddressFixture").then((emptyAddress) => {
          cy.get("@countriesFixture").then((countries) => {
            cy.mount(AddressGroupComponent, {
              props: {
                id: 0,
                modelValue: emptyAddress,
                countryList: countries,
                validations: [],
              },
            });
          });
        });

        cy.wait("@getProvinces");

        cy.get("#addr_0")
          .shadow()
          .find("input")
          .type(scenario.value, { delay: 0 });

        cy.wait("@searchAddress");

        if (scenario.identical) {
          // Sanity check to make sure the option's full text content is the exact same value typed by the user.
          cy.get("cds-combo-box-item")
            .contains(scenario.value)
            .should("have.text", scenario.value);
        }

        cy.get("cds-combo-box-item").contains(scenario.value).click();

        cy.wait("@getAddress");

        cy.get("@addressFixture").then((address: Address) => {
          cy.get("#city_0").should("have.value", address.city);

          cy.get("#province_0")
            .should("be.visible")
            .and("have.value", address.province.text);

          cy.get("#postalCode_0")
            .should("be.visible")
            .and("have.value", address.postalCode);
        });
      });
    });
  });

  it("loads pre-filled Street address value properly", () => {
    cy.get("@addressFixture").then((address) => {
      cy.get("@countriesFixture").then((countries) => {
        cy.mount(AddressGroupComponent, {
          props: {
            id: 0,
            modelValue: address,
            countryList: countries,
            validations: [],
          },
        });
      });
    });

    cy.wait("@getProvinces");

    cy.get("#addr_0").should("be.visible").and("have.value", "2975 Jutland Rd");
  });

  it("keeps working normally to search and load new address options after clearing the Street address with the x button", () => {
    cy.get("@addressFixture").then((address) => {
      cy.get("@countriesFixture").then((countries) => {
        cy.mount(AddressGroupComponent, {
          props: {
            id: 0,
            modelValue: address,
            countryList: countries,
            validations: [],
          },
        });
      });
    });

    cy.wait("@getProvinces");

    cy.get("#addr_0").should("be.visible").and("have.value", "2975 Jutland Rd");

    // Click the clear (x) button
    cy.get("#addr_0").shadow().find("#selection-button").click();

    cy.get("#addr_0").should("have.value", "");

    cy.get("#addr_0").shadow().find("input").type("111", { delay: 0 });

    cy.wait("@searchAddress111");

    cy.get("cds-combo-box-item").contains("111 A St Victoria, BC, A1A 1A1");
  });

  /**
   * @see FSADT1-905
   * @see FSADT1-907
   */
  describe('when it has last emitted "valid" with false due to a single, not empty, invalid field', () => {
    const genericTest = (
      fieldSelector: string,
      firstContent: string,
      additionalContent: string
    ) => {
      const calls: boolean[] = [];
      const onValid = (valid: boolean) => {
        calls.push(valid);
      };
      cy.get("@addressFixture").then((address) => {
        cy.get("@countriesFixture").then((countries) => {
          cy.mount(AddressGroupComponent, {
            props: {
              id: 1,
              modelValue: address,
              countryList: countries,
              validations: [],
              onValid,
            },
          });
        });
      });
      cy.wait("@getProvinces");

      // Prevents issues due to moving focus in the middle of the test.
      cy.get("#name_1").should("be.focused");

      cy.get(fieldSelector).shadow().find("input").clear(); // emits false
      cy.get(fieldSelector).blur(); // (doesn't emit)
      cy.get(fieldSelector).shadow().find("input").type(firstContent); // emits true before blurring
      cy.get(fieldSelector).blur(); // emits false
      cy.get(fieldSelector)
        .should("be.visible")
        .and("have.value", firstContent);
      cy.get(fieldSelector).shadow().find("input").type(additionalContent); // emits true (last2)
      cy.get(fieldSelector).blur(); // emits false (last1)
      cy.get(fieldSelector)
        .should("be.visible")
        .and("have.value", `${firstContent}${additionalContent}`);

      // For some reason on Electron we need to wait a millisecond now.
      // Otherwise this test either fails or can't be trusted on Electron.
      cy.wait(1);

      return calls;
    };
    const checkValidFalseAgain = (calls: boolean[]) => {
      cy.wrap(calls).then((value) => {
        const last1 = value.pop();
        const last2 = value.pop();
        const last3 = value.pop();
        expect(last3).to.equal(false);
        expect(last2).to.equal(true);
        expect(last1).to.equal(false);
      });
    };
    const checkValidTrue = (calls: boolean[]) => {
      cy.wrap(calls).then((value) => {
        const last1 = value.pop();
        const last2 = value.pop();
        expect(last2).to.equal(false);
        expect(last1).to.equal(true);
      });
    };
    describe("Location name", () => {
      it('should emit "valid" with false again', () => {
        const calls = genericTest("#name_1", "1", "2");

        checkValidFalseAgain(calls);
      });
      it('should emit "valid" with true', () => {
        const calls = genericTest("#name_1", "12", "3");

        checkValidTrue(calls);
      });
    });
    describe("City", () => {
      it('should emit "valid" with false again', () => {
        const calls = genericTest("#city_1", "A", "b");

        checkValidFalseAgain(calls);
      });
      it('should emit "valid" with true', () => {
        const calls = genericTest("#city_1", "Ab", "c");

        checkValidTrue(calls);
      });
    });
    describe("Postal code", () => {
      it('should emit "valid" with false again', () => {
        const calls = genericTest("#postalCode_1", "A1B", "2C");

        checkValidFalseAgain(calls);
      });
      it('should emit "valid" with true', () => {
        const calls = genericTest("#postalCode_1", "A1B", "2C3");

        checkValidTrue(calls);
      });
    });
  });
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
});
