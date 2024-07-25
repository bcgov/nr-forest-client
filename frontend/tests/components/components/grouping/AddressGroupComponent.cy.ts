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
    cy.intercept("GET", "/api/codes/countries/CA/provinces?page=0&size=250", {
      fixture: "provinces.json",
    }).as("getProvinces");

    cy.intercept("GET", "/api/codes/countries/US/provinces?page=0&size=250", {
      fixture: "states.json",
    }).as("getStates");

    cy.fixture("address.json").as("addressFixture");
    cy.fixture("countries.json").as("countriesFixture");

    cy.fixture("emptyAddress.json").as("emptyAddressFixture");

    cy.fixture("addressKelownaBC.json").as("addressKelownaBCFixture");

    cy.intercept(
      "GET",
      `/api/addresses?country=CA&maxSuggestions=10&searchTerm=${encodeURI(
        "2975 Jutland Rd"
      )}*`,
      {
        fixture: "addressSearch.json",
      }
    ).as("searchAddress");

    cy.intercept(
      "GET",
      `/api/addresses?country=CA&maxSuggestions=10&searchTerm=${encodeURI(
        "158 Hargrave St"
      )}*`,
      {
        fixture: "addressSearchMB.json",
      }
    ).as("searchaddressMB");

    cy.intercept(
      "GET",
      `/api/addresses?country=CA&maxSuggestions=10&searchTerm=111`,
      [
        {
          code: "A1A1A1",
          name: "111 A St Victoria, BC, A1A 1A1",
        },
      ]
    ).as("searchAddress111");

    cy.intercept("GET", "/api/addresses/V8T5J9", {
      fixture: "address.json",
    }).as("getAddress");

    cy.intercept("GET", "/api/addresses/R3C3N2", {
      fixture: "addressMB.json",
    }).as("getaddressMB");
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

});