/* eslint-disable no-undef */
describe("BCSC Form", () => {
  beforeEach(() => {
    cy.intercept("GET", "/api/districts?page=0&size=250", {
      fixture: "districts.json",
    }).as("getDistricts");

    cy.intercept("http://localhost:8080/api/clients/name/*", {
      fixture: "business.json",
    }).as("searchCompany");

    cy.intercept("GET", "/api/clients/XX9016140", {
      fixture: "example.json",
    }).as("selectCompany");

    cy.intercept("GET", "/api/countries/CA",{
      fixture: "countryCodeCA.json",
    }).as("getCanadaByCode");

    cy.intercept("POST", "/api/clients/submissions", {
      statusCode: 201,
      headers: {
        location: "http://localhost:8080/api/clients/submissions/1",
        "x-sub-id": "123456",
      },
    }).as("submitForm");

    cy.visit("/");
    cy.wait(500);

    cy.get("#landing-title").should("contain", "Client Management System");

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
      .click()
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
    .get('#phoneNumberId')
    .blur();

    cy.get('[data-test="wizard-submit-button"]')
    .click();

    cy.get('h4.fluid').should('contain', 'Application submitted!');
  });

  it("should add a new contact", () => {

    cy.get("#district")
      .should("be.visible")
      .and("have.value", "")
      .click()
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

    cy.focused().should('have.id', 'firstName_1');

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

    cy.get('[data-test="wizard-submit-button"]')
    .click();

    cy.get('h4.fluid').should('contain', 'Application submitted!');

  });

  it("should add a new contact, then remove it", () => {

    cy.get("#district")
      .should("be.visible")
      .and("have.value", "")
      .click()
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

    cy.focused().should('have.id', 'firstName_1');

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

    cy.wait(150);

    cy.get('#modal-global > cds-modal-footer > .cds--modal-submit-btn').click();

    cy.get('[data-test="wizard-submit-button"]')
    .click();

    cy.get('h4.fluid').should('contain', 'Application submitted!');
  });

  describe("when a contact which is not the last one gets deleted", () => {
    const otherContactNames = ["George", "Ringo"];
    const addContact = (contactId: number, firstName: string) => {
      cy.get('.form-steps-section > [kind="tertiary"]').should("be.visible").click();

      cy.focused().should("have.id", `firstName_${contactId}`);

      cy.get(`#firstName_${contactId}`)
        .should("be.visible")
        .shadow()
        .find("input")
        .should("have.value", "")
        .type(firstName);
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
      cy.wait(150);
      cy.get("#modal-global > cds-modal-footer > .cds--modal-submit-btn").click();
      cy.get("#firstName_1").should("not.exist");
      cy.get("#firstName_2").should("exist");
    });
    it("can submit the form (regardless of the deleted contact being invalid)", () => {
      cy.get("#deleteContact_1").click();
      cy.wait(150);
      cy.get("#modal-global > cds-modal-footer > .cds--modal-submit-btn").click();

      cy.get("#district")
        .should("be.visible")
        .and("have.value", "")
        .click()
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
