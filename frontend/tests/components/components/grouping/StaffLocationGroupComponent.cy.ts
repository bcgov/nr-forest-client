import StaffLocationGroupComponent from "@/components/grouping/StaffLocationGroupComponent.vue";
import type { Address } from "@/dto/ApplyClientNumberDto";

// load app validations
import "@/helpers/validators/BCeIDFormValidations";

describe("<StaffLocationGroupComponent />", () => {
  const dummyValidation = (): ((key: string, field: string) => (value: string) => string) => {
    return (key: string, fieldId: string) => (value: string) => {
      if (value.includes("fault")) return "Error";
      return "";
    };
  };

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

  beforeEach(() => {

    cy.fixture("address.json")
      .then((jsonValue) => ({
        ...jsonValue,
        complementaryAddressTwo: null,
      }))
      .as("addressFixture");

    cy.fixture("countries.json").as("countriesFixture");

    cy.fixture("emptyAddress.json")
      .then((jsonValue) => ({
        ...jsonValue,
        complementaryAddressTwo: null,
      }))
      .as("emptyAddressFixture");

    cy.fixture("addressKelownaBC.json")
      .then((jsonValue) => ({
        ...jsonValue,
        complementaryAddressTwo: null,
      }))
      .as("addressKelownaBCFixture");

    cy.intercept("GET", "**/api/codes/countries/CA/provinces?page=0&size=250", {
        fixture: "provinces.json",
      }).as("getProvinces");
  
      cy.intercept("GET", "**/api/codes/countries/US/provinces?page=0&size=250", {
        fixture: "states.json",
      }).as("getStates");

    cy.intercept(
      "GET",
      `**/api/addresses?country=CA&maxSuggestions=10&searchTerm=${encodeURI("2975 Jutland Rd")}*`,
      {
        fixture: "addressSearch.json",
      },
    ).as("searchAddress");

    cy.intercept(
      "GET",
      `**/api/addresses?country=CA&maxSuggestions=10&searchTerm=${encodeURI("158 Hargrave St")}*`,
      {
        fixture: "addressSearchMB.json",
      },
    ).as("searchaddressMB");

    cy.intercept("GET", `**/api/addresses?country=CA&maxSuggestions=10&searchTerm=111`, [
      {
        code: "A1A1A1",
        name: "111 A St Victoria, BC, A1A 1A1",
      },
    ]).as("searchAddress111");

    cy.intercept("GET", "**/api/addresses/V8T5J9", {
      fixture: "address.json",
    }).as("getAddress");

    cy.intercept("GET", "**/api/addresses/R3C3N2", {
      fixture: "addressMB.json",
    }).as("getaddressMB");


  });

  it("should render the component", () => {
    cy.get("@addressFixture").then((address: Address) => {
      cy.get("@countriesFixture").then((countries) => {
        cy.mount(StaffLocationGroupComponent, {
          props: {
            id: 0,
            modelValue: {
              ...address,
              locationName: "Headquarters",
              notes: "Some notes about the location",
            } as Address,
            countryList: countries,
            validations: [],
          },
        });
      });
    });

    cy.wait("@getProvinces");

    cy.get("#name_0").should("be.visible").and("have.value", "Headquarters");

    cy.get("#country_0").should("be.visible").and("have.value", "Canada");

    cy.get("#addr_0")
      .should("be.visible")
      .and("have.id", "addr_0")
      .and("have.value", "2975 Jutland Rd");

    cy.get("#city_0").should("be.visible").and("have.value", "Victoria");

    cy.get("#province_0").should("be.visible").and("have.value", "British Columbia");

    cy.get("#postalCode_0").should("be.visible").and("have.value", "V8T5J9");

    /*
    cy.get("#notes_0")
    This wouldn't work because for some reason the cds-textarea strips off its id attribute.
    Interestingly, Cypress still refers to the component as <cds-textarea#notes_0> in its UI
    messages, after we get it using an alternative selector. Sample message:
    - expected <cds-textarea#notes_0> to be visible.
    
    And though the id is not there as an attribute, it is there as a property, as tested with the
    "have.prop" below.
    */
    cy.get("[data-id='input-notes_0']")
      .should("be.visible")
      .and("have.prop", "id", "notes_0")
      .and("have.value", "Some notes about the location");
  });

  it("should render the component with validation", () => {
    cy.get("@addressFixture").then((address: any) => {
      cy.get("@countriesFixture").then((countries) => {
        cy.mount(StaffLocationGroupComponent, {
          props: {
            id: 0,
            modelValue: {
              ...address,
              locationName: address.locationName + " fault",
            },
            countryList: countries,
            validations: [dummyValidation()],
          },
        });
      });
    });

    cy.wait("@getProvinces");

    cy.get("@addressFixture").then((address: any) => {
      cy.get("#name_0")
        .should("be.visible")
        .and("have.value", address.locationName + " fault");
    });

    cy.get("#name_0")
      .shadow()
      .find(".cds--form-requirement")
      .should("be.visible")
      .find("slot")
      .and("include.text", "Error");
  });

  const deleteSelector1 = "#deleteAddress_1";

  it("should render the component with a Delete button by default when id > 0", () => {
    cy.get("@addressFixture").then((address: any) => {
      cy.get("@countriesFixture").then((countries) => {
        cy.mount(StaffLocationGroupComponent, {
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

    cy.get(deleteSelector1).should("be.visible");
  });

  it("should render the component without a Delete button", () => {
    cy.get("@addressFixture").then((address: any) => {
      cy.get("@countriesFixture").then((countries) => {
        cy.mount(StaffLocationGroupComponent, {
          props: {
            id: 1,
            modelValue: address,
            countryList: countries,
            validations: [],
            hideDeleteButton: true,
          },
        });
      });
    });

    cy.wait("@getProvinces");

    cy.get(deleteSelector1).should("not.exist");
  });

  it("should render the component and show the address name regardless of id being 0", () => {
    cy.get("@addressFixture").then((address) => {
      cy.get("@countriesFixture").then((countries) => {
        cy.mount(StaffLocationGroupComponent, {
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

    cy.get("#name_0").should("be.visible").and("have.value", "Mailing address");
  });

  it("should render the component and reset province when country changes", () => {
    cy.get("@addressFixture").then((address) => {
      cy.get("@countriesFixture").then((countries) => {
        cy.mount(StaffLocationGroupComponent, {
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

    // Wait for the option's inner, standard HTML element to exist before clicking the combo-box
    cy.get("#country_0").find('cds-combo-box-item[data-id="CA"]').shadow().find("div");

    cy.get("#country_0")
      .should("be.visible")
      .and("have.value", "Canada")
      .find("[part='trigger-button']")
      .click();

    cy.get("#country_0", { timeout: 10000 })
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
      .find("[part='trigger-button']")
      .click();

    cy.get("#province_0")
      .find('cds-combo-box-item[data-id="IL"]')
      .should("be.visible")
      .click()
      .and("have.value", "Illinois");
  });

  it("loads pre-filled Street address value properly", () => {
    cy.get("@addressFixture").then((address) => {
      cy.get("@countriesFixture").then((countries) => {
        cy.mount(StaffLocationGroupComponent, {
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
        cy.mount(StaffLocationGroupComponent, {
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

  streetAddressMatchingScenarios.forEach((scenario) => {
    describe(scenario.description, () => {
      it("should update the address related fields", () => {
        cy.get("@emptyAddressFixture").then((emptyAddress) => {
          cy.get("@countriesFixture").then((countries) => {
            cy.mount(StaffLocationGroupComponent, {
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

        cy.get("#addr_0").shadow().find("input").type(scenario.value, { delay: 0 });

        cy.wait("@searchAddress");

        if (scenario.identical) {
          // Sanity check to make sure the option's full text content is the exact same value typed by the user.
          cy.get("cds-combo-box-item").contains(scenario.value).should("have.text", scenario.value);
        }

        cy.get("cds-combo-box-item").contains(scenario.value).click();

        cy.wait("@getAddress");

        cy.get("@addressFixture").then((address: Address) => {
          cy.get("#city_0").should("have.value", address.city);

          cy.get("#province_0").should("be.visible").and("have.value", address.province.text);

          cy.get("#postalCode_0").should("be.visible").and("have.value", address.postalCode);
        });
      });
    });
  });

  describe('when it has last emitted "valid" with false due to a single, not empty, invalid field', () => {
    const genericTest = (
      fieldSelector: string,
      firstContent: string,
      additionalContent: string,
    ) => {
      const calls: boolean[] = [];
      const onValid = (valid: boolean) => {
        calls.push(valid);
      };
      cy.get("@addressFixture").then((address) => {
        cy.get("@countriesFixture").then((countries) => {
          cy.mount(StaffLocationGroupComponent, {
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

      cy.get(fieldSelector).shadow().find("input").clear(); // emits false
      cy.get(fieldSelector).shadow().find("input").should("be.focused").blur();
      cy.get(fieldSelector).shadow().find("input").type(firstContent); // emits true before blurring
      cy.get(fieldSelector).shadow().find("input").should("be.focused").blur();
      cy.get(fieldSelector).should("be.visible").and("have.value", firstContent);
      cy.get(fieldSelector).shadow().find("input").type(additionalContent); // emits true (last2)
      cy.get(fieldSelector).shadow().find("input").should("be.focused").blur();
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
        const calls = genericTest("#name_1", "1234", "5");

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

  describe("province", () => {
    it("should update the province when changed manually", () => {
      cy.get("@addressFixture").then((address) => {
        cy.get("@countriesFixture").then((countries) => {
          cy.mount(StaffLocationGroupComponent, {
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

      cy.get("#province_0").should("be.visible").and("have.value", "British Columbia");

      cy.get("#province_0").find("[part='trigger-button']").click();

      cy.get("#province_0").find('cds-combo-box-item[data-id="MB"]').should("be.visible").click();

      cy.get("#province_0").should("be.visible").and("have.value", "Manitoba");
    });
    it("should update the province when a Street address option gets selected", () => {
      cy.get("@emptyAddressFixture").then((emptyAddress) => {
        cy.get("@countriesFixture").then((countries) => {
          cy.mount(StaffLocationGroupComponent, {
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

      cy.get("@addressFixture").then((address: Address) => {
        cy.get("#province_0").should("be.visible").and("have.value", "British Columbia");
      });

      const typedAddress = "158 Hargrave St";

      cy.get("#addr_0").shadow().find("input").type(typedAddress, { delay: 0 });

      cy.wait("@searchaddressMB");

      cy.get("cds-combo-box-item").contains(typedAddress).click();

      cy.wait("@getaddressMB");

      cy.get("@addressFixture").then((address: Address) => {
        cy.get("#province_0").should("be.visible").and("have.value", "Manitoba");
      });
    });

    describe("when Province has been cleared by the user", () => {
      /**
       * @see FSADT1-914
       */
      it("should update the Province when a Street address in the same province gets selected", () => {
        cy.get("@addressKelownaBCFixture").then((initialAddress) => {
          cy.get("@countriesFixture").then((countries) => {
            cy.mount(StaffLocationGroupComponent, {
              props: {
                id: 0,
                modelValue: initialAddress,
                countryList: countries,
                validations: [],
              },
            });
          });
        });

        cy.wait("@getProvinces");

        // Original province is BC
        cy.get("#province_0").should("be.visible").and("have.value", "British Columbia");

        // Click the clear (x) button
        cy.get("#province_0").shadow().find("#selection-button").click();

        // Province got cleared.
        cy.get("#province_0").should("be.visible").and("have.value", "");

        const typedAddress = "2975 Jutland Rd";

        cy.get("#addr_0").shadow().find("input").clear().type(typedAddress, { delay: 0 });

        cy.wait("@searchAddress");

        cy.get("cds-combo-box-item").contains(typedAddress).click();

        cy.wait("@getAddress");

        // Province is BC again
        cy.get("@addressFixture").then((address: Address) => {
          cy.get("#province_0").should("be.visible").and("have.value", "British Columbia");
        });
      });

      it("should update the Province when a Street address in a different province gets selected", () => {
        cy.get("@addressKelownaBCFixture").then((initialAddress) => {
          cy.get("@countriesFixture").then((countries) => {
            cy.mount(StaffLocationGroupComponent, {
              props: {
                id: 0,
                modelValue: initialAddress,
                countryList: countries,
                validations: [],
              },
            });
          });
        });

        cy.wait("@getProvinces");

        // Original province is BC
        cy.get("#province_0").should("be.visible").and("have.value", "British Columbia");

        // Click the clear (x) button
        cy.get("#province_0").shadow().find("#selection-button").click();

        // Province got cleared.
        cy.get("#province_0").should("be.visible").and("have.value", "");

        const typedAddress = "158 Hargrave St";

        cy.get("#addr_0").shadow().find("input").clear().type(typedAddress, { delay: 0 });

        cy.wait("@searchaddressMB");

        cy.get("cds-combo-box-item").contains(typedAddress).click();

        cy.wait("@getaddressMB");

        // Province is now MB
        cy.get("@addressFixture").then((address: Address) => {
          cy.get("#province_0").should("be.visible").and("have.value", "Manitoba");
        });
      });
    });
  });

  describe("when the following fields are displayed as invalid: City, Province, Postal code", () => {

    it("should display them as valid once they get filled by selecting a Street address option", () => {

      cy.get("@emptyAddressFixture").then((emptyAddress) => {
        cy.get("@countriesFixture").then((countries) => {
          cy.mount(StaffLocationGroupComponent, {
            props: {
              id: 0,
              modelValue: emptyAddress,
              countryList: countries,
              validations: [],
            },
          });
        });
      });
      
      cy.get("#city_0").shadow().find("input").type("Test");
      cy.log("City field is filled with Test");
      cy.wait(15);

      cy.get("#city_0").shadow().find("input").clear();
      cy.wait(15);
      cy.get("#city_0").shadow().find("input").should('be.focused').blur();
      cy.log("City field is cleared and blured");
      cy.wait(15);

      cy.get("#province_0").shadow().find("#selection-button").click();
      cy.log("Province field is clicked");
      
      cy.get("#postalCode_0").shadow().find("input").type("Test");
      cy.log("Postal code field is filled with Test");

      cy.get("#postalCode_0").shadow().find("input").clear();
      cy.get("#postalCode_0").shadow().find("input").click();
      cy.get("#postalCode_0").shadow().find("input").should('be.focused').blur();
      cy.log("Postal code field is cleared and clicked");

      // Fields are displayed as invalid
      cy.get("#city_0").shadow().find("input").should("have.class", "cds--text-input--invalid");
      cy.get("#province_0").shadow().find("div").should("have.class", "cds--dropdown--invalid");
      cy.get("#postalCode_0").shadow().find("input").should("have.class", "cds--text-input--invalid");

      const typedAddress = "2975 Jutland Rd";

      cy.get("#addr_0")
        .shadow()
        .find("input")
        .type(typedAddress, { delay: 0 });

      cy.wait("@searchAddress");

      cy.get("cds-combo-box-item").contains(typedAddress).click();

      cy.wait("@getAddress");

      // Fields are now displayed as valid
      cy.get("#city_0").shadow().find("input").should("not.have.class", "cds--text-input--invalid");
      cy.get("#province_0").shadow().find("div").should("not.have.class", "cds--dropdown--invalid");
      cy.get("#postalCode_0").shadow().find("input").should("not.have.class", "cds--text-input--invalid");

    });
  });
  

});
