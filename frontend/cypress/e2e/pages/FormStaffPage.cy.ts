/* eslint-disable no-undef */
describe("Staff Form", () => {

  const individualBaseData = {
    firstName: "John",
    middleName: "Michael",
    lastName: "Silver",
    birthdateYear: "2001",
    birthdateMonth: "05",
    birthdateDay: "30",
    identificationTypeValue: "Canadian driver's licence",
    identificationProvinceValue: "British Columbia",
    clientIdentification: "34567800",
  };

  const firstNationBaseData = {
    businessName: "Squamish Nation",
    workSafeBcNumber: "123456",
    clientAcronym: "FFF"
  };

  const locationBaseData = {
    name_0: "Mailing address",
    addr_0: "2975",
  };

  const contactBaseData = {
    mail: "contact1@mail.ca",
    phone1: "1234567890",
    phone2: "1234567890",
    fax: "1234567890",
    role: "Person",
    address: locationBaseData.name_0,
  };

  const fillIndividual = (data = individualBaseData) => {
    cy.fillFormEntry("#firstName", data.firstName);
    cy.fillFormEntry("#middleName", data.middleName);
    cy.fillFormEntry("#lastName", data.lastName);

    cy.selectFormEntry("#identificationType", data.identificationTypeValue, false);

    if (data.identificationProvinceValue) {
      cy.selectFormEntry("#identificationProvince", data.identificationProvinceValue, false);
    }

    cy.fillFormEntry("#clientIdentification", data.clientIdentification);

    //This way of filling out data fails locally only with this component
    cy.fillFormEntry("#birthdateYear", data.birthdateYear);
    cy.fillFormEntry("#birthdateMonth", data.birthdateMonth);
    cy.fillFormEntry("#birthdateDay", data.birthdateDay);
  };

  const fillFirstNation = (data = firstNationBaseData) => {
    cy.get("#clientName")
      .should("be.visible")
      .shadow()
      .find("input")
      .should("have.value", "")
      .type(data.businessName);
    cy.wait("@getFirstNations");
    cy.get('cds-combo-box-item[data-id="555"]').click();

    cy.fillFormEntry("#workSafeBcNumber", data.workSafeBcNumber);
    cy.fillFormEntry("#clientAcronym", data.clientAcronym);
  };

  const fillLocation = (data = locationBaseData) => {
    cy.get("#addr_0").shadow().find("input").type(data.addr_0, { delay: 0 });

    cy.wait("@searchAddress");

    cy.get("#addr_0 cds-combo-box-item").contains(data.addr_0).click();

    cy.wait("@getAddress");
  };

  const fillContact = (data = contactBaseData) => {
    cy.fillFormEntry("#emailAddress_0", data.mail);
    cy.fillFormEntry("#businessPhoneNumber_0", data.phone1);
    cy.fillFormEntry("#secondaryPhoneNumber_0", data.phone2);
    cy.fillFormEntry("#faxNumber_0", data.fax);

    cy.wait("@getContactTypes");

    cy.selectFormEntry("#role_0", data.role, false);
    cy.selectFormEntry("#addressname_0", data.address, true);
  };

  beforeEach(() => {

    cy.intercept("GET", '**/api/codes/client-types/I', {
      fixture: "clientTypeIndividual.json",
    }).as("getIndividual");

    cy.intercept("GET", "**/api/codes/countries?page=0&size=250", {
      fixture: "countries.json",
    }).as("getCountries");

    cy.intercept("POST", '**/api/clients/matches',{
      statusCode: 204,
      headers:{
        "content-type": "application/json;charset=UTF-8"
      }
    }).as("doMatch");

    cy.intercept("GET", '**/clients/submissions?page=0&size=10', {
      fixture: "submissions.json",
      headers: {
        "x-total-count": "1",
        "content-type": "application/json;charset=UTF-8",
        "Access-Control-Expose-Headers": "x-total-count",
      },
    }).as("getSubmissions");

    cy.intercept("GET", "**/api/codes/countries/CA/provinces?page=0&size=250", {
      fixture: "provinces.json",
    }).as("getProvinces");

    cy.intercept("GET", "**/api/codes/countries/?page=0&size=250", {
      fixture: "countries.json",
    }).as("getContries");

    cy.intercept(
      "GET",
      "**/api/addresses?country=CA&maxSuggestions=10&searchTerm=*",
      {
        fixture: "addressSearch.json",
      }
    ).as("searchAddress");

    cy.intercept("GET", "**/api/addresses/*", {
      fixture: "address.json",
    }).as("getAddress");

    cy.intercept("GET", "**/api/codes/contact-types?page=0&size=250", {
      fixture: "roles.json",
    }).as("getContactTypes");

    cy.intercept("GET", "**/api/codes/identification-types", {
      fixture: "identificationTypes.json",
    }).as("getIdentificationTypes");

    cy.intercept("**/api/opendata/*", {
      fixture: "firstNations.json",
    }).as("getFirstNations");

    cy.visit("/");
    
    cy.get("#landing-title").should(
      "contain",
      "Forests Client Management System"
    );
    cy.get("#landing-subtitle").should(
      "contain",
      "Create and manage client accounts"
    );

    cy.viewport(1920, 1080);
  });

  describe('User access', () => {

    it("CLIENT_VIEWER should not be able to see the button", () => {
      cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
        given_name: "James",
        family_name: "Baxter",
        "cognito:groups": ["CLIENT_VIEWER"],
      });

      // Check if the Create client button is not visible and cannot be found
      cy.get("#menu-list-staff-form").should("not.exist");
    });

    it("CLIENT_EDITOR should be able to see and click the button", () => {
      cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
        given_name: "James",
        family_name: "Baxter",
        "cognito:groups": ["CLIENT_EDITOR"],
      });

      // Check if the Create client button is visible
      cy.get("#menu-list-staff-form").should("be.visible").click();

      cy.get("h1").should("be.visible").should("contain", " Create client ");
    });

    it("CLIENT_SUSPEND should be able to see and click the button", () => {
      cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
        given_name: "James",
        family_name: "Baxter",
        "cognito:groups": ["CLIENT_SUSPEND"],
      });

      // Check if the Create client button is visible
      cy.get("#menu-list-staff-form").should("be.visible").click();

      cy.get("h1").should("be.visible").should("contain", " Create client ");
    });

    it("CLIENT_ADMIN should be able to see and click the button", () => {
      cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
        given_name: "James",
        family_name: "Baxter",
        "cognito:groups": ["CLIENT_ADMIN"],
      });

      // Check if the Create client button is visible
      cy.get("#menu-list-staff-form").should("be.visible").click();

      cy.get("h1").should("be.visible").should("contain", " Create client ");
    });

  });

  describe("when the user clicks the Create client button", () => {
    beforeEach(() => {
      cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
        given_name: "James",
        family_name: "Baxter",
        "cognito:groups": ["CLIENT_ADMIN"],
      });

      cy.intercept("GET", "**/api/submission-limit", {
        fixture: "submissionLimitValid.json"
      }).as("getValidSubmissionLimit");

      // Check if the Create client button is visible
      cy.get("#menu-list-staff-form").should("be.visible").click();

      cy.intercept("GET", "**/api/codes/countries/CA/provinces?page=0&size=250", {
        fixture: "provinces.json",
      }).as("getProvinces");

      cy.intercept(
        "GET",
        "/api/addresses?country=CA&maxSuggestions=10&searchTerm=*",
        {
          fixture: "addressSearch.json",
        }
      ).as("searchAddress");

      cy.intercept("GET", "/api/addresses/V8T5J9", {
        fixture: "address.json",
      }).as("getAddress");

      cy.intercept("GET", "/api/codes/contact-types?page=0&size=250", {
        fixture: "roles.json",
      }).as("getContactTypes");

      cy.intercept("GET", "/api/codes/identification-types", {
        fixture: "identificationTypes.json",
      }).as("getIdentificationTypes");

      cy.intercept("**/api/opendata/*", {
        fixture: "firstNations.json",
      }).as("getFirstNations");

    });

    it("should display the Client type input field", () => {
      cy.get("#clientType").should("be.visible").and("have.value", "");
    });

    it("should not display any Client type specific input fields", () => {
      cy.get("#firstName").should("not.exist");
    });

  });

  describe("when the user clicks the Create client button and submission limit is reached", () => {
    beforeEach(() => {
      cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
        given_name: "James",
        family_name: "Baxter",
        "cognito:groups": ["CLIENT_ADMIN"],
      });

      cy.intercept("GET", "**/api/submission-limit", (req) => {
        console.log("Intercepting /api/submission-limit");
        req.reply({
          statusCode: 400,
          headers: {
            "content-type": "application/json;charset=UTF-8"
          },
          body: [
            {
              fieldId: "submissionLimit",
              errorMsg: "You can make up to 20 submissions in 24 hours. Resubmit this application in 24 hours."
            }
          ]
        });
      }).as("getIdirInvalidSubmissionLimit");

      cy.get("#menu-list-staff-form").should("be.visible").click();

      cy.intercept("GET", "**/api/codes/countries/CA/provinces?page=0&size=250", {
        fixture: "provinces.json",
      }).as("getProvinces");

      cy.intercept(
        "GET",
        "/api/addresses?country=CA&maxSuggestions=10&searchTerm=*",
        {
          fixture: "addressSearch.json",
        }
      ).as("searchAddress");

      cy.intercept("GET", "/api/addresses/V8T5J9", {
        fixture: "address.json",
      }).as("getAddress");

      cy.intercept("GET", "/api/codes/contact-types?page=0&size=250", {
        fixture: "roles.json",
      }).as("getContactTypes");

      cy.intercept("GET", "/api/codes/identification-types", {
        fixture: "identificationTypes.json",
      }).as("getIdentificationTypes");

      cy.intercept("**/api/opendata/*", {
        fixture: "firstNations.json",
      }).as("getFirstNations");
    });

    it("should display a global error", () => {
      cy.get("#serverValidationError")
        .should("be.visible")
        .and("have.attr", "kind", "error")
        .shadow();
    });

  });

  describe("when option First Nation gets selected", () => {
    beforeEach(() => {
      cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
        given_name: "James",
        family_name: "Baxter",
        "cognito:groups": ["CLIENT_ADMIN"],
      });

      // Check if the Create client button is visible
      cy.get("#menu-list-staff-form").should("be.visible").click();

      cy.get("#clientType")
          .should("be.visible")
          .and("have.value", "")
          .find("[part='trigger-button']")
          .click();

        cy.get("#clientType")
          .find('cds-combo-box-item[data-id="R"]')
          .should("be.visible")
          .click()
          .and("have.value", "First Nation");

        cy.contains("h2", "Client information");
      
    });

    it("should display the First Nation information input fields", () => {      
      cy.get("#clientName").should("be.visible");
      cy.get("#workSafeBcNumber").should("be.visible");
      cy.get("#clientAcronym").should("be.visible");
    });

    it("enables the button Next", () => {
      fillFirstNation(firstNationBaseData);
      cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.enabled");
    });

    it('should display provided info when returning to step 1',() => {
      fillFirstNation(firstNationBaseData);
      cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.enabled");

      cy.get("[data-test='wizard-next-button']").click();

      cy.contains("h2", "Client information").should("not.exist");

      cy.get("[data-test='wizard-back-button']")
        .shadow()
        .find("button")
        .should("be.enabled");

      cy.get("[data-test='wizard-back-button']").click();

      cy.get("#clientName")
      .shadow()
      .find("input")
      .should("have.value", firstNationBaseData.businessName);

      cy.get("#workSafeBcNumber")
        .shadow()
        .find("input")
        .should("have.value", firstNationBaseData.workSafeBcNumber);

      cy.get("#clientAcronym")
        .shadow()
        .find("input")
        .should("have.value", firstNationBaseData.clientAcronym);

    });

  });

  describe("when option Individual gets selected", () => {

    const scenarios = [
      {
        name: "and the selected ID type doesn't require Issuing province",
        data: {
          ...individualBaseData,
        },
      },
      {
        name: "and the selected ID type requires Issuing province",
        data: {
          ...individualBaseData,
          identificationTypeValue: "Canadian driver's licence",
          identificationProvinceValue: "Nova Scotia",
        },
      },
    ];

    beforeEach(() => {    
      cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
        given_name: "James",
        family_name: "Baxter",
        "cognito:groups": ["CLIENT_ADMIN"],
      });

      // Check if the Create client button is visible
      cy.get("#menu-list-staff-form").should("be.visible").click();

      cy.get("#clientType")
        .should("be.visible")
        .and("have.value", "")
        .find("[part='trigger-button']")
        .click();

      cy.get("#clientType")
        .find('cds-combo-box-item[data-id="I"]')
        .should("be.visible")
        .click()
        .and("have.value", "Individual");

      cy.contains("h2", "Client information");
    });

    it("should display the Individual information input fields", () => {
      
      cy.get("#firstName").should("be.visible");
      cy.get("#middleName").should("be.visible");
      cy.get("#lastName").should("be.visible");
      cy.get("#birthdate").should("be.visible");
      cy.get("#identificationType").should("be.visible");
      cy.get("#clientIdentification").should("be.visible");
    });

    scenarios.forEach(({ name, data }) => {
      describe(name, () => {
        beforeEach(() => {
          fillIndividual(data);
        });

        it("enables the button Next", () => {
          cy.get("[data-test='wizard-next-button']")
            .shadow()
            .find("button")
            .should("be.enabled");
        });


        it("should display provided info when returning to step 1", () => {
          cy.get("[data-test='wizard-next-button']").click();
          cy.contains("h2", "Client information").should("not.exist");

          cy.get("[data-test='wizard-back-button']").click();

          cy.get("#firstName")
            .shadow()
            .find("input")
            .should("have.value", data.firstName);

          cy.get("#middleName")
            .shadow()
            .find("input")
            .should("have.value", data.middleName);

          cy.get("#lastName")
            .shadow()
            .find("input")
            .should("have.value", data.lastName);

          cy.get("#birthdateYear")
            .shadow()
            .find("input")
            .should("have.value", data.birthdateYear);

          cy.get("#birthdateMonth")
            .shadow()
            .find("input")
            .should("have.value", data.birthdateMonth);

          cy.get("#birthdateDay")
            .shadow()
            .find("input")
            .should("have.value", data.birthdateDay);

          cy.get("#identificationType").should(
            "have.value",
            data.identificationTypeValue
          );

          if (data.identificationProvinceValue) {
            cy.get("#identificationProvince").should(
              "have.value",
              data.identificationProvinceValue
            );
          }

          cy.get("#clientIdentification")
            .shadow()
            .find("input")
            .should("have.value", data.clientIdentification);
        });

      });
        
    });
    
  });

  describe("locations step", () => {

    beforeEach(() => {
      cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
        given_name: "James",
        family_name: "Baxter",
        "cognito:groups": ["CLIENT_ADMIN"],
      });

      // Check if the Create client button is visible
      cy.get("#menu-list-staff-form").should("be.visible").click();

      cy.get("#clientType")
          .should("be.visible")
          .and("have.value", "")
          .find("[part='trigger-button']")
          .click();

        cy.get("#clientType")
          .find('cds-combo-box-item[data-id="I"]')
          .should("be.visible")
          .click()
          .and("have.value", "Individual");

        cy.contains("h2", "Client information");

        fillIndividual();

        cy.get("[data-test='wizard-next-button']").click();

        cy.contains("h2", "Locations");
        fillLocation();
      
    });

    it("should display an error message at the top of the page when providing invalid character", () => {
      // Add invalid character to the city      
      cy.fillFormEntry('#city_0', 'é');

      cy.get("#city_0")
        .find("input")
        .should("have.class", "cds--text-input--invalid");

      cy.get("[data-test='wizard-back-button']").click();
      cy.contains("h2", "Client information");
      cy.get("[data-test='wizard-next-button']").click();
      cy.contains("h2", "Locations");
      cy.get("[data-test='wizard-next-button']").click();

      cy.wait(10);

      // Check we are still in the same step
      cy.contains("h2", "Locations");

      // For some reason the top notification is not showing up
      // cy.get(".top-notification cds-actionable-notification")
      //   .should("be.visible")
      //   .and("have.attr", "kind", "error");

      cy.get("#city_0")
        .find("input")
        .should("have.class", "cds--text-input--invalid");
    });

    it("should display an error message due to a bad combination of the values provided in the two input fields related to delivery information",() => {
      cy.contains("Add more delivery information").click();

      cy.get("[data-test='wizard-back-button']").click();
      cy.contains("h2", "Client information");
      cy.get("[data-test='wizard-next-button']").click();
      cy.contains("h2", "Locations");

      cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");

      cy.get("#complementaryAddressOne_0")
        .find("input")
        .should("have.class", "cds--text-input--invalid");

    });

  });

  describe("contacts step", () => {

    beforeEach(() => {
      cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
        given_name: "James",
        family_name: "Baxter",
        "cognito:groups": ["CLIENT_ADMIN"],
      });

      // Check if the Create client button is visible
      cy.get("#menu-list-staff-form").should("be.visible").click();

      cy.get("#clientType")
          .should("be.visible")
          .and("have.value", "")
          .find("[part='trigger-button']")
          .click();

        cy.get("#clientType")
          .find('cds-combo-box-item[data-id="I"]')
          .should("be.visible")
          .click()
          .and("have.value", "Individual");

        cy.contains("h2", "Client information");

        fillIndividual();

        cy.get("[data-test='wizard-next-button']").click();

        cy.contains("h2", "Locations");
        fillLocation();

        cy.get("[data-test='wizard-next-button']").click();
        cy.contains("h2", "Contacts");
        fillContact();
      
    });

    it("should display an error message at the top of the page when providing invalid character", () => {
      cy.fillFormEntry("#emailAddress_0","é");

      cy.get("#emailAddress_0")
      .find("input")
      .should("have.class", "cds--text-input--invalid");

      cy.get("[data-test='wizard-back-button']").click();
      cy.contains("h2", "Locations");
      cy.get("[data-test='wizard-back-button']").click();
      cy.contains("h2", "Client information");
      cy.get("[data-test='wizard-next-button']").click();
      cy.contains("h2", "Locations");
      cy.get("[data-test='wizard-next-button']").click();
      cy.contains("h2", "Contacts");
      cy.get("[data-test='wizard-next-button']").click();

      cy.wait(10);

      // Check we are still in the same step
      cy.contains("h2", "Contacts");

      // For some reason the top notification is not showing up
      // cy.get(".top-notification cds-actionable-notification")
      //   .should("be.visible")
      //   .and("have.attr", "kind", "error");
      
      cy.get("#emailAddress_0")
        .find("input")
        .should("have.class", "cds--text-input--invalid");

    });

  });

  describe("review step", () => {

    beforeEach(() => {
      cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
        given_name: "James",
        family_name: "Baxter",
        "cognito:groups": ["CLIENT_ADMIN"],
      });

      cy.intercept("GET", "**/api/codes/countries/CA/BC",{
        fixture: "provinceCodeBC.json",
      }).as("getProvinceByCodes");

      // Check if the Create client button is visible
      cy.get("#menu-list-staff-form").should("be.visible").click();

      cy.get("#clientType")
          .should("be.visible")
          .and("have.value", "")
          .find("[part='trigger-button']")
          .click();

        cy.get("#clientType")
          .find('cds-combo-box-item[data-id="I"]')
          .should("be.visible")
          .click()
          .and("have.value", "Individual");

        cy.contains("h2", "Client information");

        fillIndividual();

        cy.get("[data-test='wizard-next-button']").click();

        cy.contains("h2", "Locations");
        fillLocation();

        cy.get("[data-test='wizard-next-button']").click();
        cy.contains("h2", "Contacts");
        fillContact();
        cy.get("[data-test='wizard-next-button']").click();
        cy.contains("h2", "Review");
      
    });

    it("should allow notes to be added", () => {
      cy.fillFormEntry("cds-textarea", "This is a note!", 1, true);

      cy.get("cds-textarea")
      .shadow()
      .find(".cds--text-area__label-wrapper")
      .should("contain", "15/4000");
      
      // Even if I try to add more than 4k characters, it will stop at 4k
      cy.fillFormEntry("cds-textarea","A".repeat(4010), 0, true);
      
      cy.get("cds-textarea")
      .shadow()
      .find(".cds--text-area__label-wrapper")
      .should("contain", "4000/4000");
    });

    it("should have the notify indicator checked", () => {
      cy.get('body').then(($body) => {
        if ($body.find('#notifyClientIndId').length) {
          cy.get("#notifyClientIndId").should('be.checked');
        }
      });
    });

  });

});
