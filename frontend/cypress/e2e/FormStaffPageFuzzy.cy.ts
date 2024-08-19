/* eslint-disable no-undef */
describe("Staff Form Fuzzy Matches", () => {
  /* Test variables and functions */
  const API_BASE = "http://localhost:8080/api";
  const warningClass = 'warning';
  const errorClass = "cds--text-input--invalid";
  const errorDropdownClass = "cds--dropdown--invalid";
  

  beforeEach(function () {
    cy.viewport(1920, 1080);

    cy.intercept("GET", `${API_BASE}/clients/submissions?page=0&size=10`, {
      fixture: "submissions.json",
      headers: {
        "x-total-count": "1",
        "content-type": "application/json;charset=UTF-8",
        "Access-Control-Expose-Headers": "x-total-count",
      },
    }).as("getSubmissions");

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

    cy.intercept("GET", "**/api/clients/name/**", {
      fixture: "clients/bcreg_ac_list1.json",
    });
    cy.intercept("GET", "/api/clients/C1234567", {
      fixture: "clients/bcreg_C1234567.json",
    });
    cy.intercept("GET", "/api/clients/FM123456", {
      fixture: "clients/bcreg_FM123456.json",
    });
    cy.intercept("GET", "**/api/opendata/**", {
      fixture: "firstNations.json",
    });

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

    cy.wait("@getSubmissions");

    // Check if the Create client button is visible
    cy.get("#menu-list-staff-form").should("be.visible").click();

    cy.get("h1").should("be.visible").should("contain", " Create client ");
    
  });

  describe("Business information step", () => {
    beforeEach(function () {
      const testFixture = this.currentTest.title
        .split("fuzzy resulting in ")[1]
        .replaceAll(" ", "_");

      cy.fixture(`fuzzy/${testFixture}`)
        .then((fixtureData: any) => {
          cy.intercept(
            {
              method: "POST",
              url: "**/api/clients/matches",
              headers: {
                "X-STEP": "1",
              },
            },
            {
              ...fixtureData,
              headers: {
                "content-type": "application/json;charset=UTF-8",
              },
            },
          ).as("doMatch");
        })
        .as("doMatchFixture");
    });

    describe('Individuals fuzzy matching', () => {

      it('should have individual data with fuzzy resulting in partial individual match', () => {
        fillIndividual();
        clickNext(true);

        checkTopNotification('warning', 'Partial matching on name and date of birth');

        checkInputWarning('#firstName');
        checkInputWarning('#lastName');
        checkInputWarning('#birthdateYear');
        checkInputWarning('#birthdateMonth');
        checkInputWarning('#birthdateDay');
        checkDropdownClean('#identificationType');
        checkDropdownClean('#identificationProvince');
        checkInputClean('#clientIdentification');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");

        cy.get('#reviewStatement')
        .should('be.visible');

      });

      it('should have individual data with fuzzy resulting in full individual match', () => {
        fillIndividual();
        clickNext(true);

        checkTopNotification('error', 'Matching on name, date of birth and ID number');

        checkInputError('#firstName');
        checkInputError('#lastName');
        checkInputError('#birthdateYear');
        checkInputError('#birthdateMonth');
        checkInputError('#birthdateDay');
        checkDropdownClean('#identificationType');
        checkDropdownClean('#identificationProvince');
        checkInputError('#clientIdentification');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");

        cy.get('#reviewStatement').should('not.exist');

      });

      it('should have individual data with fuzzy resulting in document individual match', () => {
        fillIndividual();
        clickNext(true);

        checkTopNotification('error', 'Matching on ID type and ID number');

        checkInputClean('#firstName');
        checkInputClean('#lastName');
        checkInputClean('#birthdateYear');
        checkInputClean('#birthdateMonth');
        checkInputClean('#birthdateDay');
        checkDropdownError('#identificationType');
        checkDropdownError('#identificationProvince');
        checkInputError('#clientIdentification');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");

        cy.get('#reviewStatement').should('not.exist');

      });

    });

    describe('BC Registered fuzzy matching',() =>{

      it('should have registered data with fuzzy resulting in partial business name match',() =>{
        fillRegistered({ birthdateYear: '',
          birthdateMonth: '',
          birthdateDay: '',
        });
        clickNext(true);

        checkTopNotification('warning', 'Partial matching on client name');

        checkDropdownWarning('#businessName');
        checkInputClean('#workSafeBCNumber');
        checkInputClean('#doingBusinessAs');
        checkInputClean('#acronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.enabled");

        cy.get('#reviewStatement')
        .should('be.visible');
      })

      it('should have registered data with fuzzy resulting in full business name match',() =>{
        fillRegistered({ birthdateYear: '',
          birthdateMonth: '',
          birthdateDay: '',
        });
        clickNext(true);

        checkTopNotification('error', 'Matching on client name');

        checkDropdownError('#businessName');
        checkInputClean('#workSafeBCNumber');
        checkInputClean('#doingBusinessAs');
        checkInputClean('#acronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.enabled");

        cy.get('#reviewStatement')
        .should('not.exist');
      })

      it('should have registered data with fuzzy resulting in full registered number match',() =>{

        fillRegistered({ birthdateYear: '',
          birthdateMonth: '',
          birthdateDay: '',
        });
        clickNext(true);

        checkTopNotification('error', 'Matching on registration number');

        checkDropdownError('#businessName');
        checkInputClean('#workSafeBCNumber');
        checkInputClean('#doingBusinessAs');
        checkInputClean('#acronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.enabled");

        cy.get('#reviewStatement')
        .should('not.exist');
      })

      it('should have sole proprietorship data with fuzzy resulting in partial individual match',() =>{
        fillRegistered({ 
          registrationNumber:'FM123456',
          doingBusinessAs: '',
        });
        clickNext(true);

        cy.get('#fuzzy-match-notification-global')
        .should("be.visible")
        .and("have.attr", "kind", "warning")
        .shadow()
        .find(
          "div.cds--actionable-notification__details div.cds--actionable-notification__text-wrapper div.cds--actionable-notification__content div.cds--actionable-notification__title"
        )
        .should("contain", "Possible matching records found");

        cy.get('#fuzzy-match-notification-global > div > ul > li')      
        .should('be.visible')
        .and('contain', 'Partial matching on name and date of birth');

        checkDropdownWarning('#businessName');
        checkInputWarning('#birthdateYear');
        checkInputWarning('#birthdateMonth');
        checkInputWarning('#birthdateDay');
        checkInputClean('#workSafeBCNumber');      
        checkInputClean('#acronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");

        cy.get('#reviewStatement')
        .should('be.visible');
      })

      it('should have registered data with fuzzy resulting in partial dba match',() =>{

        fillRegistered({ birthdateYear: '',
          birthdateMonth: '',
          birthdateDay: '',
        });
        clickNext(true);

        checkTopNotification('warning', 'Partial matching on doing business as');

        checkDropdownClean('#businessName');
        checkInputClean('#workSafeBCNumber');
        checkInputWarning('#doingBusinessAs');
        checkInputClean('#acronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");

        cy.get('#reviewStatement')
        .should('be.visible');
      })

      it('should have registered data with fuzzy resulting in full dba match',() =>{

        fillRegistered({ birthdateYear: '',
          birthdateMonth: '',
          birthdateDay: '',
        });
        clickNext(true);

        checkTopNotification('error', 'Matching on doing business as');

        checkDropdownClean('#businessName');
        checkInputClean('#workSafeBCNumber');
        checkInputError('#doingBusinessAs');
        checkInputClean('#acronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");

        cy.get('#reviewStatement')
        .should('not.exist');
      })

      it('should have registered data with fuzzy resulting in full acronym match',() =>{
        fillRegistered({ birthdateYear: '',
          birthdateMonth: '',
          birthdateDay: '',
        });
        clickNext(true);

        checkTopNotification('error', 'Matching on client acronym');

        checkDropdownClean('#businessName');
        checkInputClean('#workSafeBCNumber');
        checkInputClean('#doingBusinessAs');
        checkInputError('#acronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");

        cy.get('#reviewStatement')
        .should('not.exist');
      })

    });

    describe('First Nations fuzzy matching',() =>{

      it('should have first nations with fuzzy resulting in full fn federal id match',() =>{
        fillFirstNations();
        clickNext(true);

        checkTopNotification('error', 'Matching on federal identification number');

        checkDropdownError('#clientName');
        checkInputClean('#workSafeBcNumber');
        checkInputClean('#clientAcronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.enabled");

        cy.get('#reviewStatement')
        .should('not.exist');

      });

      it('should have first nations with fuzzy resulting in partial business name match',() =>{
        fillFirstNations();
        clickNext(true);

        checkTopNotification('warning', 'Partial matching on client name');

        checkDropdownWarning('#clientName');
        checkInputClean('#workSafeBcNumber');
        checkInputClean('#clientAcronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.enabled");

        cy.get('#reviewStatement')
        .should('be.visible');
      });

      it('should have first nations with fuzzy resulting in full business name match',() =>{
        fillFirstNations();
        clickNext(true);

        checkTopNotification('error', 'Matching on client name');

        checkDropdownError('#clientName');
        checkInputClean('#workSafeBcNumber');
        checkInputClean('#clientAcronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.enabled");

        cy.get('#reviewStatement')
        .should('not.exist');
      });

      it('should have registered data with fuzzy resulting in full acronym match',() =>{
        fillFirstNations();
        clickNext(true);

        checkTopNotification('error', 'Matching on client acronym');

        checkDropdownClean('#clientName');
        checkInputClean('#workSafeBcNumber');
        checkInputError('#clientAcronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");

        cy.get('#reviewStatement')
        .should('not.exist');
      });

    });

    describe('Government fuzzy matching',() =>{

      it('should have government with fuzzy resulting in partial business name match',() =>{
        fillOthers({kind: 'Government'});
        clickNext(true);

        checkTopNotification('warning', 'Partial matching on client name');

        checkInputWarning('#businessName');
        checkInputClean('#workSafeBcNumber');
        checkInputClean('#clientAcronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");

        cy.get('#reviewStatement')
        .should('be.visible');
      });

      it('should have government with fuzzy resulting in full business name match',() =>{
        fillOthers({kind: 'Government'});
        clickNext(true);

        checkTopNotification('error', 'Matching on client name');

        checkInputError('#businessName');
        checkInputClean('#workSafeBcNumber');
        checkInputClean('#clientAcronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");

        cy.get('#reviewStatement')
        .should('not.exist');
      });

      it('should have government data with fuzzy resulting in full acronym match',() =>{
        fillOthers({kind: 'Government'});
        clickNext(true);

        checkTopNotification('error', 'Matching on client acronym');

        checkInputClean('#businessName');
        checkInputClean('#workSafeBcNumber');
        checkInputError('#clientAcronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");

        cy.get('#reviewStatement')
        .should('not.exist');
      });

    });

    describe('Ministry of Forests fuzzy matching',() =>{

      it('should have forests with fuzzy resulting in partial business name match',() =>{
        fillOthers({kind: 'Ministry of Forests'});
        clickNext(true);

        checkTopNotification('warning', 'Partial matching on client name');

        checkInputWarning('#businessName');
        checkInputClean('#workSafeBcNumber');
        checkInputClean('#clientAcronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");

        cy.get('#reviewStatement')
        .should('be.visible');
      });

      it('should have forests with fuzzy resulting in full business name match',() =>{
        fillOthers({kind: 'Ministry of Forests'});
        clickNext(true);

        checkTopNotification('error', 'Matching on client name');

        checkInputError('#businessName');
        checkInputClean('#workSafeBcNumber');
        checkInputClean('#clientAcronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");

        cy.get('#reviewStatement')
        .should('not.exist');
      });

      it('should have forests data with fuzzy resulting in full acronym match',() =>{
        fillOthers({kind: 'Ministry of Forests'});
        clickNext(true);

        checkTopNotification('error', 'Matching on client acronym');

        checkInputClean('#businessName');
        checkInputClean('#workSafeBcNumber');
        checkInputError('#clientAcronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");

        cy.get('#reviewStatement')
        .should('not.exist');
      });

    });

    describe('Unregistered company fuzzy matching',() =>{

      it('should have unregistered data with fuzzy resulting in partial business name match',() =>{
        fillOthers({kind: 'Unregistered company'});
        clickNext(true);

        checkTopNotification('warning', 'Partial matching on client name');

        checkInputWarning('#businessName');
        checkInputClean('#workSafeBcNumber');
        checkInputClean('#clientAcronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");

        cy.get('#reviewStatement')
        .should('be.visible');
      });

      it('should have unregistered data with fuzzy resulting in full business name match',() =>{
        fillOthers({kind: 'Unregistered company'});
        clickNext(true);

        checkTopNotification('error', 'Matching on client name');

        checkInputError('#businessName');
        checkInputClean('#workSafeBcNumber');
        checkInputClean('#clientAcronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");

        cy.get('#reviewStatement')
        .should('not.exist');
      });

      it('should have unregistered data with fuzzy resulting in full acronym match',() =>{
        fillOthers({kind: 'Unregistered company'});
        clickNext(true);

        checkTopNotification('error', 'Matching on client acronym');

        checkInputClean('#businessName');
        checkInputClean('#workSafeBcNumber');
        checkInputError('#clientAcronym');

        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");

        cy.get('#reviewStatement')
        .should('not.exist');
      });

    });
  });

  describe('Locations fuzzy matching',() =>{
    beforeEach(function () {
      cy.intercept(
        {
          method: "POST",
          url: "**/api/clients/matches",
          headers: {
            "X-STEP": "1",
          },
        },
        {
          statusCode: 204,
          headers: {
            "content-type": "application/json;charset=UTF-8",
          },
        },
      ).as("dontMatch");

      fillIndividual();
      clickNext(false);  
    });
    describe("when there is only one location", () => {
      beforeEach(function () {
        const testFixture = this.currentTest.title
          .split("fuzzy resulting in ")[1]
          .replaceAll(" ", "_");
  
          cy.fixture(`fuzzy/${testFixture}`)
          .then((fixtureData: any) => {
            cy.intercept(
              {
                method: "POST",
                url: "**/api/clients/matches",
                headers: {
                  "X-STEP": "2",
                },
              },
              {
                ...fixtureData,
                headers: {
                  "content-type": "application/json;charset=UTF-8",
                },
              },
            ).as("doMatch");
          })
          .as("doMatchFixture");
      });
      it('should have location data with fuzzy resulting in full address match', () => {
        fillLocations(0);
        clickNext(true);
  
        checkNotification('location-addresses-0', 'warning', 'Matching on address');
  
        checkInputClean('#name_0');
        checkInputClean('#complementaryAddressOne_0');
        checkDropdownWarning('#addr_0');
        checkInputWarning('#city_0');
        checkDropdownWarning('#province_0');
        checkDropdownWarning('#country_0');
        checkInputWarning('#postalCode_0');
        checkInputClean('#emailAddress_0');
        checkInputClean('#businessPhoneNumber_0');
        checkInputClean('#secondaryPhoneNumber_0');
        checkInputClean('#faxNumber_0');
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });
      it('should have location data with fuzzy resulting in full address email match', () => {
        fillLocations(0);
        clickNext(true);
  
        checkNotification('location-addresses-0', 'warning', 'Matching on email address');
  
        checkInputClean('#name_0');
        checkInputClean('#complementaryAddressOne_0');
        checkDropdownClean('#addr_0');
        checkInputClean('#city_0');
        checkDropdownClean('#province_0');
        checkDropdownClean('#country_0');
        checkInputClean('#postalCode_0');
        checkInputWarning('#emailAddress_0');
        checkInputClean('#businessPhoneNumber_0');
        checkInputClean('#secondaryPhoneNumber_0');
        checkInputClean('#faxNumber_0');
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });
      it('should have location data with fuzzy resulting in full address business phone match', () => {
        fillLocations(0);
        clickNext(true);
  
        checkNotification('location-addresses-0', 'warning', 'Matching on primary phone number');
  
        checkInputClean('#name_0');
        checkInputClean('#complementaryAddressOne_0');
        checkDropdownClean('#addr_0');
        checkInputClean('#city_0');
        checkDropdownClean('#province_0');
        checkDropdownClean('#country_0');
        checkInputClean('#postalCode_0');
        checkInputClean('#emailAddress_0');
        checkInputWarning('#businessPhoneNumber_0');
        checkInputClean('#secondaryPhoneNumber_0');
        checkInputClean('#faxNumber_0');
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });
      it('should have location data with fuzzy resulting in full address secondary phone match', () => {
        fillLocations(0);
        clickNext(true);
  
        checkNotification('location-addresses-0', 'warning', 'Matching on secondary phone number');
  
        checkInputClean('#name_0');
        checkInputClean('#complementaryAddressOne_0');
        checkDropdownClean('#addr_0');
        checkInputClean('#city_0');
        checkDropdownClean('#province_0');
        checkDropdownClean('#country_0');
        checkInputClean('#postalCode_0');
        checkInputClean('#emailAddress_0');
        checkInputClean('#businessPhoneNumber_0');
        checkInputWarning('#secondaryPhoneNumber_0');
        checkInputClean('#faxNumber_0');
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });
      it('should have location data with fuzzy resulting in full address fax phone match', () => {
        fillLocations(0);
        clickNext(true);
  
        checkNotification('location-addresses-0', 'warning', 'Matching on fax');
  
        checkInputClean('#name_0');
        checkInputClean('#complementaryAddressOne_0');
        checkDropdownClean('#addr_0');
        checkInputClean('#city_0');
        checkDropdownClean('#province_0');
        checkDropdownClean('#country_0');
        checkInputClean('#postalCode_0');
        checkInputClean('#emailAddress_0');
        checkInputClean('#businessPhoneNumber_0');
        checkInputClean('#secondaryPhoneNumber_0');
        checkInputWarning('#faxNumber_0');
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });
    });
    const createLocationMatch = (index: string) => ({
      "field":`location.addresses[${index}].emailAddress`,
      "match":`0000000${index}`,
      "fuzzy":true,
      "partialMatch":false
    });
    describe("multiple locations", () => {
      beforeEach(function () {
        const testIndexes = this.currentTest.title
          .split("indexes:")[1]
          .replaceAll(" ", "")
          .split(",");

        const body = testIndexes.map(createLocationMatch);

        // only to be served as a base
        cy.fixture("fuzzy/full_address_email_match")
          .then((fixtureData: any) => {
            cy.intercept(
              {
                method: "POST",
                url: "**/api/clients/matches",
                headers: {
                  "X-STEP": "2",
                },
              },
              {
                ...fixtureData,
                body,
                headers: {
                  "content-type": "application/json;charset=UTF-8",
                },
              },
            ).as("doMatch");
          })
          .as("doMatchFixture");
      });
      describe("when there are two locations", () => {
        beforeEach(function () {
          fillLocations(0);

          clickAddLocation(1);
          fillLocations(1, {
            name: "Office 1",
          });
        });
        it('should have location data with fuzzy resulting in full email match at indexes: 0', () => {
          clickNext(true);

          checkNotification('location-addresses-0', 'warning', 'Matching on email address');
          checkNotificationToNotExist('location-addresses-1');
          checkAddressAccordionState(1, false);
  
          checkInputWarning('#emailAddress_0');

          checkInputClean('#emailAddress_1');
    
          cy.get("[data-test='wizard-next-button']")
          .shadow()
          .find("button")
          .should("be.disabled");
    
          cy.get('#reviewStatement')
          .should('be.visible');
        });
        it('should have location data with fuzzy resulting in full email match at indexes: 1', () => {
          clickNext(true);

          checkNotificationToNotExist('location-addresses-0');
          checkNotification('location-addresses-1', 'warning', 'Matching on email address');
          checkAddressAccordionState(1, true);
  
          checkInputClean('#emailAddress_0');

          checkInputWarning('#emailAddress_1');
    
          cy.get("[data-test='wizard-next-button']")
          .shadow()
          .find("button")
          .should("be.disabled");
    
          cy.get('#reviewStatement')
          .should('be.visible');
        });
        it('should have location data with fuzzy resulting in full email match at indexes: 0, 1', () => {
          clickNext(true);

          checkNotification('location-addresses-0', 'warning', 'Matching on email address');
          checkNotification('location-addresses-1', 'warning', 'Matching on email address');
          checkAddressAccordionState(1, true);
  
          checkInputWarning('#emailAddress_0');

          checkInputWarning('#emailAddress_1');
    
          cy.get("[data-test='wizard-next-button']")
          .shadow()
          .find("button")
          .should("be.disabled");
    
          cy.get('#reviewStatement')
          .should('be.visible');
        });
      });
      describe("when there are three locations", () => {
        beforeEach(function () {
          fillLocations(0);

          clickAddLocation(1);
          fillLocations(1, {
            name: "Office 1",
          });

          clickAddLocation(2);
          fillLocations(2, {
            name: "Office 2",
          });
        });
        it('should have location data with fuzzy resulting in full email match at indexes: 1', () => {
          clickNext(true);

          checkNotificationToNotExist('location-addresses-0');
          checkNotification('location-addresses-1', 'warning', 'Matching on email address');
          checkAddressAccordionState(1, true);
          checkNotificationToNotExist('location-addresses-2')
          checkAddressAccordionState(2, false);
  
          checkInputClean('#emailAddress_0');

          checkInputWarning('#emailAddress_1');

          checkInputClean('#emailAddress_2');
    
          cy.get("[data-test='wizard-next-button']")
          .shadow()
          .find("button")
          .should("be.disabled");
    
          cy.get('#reviewStatement')
          .should('be.visible');
        });
        it('should have location data with fuzzy resulting in full email match at indexes: 2', () => {
          clickNext(true);

          checkNotificationToNotExist('location-addresses-0');
          checkNotificationToNotExist('location-addresses-1');
          checkAddressAccordionState(1, false);
          checkNotification('location-addresses-2', 'warning', 'Matching on email address');
          checkAddressAccordionState(2, true);
  
          checkInputClean('#emailAddress_0');

          checkInputClean('#emailAddress_1');

          checkInputWarning('#emailAddress_2');
    
          cy.get("[data-test='wizard-next-button']")
          .shadow()
          .find("button")
          .should("be.disabled");
    
          cy.get('#reviewStatement')
          .should('be.visible');
        });
        it('should have location data with fuzzy resulting in full email match at indexes: 1, 2', () => {
          clickNext(true);

          checkNotificationToNotExist('location-addresses-0');
          checkNotification('location-addresses-1', 'warning', 'Matching on email address');
          checkAddressAccordionState(1, true);
          checkNotification('location-addresses-2', 'warning', 'Matching on email address');
          checkAddressAccordionState(2, true);
  
          checkInputClean('#emailAddress_0');

          checkInputWarning('#emailAddress_1');

          checkInputWarning('#emailAddress_2');
    
          cy.get("[data-test='wizard-next-button']")
          .shadow()
          .find("button")
          .should("be.disabled");
    
          cy.get('#reviewStatement')
          .should('be.visible');
        });
      });
    });
  });

  const clickNext = (shouldMatch: boolean) => {
    cy.get("[data-test='wizard-next-button']")
      .shadow()
      .find("button")
      .should("be.enabled");
    cy.get("[data-test='wizard-next-button']").click();
    if (shouldMatch) {
      cy.wait('@doMatch');
    } else {
      cy.wait('@dontMatch');
    }
  };

  const clickAddLocation = (index: number) => {
    cy.contains("cds-button", "Add another location").should("be.visible").click();
    cy.get(`cds-accordion-item[data-focus="address-${index}-heading"]`).focused();
  };

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

  const fillRegistered = (extraData: any = {}) => {
    cy.fixture("testdata/registeredBaseData").then((fixtureData: any) => {
      const data = { ...fixtureData, ...extraData };

      cy.get("#clientType").should("be.visible").and("have.value", "");
      cy.selectFormEntry("#clientType", "BC registered business", false);

      cy.selectAutocompleteEntry("#businessName", data.businessName, data.registrationNumber);

      if(data.birthdateYear){
        cy.fillFormEntry("#birthdateYear", data.birthdateYear);
        cy.fillFormEntry("#birthdateMonth", data.birthdateMonth);
        cy.fillFormEntry("#birthdateDay", data.birthdateDay);
      }

      if(data.doingBusinessAs){
        cy.fillFormEntry("#doingBusinessAs", data.doingBusinessAs);
      }

      if(data.workSafeBcNumber){
        cy.fillFormEntry("#workSafeBCNumber", data.workSafeBcNumber);
      }

      if(data.clientAcronym){
        cy.fillFormEntry("#acronym", data.clientAcronym);
      }
    });
  };

  const fillFirstNations = (extraData: any = {}) => {
    cy.fixture("testdata/firstNationsBaseData").then((fixtureData: any) => {
      const data = { ...fixtureData, ...extraData };

      cy.get("#clientType").should("be.visible").and("have.value", "");
      cy.selectFormEntry("#clientType", "First Nation", false);

      cy.selectAutocompleteEntry("#clientName", data.businessName, data.registrationNumber);
    
      if(data.workSafeBcNumber){
        cy.fillFormEntry("#workSafeBcNumber", data.workSafeBcNumber);
      }

      if(data.clientAcronym){
        cy.fillFormEntry("#clientAcronym", data.clientAcronym);
      }
    });
  };

  const fillOthers = (extraData: any = {}) => {
    cy.fixture("testdata/otherBaseData").then((fixtureData: any) => {
      const data = { ...fixtureData, ...extraData };

      cy.get("#clientType").should("be.visible").and("have.value", "");
      cy.selectFormEntry("#clientType", data.kind, false);

      cy.fillFormEntry("#businessName", data.businessName);
    
      if(data.workSafeBcNumber){
        cy.fillFormEntry("#workSafeBcNumber", data.workSafeBcNumber);
      }

      if(data.clientAcronym){
        cy.fillFormEntry("#clientAcronym", data.clientAcronym);
      }
    });
  };

  const fillLocations = (index: number, extraData: any = {}) => {
    cy.fixture("testdata/locationBaseData").then((fixtureData: any) => {
      const data = { ...fixtureData, ...extraData };

      cy.fillFormEntry(`#name_${index}`, data.name);
      cy.selectAutocompleteEntry(`#addr_${index}`, data.addr, data.addressId);
      cy.fillFormEntry(`#emailAddress_${index}`, data.emailAddress);
      cy.fillFormEntry(`#businessPhoneNumber_${index}`, data.businessPhoneNumber);
      cy.fillFormEntry(`#secondaryPhoneNumber_${index}`, data.secondaryPhoneNumber);
      cy.fillFormEntry(`#faxNumber_${index}`, data.faxNumber);
    });
  };

  const checkInputWarning = (element: string) => {
    cy.get(element)
    .should('be.visible')
    .and('have.class', warningClass)
    .shadow()
    .find('input')
    .should('not.have.class', errorClass);
  }

  const checkInputError = (element: string) => {
    cy.get(element)
    .should('be.visible')
    .and('not.have.class', warningClass)
    .shadow()
    .find('input')
    .should('have.class', errorClass);
  }

  const checkInputClean = (element: string) => {
    cy.get(element)
    .should('be.visible')
    .and('not.have.class', warningClass)
    .shadow()
    .find('input')
    .should('not.have.class', errorClass);
  }

  const checkDropdownWarning = (element: string) => {
    cy.get(element)
    .should('be.visible')
    .and('have.class', warningClass)
    .shadow()
    .find('div.cds--dropdown')
    .should('not.have.class', errorDropdownClass);
  }

  const checkDropdownError = (element: string) => {
    cy.get(element)
    .should('be.visible')
    .and('not.have.class', warningClass)
    .shadow()
    .find('div.cds--dropdown')
    .should('have.class', errorDropdownClass);
  }

  const checkDropdownClean = (element: string) => {
    cy.get(element)
    .should('be.visible')
    .and('not.have.class', warningClass)
    .shadow()
    .find('div.cds--dropdown')
    .should('not.have.class', errorDropdownClass);
  }

  const checkNotification = (id: string, kind: string, message: string) => {
    cy.get(`#fuzzy-match-notification-${id}`)
      .should("be.visible")
      .and("have.attr", "kind", kind)
      .shadow()
      .find(
        "div.cds--actionable-notification__details div.cds--actionable-notification__text-wrapper div.cds--actionable-notification__content div.cds--actionable-notification__title"
      )
      .should("contain", kind === 'warning' ? 'Possible matching records found' : 'Client already exists');

      cy.get(`#fuzzy-match-notification-${id} > div > ul > li`)
      .should('be.visible')
      .and('contain', message);
  }

  const checkNotificationToNotExist = (id: string) => {
    cy.get(`#fuzzy-match-notification-${id}`)
      .should("not.exist");
  }

  const checkTopNotification = (kind: string, message: string) => {
    checkNotification('global', kind, message);
  }

  const checkAddressAccordionState = (index: number, open: boolean) => {
    cy.checkAccordionItemState(`[data-focus="address-${index}-heading"]`, open);
  }
});
