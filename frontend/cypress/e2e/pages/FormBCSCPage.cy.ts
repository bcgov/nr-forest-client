/* eslint-disable no-undef */
describe("BCSC Form", () => {
  
  const submitResponse = {
    "success":{
      statusCode: 201,
      body: {},
      headers: {
        location: "http://localhost:3000/clients/submissions/1",
        "Access-Control-Expose-Headers": "x-sub-count, location",
        "x-sub-id": "123456",
      },
      delay: 1000,
    },
    "failure": {
      statusCode: 400,
      body: [],
      headers: {},
      delay: 1000,
    },
  };

  beforeEach(function() {    

    cy.intercept("GET", "**/api/codes/districts?page=0&size=250", {
      fixture: "districts.json",
    }).as("getDistricts");

    cy.intercept("**/api/clients/name/*", {
      fixture: "business.json",
    }).as("searchCompany");

    cy.intercept("GET", "**/api/clients/XX9016140", {
      fixture: "example.json",
    }).as("selectCompany");

    cy.intercept("GET", "**/api/codes/countries/CA",{
      fixture: "countryCodeCA.json",
    }).as("getCanadaByCode");

    cy.intercept("GET", "**/api/clients/submissions/duplicate-check/U/NaN", {
      fixture: "submissionDuplicationCheckValid.json"
    }).as("getValidSubmissionDuplicationCheck");

    const response = this.currentTest.title.endsWith("failure") ? submitResponse.failure : submitResponse.success;

    cy.intercept("POST", "**/api/clients/submissions",function(req) {
      req.reply(response);
    }).as("submitForm");

    cy.intercept("GET", "**/api/clients/individual/**", {
      statusCode: 200,
      body: {},
    }).as("getIndividual");

    cy.intercept("GET", "**/api/codes/contact-types?page=0&size=250", {
      statusCode: 200,
      body: [{"code":"P","name":"Person"}],
    }).as("getContactTypes");

    cy.visit("/");
    
    cy.get("#landing-title").should("contain", "Forests Client Management System");

    cy.get("#landing-subtitle").should(
      "contain",
      "Create and manage client accounts"
    );

    cy.login("uattest@gov.bc.ca", "Uat Test", "ca.bc.gov.flnr.fam.dev",
    {
      address: {
        formatted: "{\"street_address\":\"4000 SEYMOUR PLACE\",\"country\":\"CA\",\"locality\":\"VICTORIA\",\"region\":\"BC\",\"postal_code\":\"V8Z 1C8\"}"
      },
      given_name: "James",
      family_name: "Baxter",
      birthdate: "1986-11-12"
    }
    );

    cy.wait("@getDistricts");
  });

  it("should submit the form with the required information", () => {
    cy.get("#district")
      .should("be.visible")
      .and("have.value", "")
      .find("[part='trigger-button']")
      .click();

    cy.get("#district")
      .find('cds-combo-box-item[data-id="DCC"]')
      .should("be.visible")
      .click()
      .and("have.value", "DCC - Cariboo-Chilcotin Natural Resource District");

    cy.get("#phoneNumberId").shadow().find("input").type("2503008326");

    cy.get('[data-test="wizard-submit-button"]')
    .click();

    cy.get("h1").should('contain', 'Application submitted!');
  });

  it("should disable the Submit button after it's clicked", () => {
    cy.get("#district").find("[part='trigger-button']").click();

    cy.get("#district")
      .find("cds-combo-box-item[data-id='DCC']")
      .click()
      .should("have.value", "DCC - Cariboo-Chilcotin Natural Resource District");

    cy.get("#phoneNumberId").shadow().find("input").type("2503008326");

    cy.get("[data-test='wizard-submit-button']").click();

    cy.get("[data-test='wizard-submit-button']").shadow().find("button").should("be.disabled");
  });

  describe("when submission fails", () => {
    it("should disable the Submit button after clicked, then re-enable it after the failure", () => {
      cy.get("#district").find("[part='trigger-button']").click();

      cy.get("#district")
        .find("cds-combo-box-item[data-id='DCC']")
        .click()
        .should("have.value", "DCC - Cariboo-Chilcotin Natural Resource District");

      cy.get("#phoneNumberId").shadow().find("input").type("4004004000");

      cy.get("[data-test='wizard-submit-button']").click();

      cy.get("[data-test='wizard-submit-button']").shadow().find("button").should("be.disabled");

      cy.wait("@submitForm");

      cy.get("[data-test='wizard-submit-button']").shadow().find("button").should("be.enabled");
    });
  });

  it("should add a new contact", () => {

    cy.get("#district")
      .should("be.visible")
      .and("have.value", "")
      .find("[part='trigger-button']")
      .click();

    cy.get("#district")
      .find('cds-combo-box-item[data-id="DCC"]')
      .should("be.visible")
      .click()
      .and("have.value", "DCC - Cariboo-Chilcotin Natural Resource District");

    cy.get("#phoneNumberId").shadow().find("input").type("2503008326");

    cy
    .get('.form-steps-section > [kind="tertiary"]')
    .should("be.visible")
    .click();

    cy.get("#firstName_1").shadow().find("input").should('be.focused');

    cy
    .get('#role_1')
    .should("be.visible")
    .shadow()
    .find("input")
    .click();

    cy
    .get('#role_1')
    .find('cds-combo-box-item')
    .should("be.visible")
    .click();

    cy.get("#firstName_1").shadow().find("input").type("James");

    cy.get('#lastName_1')
    .should("be.visible")
      .shadow()
      .find("input")
      .should("have.value", "")
      .type('McCloud');

    cy.get('#email_1')
    .should("be.visible")
      .shadow()
      .find("input")
      .should("have.value", "")
      .type('jmccloud@starfox.aa');

    cy.get('#phoneNumber_1')
    .should("be.visible")
      .shadow()
      .find("input")
      .should("have.value", "")
      .type('2773008326');

    cy.get('[data-test="wizard-submit-button"]')
    .click();

    cy.get("h1").should('contain', 'Application submitted!');

  });

  it("should add a new contact, then remove it", () => {

    cy.get("#district")
      .should("be.visible")
      .and("have.value", "")
      .find("[part='trigger-button']")
      .click();

    cy.get("#district")
      .find('cds-combo-box-item[data-id="DCC"]')
      .should("be.visible")
      .click()
      .and("have.value", "DCC - Cariboo-Chilcotin Natural Resource District");

    cy
    .get('#phoneNumberId')
    .should("be.visible")
      .shadow()
      .find("input")
      .should("have.value", "")
      .type('2503008326');

    cy
    .get('.form-steps-section > [kind="tertiary"]')
    .should("be.visible")
    .click();

    cy.get("#firstName_1").shadow().find("input").should('be.focused');

    cy
    .get('#role_1')
    .should("be.visible")
    .shadow()
    .find("input")
    .click();

    cy
    .get('#role_1')
    .find('cds-combo-box-item')
    .should("be.visible")
    .click();

    cy
    .get('#firstName_1')
    .should("be.visible")
      .shadow()
      .find("input")
      .should("have.value", "")
      .type('James');

    cy.get('#lastName_1')
    .should("be.visible")
      .shadow()
      .find("input")
      .should("have.value", "")
      .type('McCloud');

    cy.get('#email_1')
    .should("be.visible")
      .shadow()
      .find("input")
      .should("have.value", "")
      .type('jmccloud@starfox.aa');

    cy.get('#phoneNumber_1')
    .should("be.visible")
      .shadow()
      .find("input")
      .should("have.value", "")
      .type('2773008326');

    cy.get('#deleteContact_1').click();

    cy.wait(10);

    cy.get('#modal-global > cds-modal-footer > .cds--modal-submit-btn').click();

    cy.get('[data-test="wizard-submit-button"]')
    .click();

    cy.get("h1").should('contain', 'Application submitted!');
  });

  describe("when a contact which is not the last one gets deleted", () => {
    const otherContactNames = ["George", "Ringo"];
    const addContact = (contactId: number, firstName: string) => {
      cy.get('.form-steps-section > [kind="tertiary"]').should("be.visible").click();
      
      cy.get(`#firstName_${contactId}`).shadow().find("input").should('be.focused')
      cy.get(`#firstName_${contactId}`).shadow().find("input").type(firstName);
    };
    const fillContact = (contactId: number) => {
      cy.get(`#role_${contactId}`).should("be.visible").shadow().find("input").click();

      cy.get(`#role_${contactId}`).find("cds-combo-box-item").should("be.visible").click();

      cy.get(`#lastName_${contactId}`)
        .should("be.visible")
        .shadow()
        .find("input")
        .should("have.value", "")
        .type("McCloud");

      cy.get(`#email_${contactId}`)
        .should("be.visible")
        .shadow()
        .find("input")
        .should("have.value", "")
        .type("jmccloud@starfox.aa");

      cy.get(`#phoneNumber_${contactId}`)
        .should("be.visible")
        .shadow()
        .find("input")
        .should("have.value", "")
        .type("2773008326");
    };
    beforeEach(() => {
      otherContactNames.forEach((name, index) => {
        addContact(index + 1, name);
      });
    });
    it("removes the intended contact from the DOM", () => {
      cy.get("#deleteContact_1").click();
      cy.wait(10);
      cy.get("#modal-global > cds-modal-footer > .cds--modal-submit-btn").click();
      cy.get("#firstName_1").should("not.exist");
      cy.get("#firstName_2").should("exist");
    });
    it("can submit the form (regardless of the deleted contact being invalid)", () => {
      cy.get("#deleteContact_1").click();
      cy.wait(10);
      cy.get("#modal-global > cds-modal-footer > .cds--modal-submit-btn").click();

      cy.get("#district")
        .should("be.visible")
        .and("have.value", "")
        .find("[part='trigger-button']")
        .click();

      cy.get("#district")
        .find('cds-combo-box-item[data-id="DCC"]')
        .should("be.visible")
        .click()
        .and("have.value", "DCC - Cariboo-Chilcotin Natural Resource District");

      cy.get("#phoneNumberId")
        .should("be.visible")
        .shadow()
        .find("input")
        .should("have.value", "")
        .type("2503008326");

      fillContact(2);

      cy.get('[data-test="wizard-submit-button"]').shadow().find("button").should("be.enabled");
    });
  });
});
