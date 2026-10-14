/* eslint-disable no-undef */
describe("Staff Form Submission", () => {
  /* Test variables and functions */
  const API_BASE = "http://localhost:8080/api";

  const fillIndividual = (extraData: any = {}) => {
    cy.fixture("testdata/individualBaseData").then((fixtureData: any) => {
      const data = { ...fixtureData, ...extraData };

      cy.get("#clientType").should("be.visible").and("have.value", "");
      cy.selectFormEntry("#clientType", "Individual", false);
      cy.fillFormEntry("#firstName", data.firstName);
      cy.fillFormEntry("#middleName", data.middleName);
      cy.fillFormEntry("#lastName", data.lastName);
      cy.selectFormEntry(
        "#identificationType",
        data.identificationTypeValue,
        false
      );
      cy.wait(10);

      if (data.identificationProvinceValue) {
        cy.selectFormEntry(
          "#identificationProvince",
          data.identificationProvinceValue,
          false
        );
        cy.wait(10);
      }

      cy.fillFormEntry("#clientIdentification", data.clientIdentification);
      cy.fillFormEntry("#birthdateYear", data.birthdateYear);
      cy.fillFormEntry("#birthdateMonth", data.birthdateMonth);
      cy.fillFormEntry("#birthdateDay", data.birthdateDay);
    });
  };

  const fillLocation = (index: number, extraData: any = {}) => {
    cy.fixture("testdata/locationBaseData").then((fixtureData: any) => {
      const data = { ...fixtureData, ...extraData };
      
      cy.selectAutocompleteEntry(`#addr_${index}`, data.addr, data.addressId);
    });
  };

  const fillContactWithoutName = (extraData: any = {}) => {
    return fillContact(0, extraData, true);
  };

  /**
   * @returns Cypress chainable whose subject is the resulting contact data.
   */
  const fillContact = (index: number, extraData: any = {}, skipName = false) => {
    return cy
      .fixture<Record<string, string>>("testdata/contactBaseData")
      .then((fixtureData: any) => {
      const data = { ...fixtureData, ...extraData };
      if (!skipName) {
        cy.fillFormEntry(`#firstName_${index}`, data.firstName);
        cy.fillFormEntry(`#lastName_${index}`, data.lastName);
      }
      cy.fillFormEntry(`#emailAddress_${index}`, data.emailAddress);
      cy.fillFormEntry(`#businessPhoneNumber_${index}`, data.businessPhoneNumber);
      cy.fillFormEntry(`#secondaryPhoneNumber_${index}`, data.secondaryPhoneNumber);
      cy.fillFormEntry(`#faxNumber_${index}`, data.faxNumber);
      cy.wait("@getRoles");
      cy.selectFormEntry(`#role_${index}`, data.role, false);
      cy.selectFormEntry(`#addressname_${index}`, data.addressname, true);

      // Yelds the contact data
      cy.wrap(data);
    });
  };

  const clickNext = () => {
    cy.get("[data-test='wizard-next-button']")
      .shadow()
      .find("button")
      .should("be.enabled");
    cy.get("[data-test='wizard-next-button']").click();
  };

  beforeEach(function () {
    cy.viewport(1920, 1080);

    cy.intercept("GET", `${API_BASE}/codes/identification-types`, {
      fixture: "identificationTypes.json",
      headers: {
        "content-type": "application/json;charset=UTF-8",
      },
    }).as("getIdentificationTypes");

    cy.intercept("GET", `${API_BASE}/codes/countries?page=0&size=250`, {
      fixture: "countries.json",
      headers: {
        "content-type": "application/json;charset=UTF-8",
      },
    }).as("getCountries");

    cy.intercept(
      "GET",
      `${API_BASE}/codes/countries/CA/provinces?page=0&size=250`,
      {
        fixture: "provinces.json",
        headers: {
          "content-type": "application/json;charset=UTF-8",
        },
      }
    ).as("getProvinces");

    cy.intercept(
      "GET",
      `${API_BASE}/addresses?country=CA&maxSuggestions=10&searchTerm=297`,
      {
        fixture: "addressSearch.json",
        headers: {
          "content-type": "application/json;charset=UTF-8",
        },
      }
    ).as("getAddressAutoComplete");

    cy.intercept(
      "GET",
      `${API_BASE}/addresses?country=CA&maxSuggestions=10&searchTerm=2975%20Jutland%20Rd%20Victoria,%20BC,%20V8T%205J9`,
      {
        fixture: "addressSearch.json",
        headers: {
          "content-type": "application/json;charset=UTF-8",
        },
      }
    ).as("getAddressAutoCompleteSelect1");

    cy.intercept(
      "GET",
      `${API_BASE}/addresses?country=CA&maxSuggestions=10&searchTerm=2975%20Jutland%20Rd`,
      {
        fixture: "addressSearch.json",
        headers: {
          "content-type": "application/json;charset=UTF-8",
        },
      }
    ).as("getAddressAutoCompleteSelect2");

    cy.intercept("GET", `${API_BASE}/addresses/V8T5J9`, {
      fixture: "address.json",
      headers: {
        "content-type": "application/json;charset=UTF-8",
      },
    }).as("getAddressValue");

    cy.intercept("GET", `${API_BASE}/codes/contact-types?page=0&size=250`, {
      fixture: "roles.json",
      headers: {
        "content-type": "application/json;charset=UTF-8",
      },
    }).as("getRoles");

    cy.intercept("GET", `${API_BASE}/codes/client-types/I`, {
      fixture: "clientTypeIndividual.json",
      headers: {
        "content-type": "application/json;charset=UTF-8",
      },
    }).as("getClientType");

    const testSubmissionFixture = this.currentTest.title
      .replace("should be ", "")
      .replaceAll(" ", "-");

    cy.fixture(`staffSubmit/${testSubmissionFixture}`).then(
      (fixtureData: any) => {
        cy.intercept("POST", "/api/clients/submissions/staff", fixtureData).as(
          "submitForm"
        );
      }
    );

    cy.intercept("POST", "**/api/clients/matches", {
      statusCode: 204,
      headers: {
        "content-type": "application/json;charset=UTF-8",
      },
    }).as("doMatch");

    cy.visit("/");

    cy.get("#landing-title").should(
      "contain",
      "Forests Client Management System"
    );

    cy.get("#landing-subtitle").should(
      "contain",
      "Create and manage client accounts"
    );

    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_ADMIN"],
    });

    // Check if the Create client button is visible
    cy.get("#menu-list-staff-form").should("be.visible").click();

    cy.get("h1").should("be.visible").should("contain", " Create client ");
  });

  it("should be success message", () => {
    fillIndividual();
    clickNext();
    cy.contains("h2", "Locations");
    fillLocation(0);
    clickNext();
    cy.contains("h2", "Contacts");
    fillContactWithoutName();
    clickNext();
    cy.contains("h2", "Review");

    cy.get("[data-test='wizard-submit-button']").click();
    cy.get("h1").should("contain", "New client 00123456 has been created!");
  });

  it("should be timeout message", () => {
    fillIndividual();
    clickNext();
    cy.contains("h2", "Locations");
    fillLocation(0);
    clickNext();
    cy.contains("h2", "Contacts");
    fillContactWithoutName().then((contactData) => {
      clickNext();
      cy.contains("h2", "Review");
  
      cy.fillFormEntry("cds-textarea", "error", 10, true);
  
      cy.get("[data-test='wizard-submit-button']").click();
      cy.wait("@submitForm").then((interception) => {
        cy.wait(5000);
        cy.get("h1").should("contain", "This submission is being processed");

        // message contains the email address
        cy.contains(
          `We’ll send the client number and submission details to ${contactData.emailAddress} when the process is complete.`,
        );

        // button contains the submission id
        cy.get("cds-button[href='/submissions/4444']").should("exist");
      });
    });
  });

  it("should be validation error message", () => {
    fillIndividual({
      identificationTypeValue: "Canadian passport",
      identificationProvinceValue: undefined,
      clientIdentification: "AB345678",
    });
    clickNext();
    cy.contains("h2", "Locations");
    fillLocation(0);
    clickNext();
    cy.contains("h2", "Contacts");
    fillContactWithoutName();
    clickNext();
    cy.contains("h2", "Review");

    cy.get("[data-test='wizard-submit-button']").click();
    cy.get("cds-actionable-notification")
      .should("be.visible")
      .and("have.attr", "kind", "error")
      .shadow()
      .find(
        "div.cds--actionable-notification__details div.cds--actionable-notification__text-wrapper div.cds--actionable-notification__content div.cds--actionable-notification__title"
      )
      .should("contain", "Your application could not be submitted:");
  });
});
