import BusinessInformationWizardStep from "@/pages/bceidform/BusinessInformationWizardStep.vue";
import type { FormDataDto } from "@/dto/ApplyClientNumberDto";

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

let individualStatusCode: number;

describe('<BusinessInformationWizardStep />', () => {

  beforeEach(() => {
    individualStatusCode = 200;

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

    cy.intercept("GET", "/api/codes/client-types/C", {
      statusCode: 200,
      body: {
        code: "C",
        name: "Corporation",
      },
    }).as("getClientType");

    cy.intercept("GET", "/api/clients/individual/mockUserId?lastName=Doe", (req) => {
      req.reply({
        statusCode: individualStatusCode,
        delay: 10,
      });
    }).as("checkIndividualUser");

    cy.intercept("GET", "/api/clients/individual/mockUserId?lastName=Else", (req) => {
      req.reply({
        statusCode: individualStatusCode,
        delay: 10,
      });
    }).as("checkIndividualElse");
  });

  it('renders the BusinessInformationWizardStep component and interacts with elements', () => {

    cy.fixture('districts.json').then((districts) => {
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
    });

    cy.get('#businessTyperbR').click();
 
    cy.get('#bcRegistryEmailId').should('exist');

  });

  it('shows "Client type not supported"', () => {
    cy.fixture('districts.json').then((districts) => {
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
        },
        global,
      });
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
    cy.fixture('districts').then((districts) => {
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

  describe("individual validation", () => {
    let callsSetIndividualValidInd = [];
    const individualValidWrapper = {
      individualValidInd: false,
    };
    const functionWrapper = {
      setIndividualValidInd: (value: boolean) => {
        individualValidWrapper.individualValidInd = value;
        callsSetIndividualValidInd.push(value);
      },
    };
    const { setIndividualValidInd } = functionWrapper;
    beforeEach(() => {
      individualValidWrapper.individualValidInd = false;
      callsSetIndividualValidInd = [];

      cy.fixture("districts").then((districts) => {
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
            individualValidInd: individualValidWrapper.individualValidInd,
            setIndividualValidInd,
          },
          global,
        });
      });
    });

    interface Scenario {
      statusCode: number;
      valid: boolean;
    };
    const scenarios: Scenario[] = [
      { statusCode: 200, valid: true },
      { statusCode: 404, valid: true },
      { statusCode: 409, valid: false },
      { statusCode: 400, valid: false },
      { statusCode: 403, valid: false },
      { statusCode: 500, valid: false },
      { statusCode: 503, valid: false },
    ];
    scenarios.forEach((scenario) => {
      describe(`when response is ${scenario.statusCode}`, () => {
        beforeEach(() => {
          individualStatusCode = scenario.statusCode;
        });
        it(`should set valid to: ${scenario.valid}`, () => {
          cy.get("#businessTyperbU").click();
          cy.wait("@checkIndividualUser");
          cy.wrap(individualValidWrapper).should(({ individualValidInd }) => {
            expect(individualValidInd).to.equal(scenario.valid);
          });
        })
      });
    });
  });

  describe("when a Registered individual business gets selected", () => {
    let callsSetIndividualValidInd = [];
    const functionWrapper = {
      setIndividualValidInd: (value: boolean) => {
        callsSetIndividualValidInd.push(value);
      },
    };
    const { setIndividualValidInd } = functionWrapper;
    beforeEach(() => {
      callsSetIndividualValidInd = [];
      cy.fixture('districts').then((districts) => {
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
            individualValidInd: false,
            setIndividualValidInd,
          },
          global,
        });
      });

      cy.get("#businessTyperbR").click();

      cy.get("#business").should("be.visible").shadow().find("input").type("Valid");
      cy.wait("@searchCompany");

      cy.get("cds-combo-box-item[data-id='XX9016140']").click();
      cy.wrap(callsSetIndividualValidInd).should((value) => {
        const lastCall = value.slice(-1)[0];
        expect(lastCall).to.equal(true);
      });
    });

    describe("and type of business is changed to Unregistered", () => {
      beforeEach(() => {
        callsSetIndividualValidInd = [];
        cy.get("#businessTyperbU").click();
      })
      it("should re-check with user's last name", () => {
        cy.wait("@checkIndividualUser");
        cy.wrap(callsSetIndividualValidInd).should((value) => {
          const lastCall = value.slice(-1)[0];
          expect(lastCall).to.equal(true);
        });
      });
    });
  });

  describe("when props.individualValue is true", () => {
    const individualValidInd = true;
    let callsSetIndividualValidInd = [];
    const functionWrapper = {
      setIndividualValidInd: (value: boolean) => {
        callsSetIndividualValidInd.push(value);
      },
    };
    const { setIndividualValidInd } = functionWrapper;
    describe("and type of business is Registered", () => {
      beforeEach(() => {
        callsSetIndividualValidInd = [];
        cy.fixture('districts').then((districts) => {
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
              individualValidInd,
              setIndividualValidInd,
            },
            global,
          });
        });
      });

      describe("and type of business is changed to Unregistered", () => {
        beforeEach(() => {
          cy.get("#businessTyperbU").click();
        })
        it("should re-check individual validation", () => {
          cy.wait("@checkIndividualUser");
          cy.wrap(callsSetIndividualValidInd).should((value) => {
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
          cy.wrap(callsSetIndividualValidInd).should((value) => {
            const lastCall = value.slice(-1)[0];
            expect(lastCall).to.equal(true);
          });
        });
      });
    });

    describe("and type of business is Unregistered", () => {
      beforeEach(() => {
        callsSetIndividualValidInd = [];
        cy.fixture('districts').then((districts) => {
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
              individualValidInd,
              setIndividualValidInd,
            },
            global,
          });
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
            cy.wrap(callsSetIndividualValidInd).should((value) => {
              const lastCall = value.slice(-1)[0];
              expect(lastCall).to.equal(true);
            });
          });
        });
      });
    });
  });

});