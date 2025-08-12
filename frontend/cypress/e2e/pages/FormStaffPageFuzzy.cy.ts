/* eslint-disable no-undef */
describe("Staff Form Fuzzy Matches", () => {
  /* Test variables and functions */
  const API_BASE = "http://localhost:8080/api";
  const warningClass = 'warning';
  const errorClass = "cds--text-input--invalid";
  const errorDropdownClass = "cds--dropdown--invalid";
  

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

    interceptFuzzyMatch(1, this.currentTest.title);
    interceptFuzzyMatch(2, this.currentTest.title);
    interceptFuzzyMatch(3, this.currentTest.title);
    
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

    // Check if the Create client button is visible
    cy.get("#menu-list-staff-form").should("be.visible").click();

    cy.get("h1").should("be.visible").should("contain", " Create client ");
    
  });

  describe('Individuals fuzzy matching', () => {

    it('should have individual data with fuzzy resulting in Step 1: partial individual match', () => {
      fillIndividual();
      clickNext(1);

      checkTopNotification('warning', 'was found with similar name and birthdate');

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

    it('should have individual data with fuzzy resulting in Step 1: full individual match', () => {
      fillIndividual();
      clickNext(1);

      checkTopNotification('error', 'has client number');

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

    it('should have individual data with fuzzy resulting in Step 1: document individual match', () => {
      fillIndividual();
      clickNext(1);

      checkTopNotification('error', 'has client number');

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

    it('should have individual data with fuzzy resulting in Step 1: full and partial individual match', () => {
      fillIndividual();
      clickNext(1);

      checkTopNotification('error', 'has client number');

      /*
      The following group of fields has both warning and error.
      In this case they are displayed as error.
      They are from both the full and the partial matches.
      */
      checkInputError('#firstName');
      checkInputError('#lastName');
      checkInputError('#birthdateYear');
      checkInputError('#birthdateMonth');
      checkInputError('#birthdateDay');

      checkDropdownClean('#identificationType');
      checkDropdownClean('#identificationProvince');

      // From the full match
      checkInputError('#clientIdentification');

      cy.get("[data-test='wizard-next-button']")
      .shadow()
      .find("button")
      .should("be.disabled");

      cy.get('#reviewStatement').should('not.exist');
    });

    it('should have individual data with fuzzy resulting in Step 1: document and partial individual match', () => {
      fillIndividual();
      clickNext(1);

      checkTopNotification('error', 'has client number');

      // The partial match (warning)
      checkInputWarning('#firstName');
      checkInputWarning('#lastName');
      checkInputWarning('#birthdateYear');
      checkInputWarning('#birthdateMonth');
      checkInputWarning('#birthdateDay');

      // The document match (error)
      checkDropdownError('#identificationType');
      checkDropdownError('#identificationProvince');
      checkInputError('#clientIdentification');

      cy.get("[data-test='wizard-next-button']")
      .shadow()
      .find("button")
      .should("be.disabled");

      cy.get('#reviewStatement').should('not.exist');
    });

    describe('when we check the Review statement', () => {
      beforeEach(function () {
        interceptFuzzyMatch(1, "Step 1: partial individual match");
        fillIndividual();
        clickNext(1);

        checkTopNotification('warning', 'was found with similar name and birthdate');
        cy.get('#reviewStatement').should('be.visible');

        cy.get("[data-test='wizard-next-button']")
          .shadow()
          .find("button")
          .should("be.disabled");

        cy.markCheckbox("#reviewStatement");
      });

      it("should reach the next step", () => {
        clickNext(null);

        cy.contains("h2", "Locations");
      });
    });
  });

  describe('BC Registered fuzzy matching',() =>{

    it('should have registered data with fuzzy resulting in Step 1: partial business name match',() =>{
      fillRegistered({ birthdateYear: '',
        birthdateMonth: '',
        birthdateDay: '',
      });
      clickNext(1);

      checkTopNotification('warning', 'was found with similar client name');

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

    it('should have registered data with fuzzy resulting in Step 1: full business name match',() =>{
      fillRegistered({ birthdateYear: '',
        birthdateMonth: '',
        birthdateDay: '',
      });
      clickNext(1);

      checkTopNotification('error', 'has client number');

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

    it('should have registered data with fuzzy resulting in Step 1: full registered number match',() =>{

      fillRegistered({ birthdateYear: '',
        birthdateMonth: '',
        birthdateDay: '',
      });
      clickNext(1);

      checkTopNotification('error', 'has client number');

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

    it('should have sole proprietorship data with fuzzy resulting in Step 1: partial individual match',() =>{
      fillRegistered({ 
        registrationNumber:'FM123456',
        doingBusinessAs: '',
      });
      clickNext(1);

      checkTopNotification('warning', 'was found with similar name and birthdate');

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

    it('should have registered data with fuzzy resulting in Step 1: partial dba match',() =>{

      fillRegistered({ birthdateYear: '',
        birthdateMonth: '',
        birthdateDay: '',
      });
      clickNext(1);

      checkTopNotification('warning', 'was found with similar doing business as');

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

    it('should have registered data with fuzzy resulting in Step 1: full dba match',() =>{

      fillRegistered({ birthdateYear: '',
        birthdateMonth: '',
        birthdateDay: '',
      });
      clickNext(1);

      checkTopNotification('error', 'has client number');

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

    it('should have registered data with fuzzy resulting in Step 1: full acronym match',() =>{
      fillRegistered({ birthdateYear: '',
        birthdateMonth: '',
        birthdateDay: '',
      });
      clickNext(1);

      checkTopNotification('error', 'has client number');

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

    describe('when we check the Review statement', () => {
      beforeEach(function () {
        interceptFuzzyMatch(1, "Step 1: partial business name match");
        fillRegistered({ birthdateYear: '',
          birthdateMonth: '',
          birthdateDay: '',
        });
        clickNext(1);

        checkTopNotification('warning', 'was found with similar client name');
        cy.get('#reviewStatement').should('be.visible');

        // TODO: fix form validation so the button gets properly disabled
        // cy.get("[data-test='wizard-next-button']")
        //   .shadow()
        //   .find("button")
        //   .should("be.disabled");

        cy.markCheckbox("#reviewStatement");
      });

      it("should reach the next step", () => {
        clickNext(null);

        cy.contains("h2", "Locations");
      });
    });

  });

  describe('First Nations fuzzy matching',() =>{

    it('should have first nations with fuzzy resulting in Step 1: full fn federal id match',() =>{
      fillFirstNations();
      clickNext(1);

      checkTopNotification('error', 'has client number');

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

    it('should have first nations with fuzzy resulting in Step 1: partial business name match',() =>{
      fillFirstNations();
      clickNext(1);

      checkTopNotification('warning', 'was found with similar client name');

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

    it('should have first nations with fuzzy resulting in Step 1: full business name match',() =>{
      fillFirstNations();
      clickNext(1);

      checkTopNotification('error', 'has client number');

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

    it('should have registered data with fuzzy resulting in Step 1: full acronym match',() =>{
      fillFirstNations();
      clickNext(1);

      checkTopNotification('error', 'has client number');

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

    describe('when we check the Review statement', () => {
      beforeEach(function () {
        interceptFuzzyMatch(1, "Step 1: partial business name match");
        fillFirstNations();
        clickNext(1);

        checkTopNotification('warning', 'was found with similar client name');
        cy.get('#reviewStatement').should('be.visible');

        // TODO: fix form validation so the button gets properly disabled
        // cy.get("[data-test='wizard-next-button']")
        //   .shadow()
        //   .find("button")
        //   .should("be.disabled");

        cy.markCheckbox("#reviewStatement");
      });

      it("should reach the next step", () => {
        clickNext(null);

        cy.contains("h2", "Locations");
      });
    });

  });

  describe('Government fuzzy matching',() =>{

    it('should have government with fuzzy resulting in Step 1: partial business name match',() =>{
      fillOthers({kind: 'Government'});
      clickNext(1);

      checkTopNotification('warning', 'was found with similar client name');

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

    it('should have government with fuzzy resulting in Step 1: full business name match',() =>{
      fillOthers({kind: 'Government'});
      clickNext(1);

      checkTopNotification('error', 'has client number');

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

    it('should have government data with fuzzy resulting in Step 1: full acronym match',() =>{
      fillOthers({kind: 'Government'});
      clickNext(1);

      checkTopNotification('error', 'has client number');

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

    describe('when we check the Review statement', () => {
      beforeEach(function () {
        interceptFuzzyMatch(1, "Step 1: partial business name match");
        fillOthers({kind: 'Government'});
        clickNext(1);

        checkTopNotification('warning', 'was found with similar client name');
        cy.get('#reviewStatement').should('be.visible');

        cy.get("[data-test='wizard-next-button']")
          .shadow()
          .find("button")
          .should("be.disabled");

        cy.markCheckbox("#reviewStatement");
      });

      it("should reach the next step", () => {
        clickNext(null);

        cy.contains("h2", "Locations");
      });
    });

  });

  describe('Ministry of Forests fuzzy matching',() =>{

    it('should have forests with fuzzy resulting in Step 1: partial business name match',() =>{
      fillOthers({kind: 'Ministry of Forests'});
      clickNext(1);

      checkTopNotification('warning', 'was found with similar client name');

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

    it('should have forests with fuzzy resulting in Step 1: full business name match',() =>{
      fillOthers({kind: 'Ministry of Forests'});
      clickNext(1);

      checkTopNotification('error', 'has client number');

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

    it('should have forests data with fuzzy resulting in Step 1: full acronym match',() =>{
      fillOthers({kind: 'Ministry of Forests'});
      clickNext(1);

      checkTopNotification('error', 'has client number');

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

    describe('when we check the Review statement', () => {
      beforeEach(function () {
        interceptFuzzyMatch(1, "Step 1: partial business name match");
        fillOthers({kind: 'Ministry of Forests'});
        clickNext(1);

        checkTopNotification('warning', 'was found with similar client name');
        cy.get('#reviewStatement').should('be.visible');

        cy.get("[data-test='wizard-next-button']")
          .shadow()
          .find("button")
          .should("be.disabled");

        cy.markCheckbox("#reviewStatement");
      });

      it("should reach the next step", () => {
        clickNext(null);

        cy.contains("h2", "Locations");
      });
    });

  });

  describe('Unregistered company fuzzy matching',() =>{

    it('should have unregistered data with fuzzy resulting in Step 1: partial business name match',() =>{
      fillOthers({kind: 'Unregistered company'});
      clickNext(1);

      checkTopNotification('warning', 'was found with similar client name');

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

    it('should have unregistered data with fuzzy resulting in Step 1: full business name match',() =>{
      fillOthers({kind: 'Unregistered company'});
      clickNext(1);

      checkTopNotification('error', 'has client number');

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

    it('should have unregistered data with fuzzy resulting in Step 1: full acronym match',() =>{
      fillOthers({kind: 'Unregistered company'});
      clickNext(1);

      checkTopNotification('error', 'has client number');

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

    describe('when we check the Review statement', () => {
      beforeEach(function () {
        interceptFuzzyMatch(1, "Step 1: partial business name match");
        fillOthers({kind: 'Unregistered company'});
        clickNext(1);

        checkTopNotification('warning', 'was found with similar client name');
        cy.get('#reviewStatement').should('be.visible');

        cy.get("[data-test='wizard-next-button']")
          .shadow()
          .find("button")
          .should("be.disabled");

        cy.markCheckbox("#reviewStatement");
      });

      it("should reach the next step", () => {
        clickNext(null);

        cy.contains("h2", "Locations");
      });
    });

  });

  describe('Locations fuzzy matching',() =>{
    beforeEach(function () {
      fillIndividual();
      clickNext(1);  
    });

    describe("when there is only one location", () => {

      it('should have location data with fuzzy resulting in Step 2: full address match', () => {
        fillLocation(0);
        clickNext(2);
  
        checkNotification('location-addresses-0', 'warning', 'was found with similar address');
  
        checkInputClean('#name_0');
        checkInputClean('#complementaryAddressOne_0');
        checkDropdownWarning('#addr_0');
        checkInputWarning('#city_0');
        checkDropdownWarning('#province_0');
        checkDropdownWarning('#country_0');
        checkInputWarning('#postalCode_0');
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });

      it('should have location data with fuzzy resulting in Step 2: full address email match', () => {
        fillLocation(0);
        clickNext(2);
  
        checkNotification('location-addresses-0', 'warning', 'was found with similar email address.');
  
        checkInputClean('#name_0');
        checkInputClean('#complementaryAddressOne_0');
        checkDropdownClean('#addr_0');
        checkInputClean('#city_0');
        checkDropdownClean('#province_0');
        checkDropdownClean('#country_0');
        checkInputClean('#postalCode_0');
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });

      it('should have location data with fuzzy resulting in Step 2: full address business phone match', () => {
        fillLocation(0);
        clickNext(2);
  
        checkNotification('location-addresses-0', 'warning', 'was found with similar primary phone number');
  
        checkInputClean('#name_0');
        checkInputClean('#complementaryAddressOne_0');
        checkDropdownClean('#addr_0');
        checkInputClean('#city_0');
        checkDropdownClean('#province_0');
        checkDropdownClean('#country_0');
        checkInputClean('#postalCode_0');
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });

      it('should have location data with fuzzy resulting in Step 2: full address secondary phone match', () => {
        fillLocation(0);
        clickNext(2);
  
        checkNotification('location-addresses-0', 'warning', 'was found with similar secondary phone number');
  
        checkInputClean('#name_0');
        checkInputClean('#complementaryAddressOne_0');
        checkDropdownClean('#addr_0');
        checkInputClean('#city_0');
        checkDropdownClean('#province_0');
        checkDropdownClean('#country_0');
        checkInputClean('#postalCode_0');
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });

      it('should have location data with fuzzy resulting in Step 2: full address fax phone match', () => {
        fillLocation(0);
        clickNext(2);
  
        checkNotification('location-addresses-0', 'warning', 'was found with similar fax');
  
        checkInputClean('#name_0');
        checkInputClean('#complementaryAddressOne_0');
        checkDropdownClean('#addr_0');
        checkInputClean('#city_0');
        checkDropdownClean('#province_0');
        checkDropdownClean('#country_0');
        checkInputClean('#postalCode_0');
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });

    });
  
    describe("when there are two locations", () => {
      beforeEach(function () {
        fillLocation(0);

        clickAddLocation(1);
        fillLocation(1, {
          name: "Office 1",
        });
      });

      it('should have location at index: 0 with fuzzy resulting in Step 2: full address email 0 match', () => {
        clickNext(2);

        checkNotification('location-addresses-0', 'warning', 'was found with similar email address.');
        checkNotificationToNotExist('location-addresses-1');
        checkAddressAccordionState(1, false);
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });

      it('should have location at index: 1 with fuzzy resulting in Step 2: full address email 1 match', () => {
        clickNext(2);

        checkNotificationToNotExist('location-addresses-0');
        checkNotification('location-addresses-1', 'warning', 'was found with similar email address.');
        checkAddressAccordionState(1, true);
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });

      it('should have locations at indexes: 0, 1 with fuzzy resulting in Step 2: full address email 0 and 1 match', () => {
        clickNext(2);

        checkNotification('location-addresses-0', 'warning', 'was found with similar email address.');
        checkNotification('location-addresses-1', 'warning', 'was found with similar email address.');
        checkAddressAccordionState(1, true);
  
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
        fillLocation(0);

        clickAddLocation(1);
        fillLocation(1, {
          name: "Office 1",
        });

        clickAddLocation(2);
        fillLocation(2, {
          name: "Office 2",
        });
      });

      it('should have location at index: 1 with fuzzy resulting in Step 2: full address email 1 match', () => {
        clickNext(2);

        checkNotificationToNotExist('location-addresses-0');
        checkNotification('location-addresses-1', 'warning', 'was found with similar email address.');
        checkAddressAccordionState(1, true);
        checkNotificationToNotExist('location-addresses-2')
        checkAddressAccordionState(2, false);
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });

      it('should have location at index: 2 with fuzzy resulting in Step 2: full address email 2 match', () => {
        clickNext(2);

        checkNotificationToNotExist('location-addresses-0');
        checkNotificationToNotExist('location-addresses-1');
        checkAddressAccordionState(1, false);
        checkNotification('location-addresses-2', 'warning', 'was found with similar email address.');
        checkAddressAccordionState(2, true);
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });

      it('should have locations at indexes: 1, 2 with fuzzy resulting in Step 2: full address email 1 and 2 match', () => {
        clickNext(2);

        checkNotificationToNotExist('location-addresses-0');
        checkNotification('location-addresses-1', 'warning', 'was found with similar email address.');
        checkAddressAccordionState(1, true);
        checkNotification('location-addresses-2', 'warning', 'was found with similar email address.');
        checkAddressAccordionState(2, true);
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });

    });

    describe("when we go back to the previous step", () => {
      beforeEach(function () {
        interceptFuzzyMatch(2, "Step 2: full address email match");
        fillLocation(0);
        clickNext(2);
        cy.get('#reviewStatement').should('be.visible');
        clickBack();
      });
      it("does not display the review statement checkbox", () => {
        cy.get('#reviewStatement').should('not.exist');
      });
    });

    describe('when we check the Review statement', () => {
      beforeEach(function () {
        interceptFuzzyMatch(2, "Step 2: full address email match");
        fillLocation(0);
        clickNext(2);

        checkNotification('location-addresses-0', 'warning', 'was found with similar email address.');
        cy.get('#reviewStatement').should('be.visible');

        cy.get("[data-test='wizard-next-button']")
          .shadow()
          .find("button")
          .should("be.disabled");

        cy.markCheckbox("#reviewStatement");
      });

      it("should reach the next step", () => {
        clickNext(null);

        cy.contains("h2", "Contacts");
      });
    });

  });

  describe('Contacts fuzzy matching',() =>{
    beforeEach(function () {
      fillRegistered({ birthdateYear: '',
        birthdateMonth: '',
        birthdateDay: '',
      });
      clickNext(1);
      clickNext(2);
      cy.wait("@getRoles");
    });

    describe("when there is only one contact", () => {

      it('should have contact data with fuzzy resulting in Step 3: full contact match', () => {
        // Using this because the name has already been supplied by the client API.
        fillContactWithoutName();

        clickNext(3);
  
        checkNotification('location-contacts-0', 'warning', 'was found with similar contact');
  
        checkInputWarning('#firstName_0');
        checkInputWarning('#lastName_0');
        checkInputWarning('#emailAddress_0');
        checkInputClean('#businessPhoneNumber_0');
        checkInputClean('#secondaryPhoneNumber_0');
        checkInputClean('#faxNumber_0');
        checkDropdownClean('#role_0');
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });

      it('should have contact data with fuzzy resulting in Step 3: full contact email match', () => {
        fillContactWithoutName();
        clickNext(3);
  
        checkNotification('location-contacts-0', 'warning', 'was found with similar email address.');
  
        checkInputClean('#firstName_0');
        checkInputClean('#lastName_0');
        checkInputWarning('#emailAddress_0');
        checkInputClean('#businessPhoneNumber_0');
        checkInputClean('#secondaryPhoneNumber_0');
        checkInputClean('#faxNumber_0');
        checkDropdownClean('#role_0');
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });

      it('should have contact data with fuzzy resulting in Step 3: full contact business phone match', () => {
        fillContactWithoutName();
        clickNext(3);
  
        checkNotification('location-contacts-0', 'warning', 'was found with similar primary phone number');
  
        checkInputClean('#firstName_0');
        checkInputClean('#lastName_0');
        checkInputClean('#emailAddress_0');
        checkInputWarning('#businessPhoneNumber_0');
        checkInputClean('#secondaryPhoneNumber_0');
        checkInputClean('#faxNumber_0');
        checkDropdownClean('#role_0');
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });

      it('should have contact data with fuzzy resulting in Step 3: full contact secondary phone match', () => {
        fillContactWithoutName();
        clickNext(3);
  
        checkNotification('location-contacts-0', 'warning', 'was found with similar secondary phone number');
  
        checkInputClean('#firstName_0');
        checkInputClean('#lastName_0');
        checkInputClean('#emailAddress_0');
        checkInputClean('#businessPhoneNumber_0');
        checkInputWarning('#secondaryPhoneNumber_0');
        checkInputClean('#faxNumber_0');
        checkDropdownClean('#role_0');
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });

      it('should have contact data with fuzzy resulting in Step 3: full contact fax phone match', () => {
        fillContactWithoutName();
        clickNext(3);
  
        checkNotification('location-contacts-0', 'warning', 'was found with similar fax');
  
        checkInputClean('#firstName_0');
        checkInputClean('#lastName_0');
        checkInputClean('#emailAddress_0');
        checkInputClean('#businessPhoneNumber_0');
        checkInputClean('#secondaryPhoneNumber_0');
        checkInputWarning('#faxNumber_0');
        checkDropdownClean('#role_0');
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });

      // test with 2 matches
      it('should have contact data with fuzzy resulting in Step 3: full contact and full contact secondary phone matches', () => {
        fillContactWithoutName();

        clickNext(3);
  
        checkNotification('location-contacts-0', 'warning', 'was found with similar contact');
  
        // from the full contact info
        checkInputWarning('#firstName_0');
        checkInputWarning('#lastName_0');
        checkInputWarning('#emailAddress_0');

        checkInputClean('#businessPhoneNumber_0');

        // from the full contact secondary phone 
        checkInputWarning('#secondaryPhoneNumber_0');

        checkInputClean('#faxNumber_0');
        checkDropdownClean('#role_0');
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });

    });
  
    describe("when there are two contacts", () => {
      beforeEach(function () {
        fillContactWithoutName();

        clickAddContact(1);
        fillContact(1);
      });

      it('should have contact at index: 0 with fuzzy resulting in Step 3: full contact email 0 match', () => {
        clickNext(3);

        checkNotification('location-contacts-0', 'warning', 'was found with similar email address.');
        checkNotificationToNotExist('location-contacts-1');
        checkContactAccordionState(1, false);

        checkInputWarning('#emailAddress_0');

        checkInputClean('#emailAddress_1');
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });

      it('should have contact at index: 1 with fuzzy resulting in Step 3: full contact email 1 match', () => {
        clickNext(3);

        checkNotificationToNotExist('location-contacts-0');
        checkNotification('location-contacts-1', 'warning', 'was found with similar email address.');
        checkContactAccordionState(1, true);

        checkInputClean('#emailAddress_0');

        checkInputWarning('#emailAddress_1');
  
        cy.get("[data-test='wizard-next-button']")
        .shadow()
        .find("button")
        .should("be.disabled");
  
        cy.get('#reviewStatement')
        .should('be.visible');
      });

      it('should have contacts at indexes: 0, 1 with fuzzy resulting in Step 3: full contact email 0 and 1 match', () => {
        clickNext(3);

        checkNotification('location-contacts-0', 'warning', 'was found with similar email address.');
        checkNotification('location-contacts-1', 'warning', 'was found with similar email address.');
        checkContactAccordionState(1, true);

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

    describe("when there are three contacts", () => {
      beforeEach(function () {
        // Using this because the name has already been supplied by the client API.
        fillContactWithoutName();

        clickAddContact(1);
        fillContact(1);

        clickAddContact(2);
        fillContact(2, {
          firstName: "Paul"
        });
      });

      it('should have contact at index: 1 with fuzzy resulting in Step 3: full contact email 1 match', () => {
        clickNext(3);

        checkNotificationToNotExist('location-contacts-0');
        checkNotification('location-contacts-1', 'warning', 'was found with similar email address.');
        checkContactAccordionState(1, true);
        checkNotificationToNotExist('location-contacts-2')
        checkContactAccordionState(2, false);

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

      it('should have contact at index: 2 with fuzzy resulting in Step 3: full contact email 2 match', () => {
        clickNext(3);

        checkNotificationToNotExist('location-contacts-0');
        checkNotificationToNotExist('location-contacts-1');
        checkContactAccordionState(1, false);
        checkNotification('location-contacts-2', 'warning', 'was found with similar email address.');
        checkContactAccordionState(2, true);

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

      it('should have contacts at indexes: 1, 2 with fuzzy resulting in Step 3: full contact email 1 and 2 match', () => {
        clickNext(3);

        checkNotificationToNotExist('location-contacts-0');
        checkNotification('location-contacts-1', 'warning', 'was found with similar email address.');
        checkContactAccordionState(1, true);
        checkNotification('location-contacts-2', 'warning', 'was found with similar email address.');
        checkContactAccordionState(2, true);

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

    describe("when we go back to the previous step", () => {
      beforeEach(function () {
        interceptFuzzyMatch(3, "Step 3: full contact email match");
        fillContactWithoutName();
        clickNext(3);
        cy.get('#reviewStatement').should('be.visible');
        clickBack();
      });
      it("does not display the review statement checkbox", () => {
        cy.get('#reviewStatement').should('not.exist');
      });
    });

    describe('when we check the Review statement', () => {
      beforeEach(function () {
        interceptFuzzyMatch(3, "Step 3: full contact email match");
        fillContactWithoutName();
        clickNext(3);

        checkNotification('location-contacts-0', 'warning', 'was found with similar email address.');
        cy.get('#reviewStatement').should('be.visible');

        cy.get("[data-test='wizard-next-button']")
          .shadow()
          .find("button")
          .should("be.disabled");

        cy.markCheckbox("#reviewStatement");
      });

      it("should reach the next step", () => {
        clickNext(null);

        cy.contains("h2", "Review");
      });
    });

  });

  const clickNext = (matchedStep: number | null) => {
    cy.get("[data-test='wizard-next-button']")
      .shadow()
      .find("button")
      .should("be.enabled");
    cy.get("[data-test='wizard-next-button']").click();
    if (matchedStep) {
      cy.wait(`@doMatch${matchedStep}`);
    }
  };

  const clickBack = () => {
    cy.get("[data-test='wizard-back-button']").click();
  };

  const clickAddLocation = (index: number) => {
    cy.contains("cds-button", "Add another location").should("be.visible").click();
    cy.get(`cds-accordion-item[data-focus="address-${index}-heading"]`).focused();
  };

  const clickAddContact = (index: number) => {
    cy.contains("cds-button", "Add another contact").should("be.visible").click();
    cy.get(`cds-accordion-item[data-focus="contact-${index}-heading"]`).focused();
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

  const fillLocation = (index: number, extraData: any = {}) => {
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

  const fillContactWithoutName = (extraData: any = {}) => {
    fillContact(0, extraData, true);
  };

  const fillContact = (index: number, extraData: any = {}, skipName = false) => {
    cy.fixture("testdata/contactBaseData").then((fixtureData: any) => {
      const data = { ...fixtureData, ...extraData };
      if (!skipName) {
        cy.fillFormEntry(`#firstName_${index}`, data.firstName);
        cy.fillFormEntry(`#lastName_${index}`, data.lastName);
      }
      cy.fillFormEntry(`#emailAddress_${index}`, data.emailAddress);
      cy.fillFormEntry(`#businessPhoneNumber_${index}`, data.businessPhoneNumber);
      cy.fillFormEntry(`#secondaryPhoneNumber_${index}`, data.secondaryPhoneNumber);
      cy.fillFormEntry(`#faxNumber_${index}`, data.faxNumber);
      cy.selectFormEntry(`#role_${index}`, data.role, false);
      cy.selectFormEntry(`#addressname_${index}`, data.addressname, true);
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
      
      cy.get(`#fuzzy-match-notification-${id} > div > span.body-compact-01`)
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

  const checkAccordionState = (dataFocus: string, open: boolean) => {
    cy.checkAccordionItemState(`[data-focus="${dataFocus}"]`, open);
  }

  const checkAddressAccordionState = (index: number, open: boolean) => {
    checkAccordionState(`address-${index}-heading`, open);
  }

  const checkContactAccordionState = (index: number, open: boolean) => {
    checkAccordionState(`contact-${index}-heading`, open);
  }

  const extractStepFixture = (text: string, step: number) =>{
    const regex = new RegExp(`Step\\s+${step}:\\s*([^S]*)`);
    const match = regex.exec(text);
    return match ? match[1].trim().replace(/\s/g, "_") : '';
  }

  const interceptFuzzyMatch = (step: number, fixture: string) => {

    const stepMatchFixture = extractStepFixture(fixture, step);

    if(stepMatchFixture){
      cy.fixture(`fuzzy/${stepMatchFixture}`).then((fixtureData: any) => {
        cy.intercept(
          {
            method: "POST",
            url: "**/api/clients/matches",
            headers: { "X-STEP": `${step}`, },
          }, {
          ...fixtureData,
          headers: {
            "content-type": "application/json;charset=UTF-8",
          },
        }).as(`doMatch${step}`);
      })
      .as(`doMatchFixture${step}`);
    } else {
      cy.fixture('fuzzy/no_match').then((fixtureData: any) => {
        cy.intercept(
          {
            method: "POST",
            url: "**/api/clients/matches",
            headers: { "X-STEP": `${step}`, },
          }, {
          ...fixtureData,
          headers: {
            "content-type": "application/json;charset=UTF-8",
          },
        }).as(`doMatch${step}`);
      })
      .as(`doMatchFixture${step}`);
    }

  }

});
