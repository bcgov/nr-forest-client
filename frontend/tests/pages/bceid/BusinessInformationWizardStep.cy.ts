import BusinessInformationWizardStep from "@/pages/bceidform/BusinessInformationWizardStep.vue";
import type { FormDataDto } from "@/dto/ApplyClientNumberDto";
import districts from "../../../cypress/fixtures/districts.json";

const user = {
  userId: "bceid\\mockUserId",
  firstName: "John",
  lastName: "Doe",
};

const global = {
  config: {
    globalProperties: {
      $session: {
        user,
      },
      $features: {
        BCEID_MULTI_ADDRESS: false,
      },
    },
  },
};

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

    cy.intercept("GET", "/api/clients/SP2", {
      statusCode: 200,
      body: "Unable to process request. This sole proprietor is not owner by a person",
    });

    cy.intercept("GET", "/api/clients/XX9016140", {
      statusCode: 200,
      body: {
        name: "Anyone",
        id: "XX9016140",
        goodStanding: true,
        addresses: [{}],
        contacts: [
          {
            lastName: "Else",
          },
        ],
      },
      delay: 10,
    });

    cy.intercept("GET", "/api/codes/clientTypes/C", {
      statusCode: 200,
      body: {
        code: "C",
        name: "Corporation",
      },
    }).as("getClientType");

    cy.intercept("GET", "/api/clients/individual/mockUserId?lastName=Doe", {
      statusCode: 200,
      delay: 10,
    }).as("checkIndividualUser");

    cy.intercept("GET", "/api/clients/individual/mockUserId?lastName=Else", {
      statusCode: 404,
      delay: 10,
    }).as("checkIndividualElse");
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
      global,
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
            address: "",
            district: "DMH",
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
      global,
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
      global,
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

  describe("when a Registered individual business gets selected", () => {
    let callsSetIndividualValid = [];
    const functionWrapper = {
      setIndividualValid: (value: boolean) => {
        callsSetIndividualValid.push(value);
      },
    };
    const { setIndividualValid } = functionWrapper;
    beforeEach(() => {
      callsSetIndividualValid = [];
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
              address: "",
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
          individualValid: false,
          setIndividualValid,
        },
        global,
      });

      cy.get("#businessTyperbR").click();

      cy.get("#business").should("be.visible").shadow().find("input").type("Valid");
      cy.wait("@searchCompany");

      cy.get("cds-combo-box-item[data-id='XX9016140']").click();
      cy.wrap(callsSetIndividualValid).should((value) => {
        const lastCall = value.slice(-1)[0];
        expect(lastCall).to.equal(true);
      });
    });

    describe("and type of business is changed to Unregistered", () => {
      beforeEach(() => {
        callsSetIndividualValid = [];
        cy.get("#businessTyperbU").click();
      })
      it("should re-check with user's last name", () => {
        cy.wait("@checkIndividualUser");
        cy.wrap(callsSetIndividualValid).should((value) => {
          const lastCall = value.slice(-1)[0];
          expect(lastCall).to.equal(true);
        });
      });
    });
  });

  describe("when props.individualValue is true", () => {
    const individualValid = true;
    let callsSetIndividualValid = [];
    const functionWrapper = {
      setIndividualValid: (value: boolean) => {
        callsSetIndividualValid.push(value);
      },
    };
    const { setIndividualValid } = functionWrapper;
    describe("and type of business is Registered", () => {
      beforeEach(() => {
        callsSetIndividualValid = [];
        cy.mount(BusinessInformationWizardStep, {
          props: {
            data: {
              businessInformation: {
                businessType: "R",
                legalType: "",
                clientType: "RSP",
                registrationNumber: "",
                businessName: "abc",
                goodStandingInd: "",
                birthdate: "2000-01-01",
                address: "",
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
            individualValid,
            setIndividualValid,
          },
          global,
        });
      });
      describe("and type of business is changed to Unregistered", () => {
        beforeEach(() => {
          cy.get("#businessTyperbU").click();
        })
        it("should re-check individual validation", () => {
          cy.wait("@checkIndividualUser");
          cy.wrap(callsSetIndividualValid).should((value) => {
            const lastCall = value.slice(-1)[0];
            expect(lastCall).to.equal(true);
          });
        });
      });
      describe("and a different individual business gets selected", () => {
        beforeEach(() => {
          cy.get("#business").should("be.visible").shadow().find("input").type("Valid");
          cy.wait("@searchCompany");

          cy.get("cds-combo-box-item[data-id='XX9016140']").click();
        });
        it("should re-check individual validation", () => {
          cy.wait("@checkIndividualElse");
          cy.wrap(callsSetIndividualValid).should((value) => {
            const lastCall = value.slice(-1)[0];
            expect(lastCall).to.equal(true);
          });
        });
      });
    });

    describe("and type of business is Unregistered", () => {
      beforeEach(() => {
        callsSetIndividualValid = [];
        cy.mount(BusinessInformationWizardStep, {
          props: {
            data: {
              businessInformation: {
                businessType: "U",
                legalType: "",
                clientType: "USP",
                registrationNumber: "",
                businessName: "abc",
                goodStandingInd: "",
                birthdate: "2000-01-01",
                address: "",
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
            individualValid,
            setIndividualValid,
          },
          global,
        });
      });
      describe("and type of business is changed to Registered", () => {
        beforeEach(() => {
          cy.get("#businessTyperbR").click();
        });
        describe("and an indivual business gets selected", () => {
          beforeEach(() => {
            cy.get("#business").should("be.visible").shadow().find("input").type("Valid");
            cy.wait("@searchCompany");

            cy.get("cds-combo-box-item[data-id='XX9016140']").click();
          });
          it("should re-check individual validation", () => {
            cy.wait("@checkIndividualElse");
            cy.wrap(callsSetIndividualValid).should((value) => {
              const lastCall = value.slice(-1)[0];
              expect(lastCall).to.equal(true);
            });
          });
        });
      });
    });
  });
});
