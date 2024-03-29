import BusinessInformationWizardStep from "@/pages/bceidform/BusinessInformationWizardStep.vue";
import type { FormDataDto } from "@/dto/ApplyClientNumberDto";
import districts from "../../../cypress/fixtures/districts.json"

describe('<BusinessInformationWizardStep />', () => {

  beforeEach(() => {
    cy.intercept("/api/clients/name/*", {
      fixture: "business.json",
    }).as("searchCompany");

    cy.intercept("GET", "/api/clients/BC1234567", {
      statusCode: 406,
      body: "Client type BC is not supported at the moment",
    });

    cy.intercept("GET", "/api/clients/SP1234567", {
      statusCode: 422,
      body: "Unable to process request. This sole proprietor is not owner by a person",
    });

    cy.intercept("GET", "/api/codes/clientTypes/C", {
      statusCode: 200,
      body: {
        code: "C",
        name: "Corporation",
      },
    }).as("getClientType");
  });

  it('renders the BusinessInformationWizardStep component and interacts with elements', () => {
    cy.mount(BusinessInformationWizardStep, {
      props: {
        data: {
          businessInformation: {
            businessType: "",
            legalType: "",
            clientType: "",
            registrationNumber: "",
            businessName: "",
            goodStandingInd: "",
            birthdate: "",
            address: ""
          },
        } as unknown as FormDataDto,
        districtsList: districts,
        active: false,
      },
    });

    cy.get('#businessTyperbR').click();
 
    cy.get('.link-button:visible').should('have.attr', 'aria-label', 'Contact BC Registry via Email');
    cy.get('.link-button:visible span').should('exist');
    cy.get('.link-button:visible span').invoke('text').should('not.be.empty');
  });

  it('shows "Client type not supported"', () => {
    cy.mount(BusinessInformationWizardStep, {
      props: {
        data: {
          businessInformation: {
            businessType: "",
            legalType: "",
            clientType: "",
            registrationNumber: "",
            businessName: "",
            goodStandingInd: "",
            birthdate: "",
            address: ""
          },
          location: {
            contacts: [
              {
                email: "john@doe.com",
                firstName: "John",
              },
            ],
          },
        } as unknown as FormDataDto,
        districtsList: districts,
        active: false,
      },
    });

    cy.get("#businessTyperbR").click();

    cy.get("#business")
      .should("be.visible")
      .shadow()
      .find("input")
      .should("have.value", "")
      .type("Unsupported");
    cy.wait("@searchCompany");

    cy.get('cds-combo-box-item[data-id="BC1234567"]').click();

    cy.get("cds-inline-notification")
      .shadow()
      .contains("Client type not supported")
      .should("be.visible");

    // The name of the client type
    cy.contains("cds-inline-notification", "Corporation").should("be.visible");
  });

  const showsUnknownSoleProprietor = () => {
    cy.mount(BusinessInformationWizardStep, {
      props: {
        data: {
          businessInformation: {
            businessType: "",
            legalType: "",
            clientType: "",
            registrationNumber: "",
            businessName: "",
            goodStandingInd: "",
            birthdate: "",
            address: ""
          },
          location: {
            contacts: [
              {
                email: "john@doe.com",
                firstName: "John",
              },
            ],
          },
        } as unknown as FormDataDto,
        districtsList: districts,
        active: false,
      },
    });

    cy.get("#businessTyperbR").click();

    cy.get("#business")
      .should("be.visible")
      .shadow()
      .find("input")
      .should("have.value", "")
      .type("Unknown");
    cy.wait("@searchCompany");

    cy.get('cds-combo-box-item[data-id="SP1234567"]').click();

    cy.get("cds-inline-notification")
      .shadow()
      .contains("Unknown sole proprietor")
      .should("be.visible");
  };

  it('shows "Unknown sole proprietor"', () => {
    showsUnknownSoleProprietor();
  });

  it('clears the error when Type of business changes', () => {
    showsUnknownSoleProprietor();

    cy.get("#businessTyperbU").click();

    cy.get("cds-inline-notification").should("not.exist");
  });

  it('clears the error when the business name gets cleared', () => {
    showsUnknownSoleProprietor();

    cy.get("#business")
      .should("be.visible")
      .shadow()
      .find("#selection-button") // The X clear button
      .click();

    cy.get("cds-inline-notification").should("not.exist");
  });
});
