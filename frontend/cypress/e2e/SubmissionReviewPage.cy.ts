import testCases from "../fixtures/test-cases-review-submissions.json";

/* eslint-disable no-undef */
describe("Submission Review Page", () => {
  //Login before all tests
  beforeEach(() => {
    cy.visit("/");
    cy.wait(500);

    cy.get("#landing-title").should("contain", "Client Management System");

    cy.get("#landing-subtitle").should(
      "contain",
      "Create and manage client accounts"
    );

    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
    });

    //Some simple checks to make sure we are on the list page
    cy.contains("Submission");

    cy.get("h1").should("exist").should("contain", "Submissions");

    cy.get(".body-compact-01")
      .should("exist")
      .should("contain", "Check and manage client submissions");
  });

  testCases.forEach((testCase) => {
    if (testCase.approved) {
      it(`Should check for ${testCase.name}`, () => {
        //Load the fixture for the details
        cy.intercept("GET", "api/clients/submissions/*", {
          fixture: testCase.fixture,
        }).as("loadSubmission");

        //Click any submission
        cy.get('[sort-id="0"] > :nth-child(2)').click();

        cy.wait("@loadSubmission")
          .its("response.body.submissionStatus")
          .should("eq", "Approved");

        cy.get('[data-testid="subtitle"]')
          .should("exist")
          .should("contain", "Check this new client data");

        cy.get("cds-actionable-notification")
          .should("exist")
          .should(
            "contain",
            "No matching client records or BC Registries standing issues were found. Review the details in the read-only version below."
          );

        cy.get(".grouping-10 > :nth-child(2) > .body-compact-01")
          .should("exist")
          .should("contain", testCase.clientNumber);

        cy.contains("Submission status").parent().should("contain", "Approved");

        //Go to the submission list page
        cy.visit("/submissions");

        //Some simple checks to make sure we are on the list page
        cy.contains("Submission");

        cy.get("h1").should("exist").should("contain", "Submissions");

        cy.get(".body-compact-01")
          .should("exist")
          .should("contain", "Check and manage client submissions");
      });
    } else {
      it(`Should check for ${testCase.name}`, () => {
        //Load the fixture for the details
        cy.intercept("GET", "api/clients/submissions/*", {
          fixture: testCase.fixture,
        }).as("loadSubmission");

        //Click any submission
        cy.get('[sort-id="0"] > :nth-child(2)').click();

        cy.wait("@loadSubmission")
          .its("response.body.submissionStatus")
          .should("eq", "New");

        cy.get('[data-testid="subtitle"]')
          .should("exist")
          .should(
            "contain",
            "Check and manage this submission for a new client number"
          );

        if (testCase.goodStanding) {
          cy.get("cds-actionable-notification")
            .should("exist")
            .should("contain", "Check this client's standing with");
        }
        
        if (testCase.incorporationName) {
          cy.get("cds-actionable-notification")
            .should("exist")
            .should(
              "contain",
              "Review their information in the Client Management System to determine if this submission should be approved or rejected:"
            )
            .should(
              "contain",
              `Partial match on business name - Client number: ${testCase.incorporationName}`
            );
        }

        if (testCase.registrationNumber) {
          cy.get("cds-actionable-notification")
            .should("exist")
            .should(
              "contain",
              "Review their information in the Client Management System to determine if this submission should be approved or rejected:"
            )
            .should(
              "contain",
              `Partial match on registration number - Client number: ${testCase.registrationNumber}`
            );
        }

        if (testCase.contact) {
          cy.get("cds-actionable-notification")
            .should("exist")
            .should(
              "contain",
              "Review their information in the Client Management System to determine if this submission should be approved or rejected:"
            )
            .should(
              "contain",
              `Matching one or more contacts - Client number: ${testCase.contact}`
            );
        }

        cy.contains("Submission status").parent().should("contain", "New");

        cy.get('.grouping-15 > [kind="primary"]').should("exist");

        cy.get('.grouping-15 > [kind="danger--tertiary"]').should("exist");
      });
    }
  });

  const approveLabel = "Approve submission";
  const rejectLabel = "Reject submission";
  const actions = [
    {
      action: "approve",
      buttonLabel: approveLabel,
    },
    {
      action: "reject",
      buttonLabel: rejectLabel,
    },
  ];
  actions.forEach(({ action, buttonLabel }) => {
    const testDisable = (statusCode = 202) => {
      // Load the fixture for the details
      cy.intercept("GET", "api/clients/submissions/*", {
        fixture: "test-case-review-goodstanding.json",
      }).as("loadSubmission");

      cy.intercept("POST", "api/clients/submissions/*", (req) => {
        req.reply({
          statusCode,
          delay: 500, // allow some time to make some assertions before the response.
        });
      }).as("action");

      // Click any submission
      cy.get('[sort-id="0"] > :nth-child(2)').click();

      cy.wait("@loadSubmission").its("response.body.submissionStatus").should("eq", "New");

      cy.contains("Submission status").parent().should("contain", "New");

      cy.contains("cds-button", buttonLabel).click();

      if (action === "reject") {
        cy.get("#reject_reason_id").find("[part='trigger-button']").click();
        cy.get("[data-id='goodstanding']").click();
      }

      cy.contains("cds-modal-footer-button", buttonLabel).click();

      // all approve/reject buttons are disabled, including the ones in the modal.
      actions.forEach(({ buttonLabel }) => {
        cy.contains("cds-button", buttonLabel).should("have.attr", "disabled");
        cy.contains("cds-modal-footer-button", buttonLabel).should("have.attr", "disabled");
      });
    };

    describe(`when the user clicks to ${action} the submission`, () => {
      it("should disable the approve/reject buttons", () => {
        testDisable(202);
      });
      describe("and the action fails", () => {
        describe("and it's not a conflict eror", () => {
          beforeEach(() => {
            testDisable(400);
          });
          it("should re-enable the buttons", () => {
            cy.wait("@action");

            // the approve/reject buttons are enabled again
            cy.contains("cds-button", approveLabel).should("not.have.attr", "disabled");
            cy.contains("cds-modal-footer-button", approveLabel).should(
              "not.have.attr",
              "disabled",
            );
            cy.contains("cds-button", rejectLabel).should("not.have.attr", "disabled");

            /*
            For the Reject in the modal, it depends if the reason input is alread filled.
            */
            if (action === "reject") {
              // Value is not empty
              cy.get("#reject_reason_id").should("not.have.value", "");

              // Thus button was properly re-enabled
              cy.contains("cds-modal-footer-button", rejectLabel).should(
                "not.have.attr",
                "disabled",
              );
            } else {
              // Value is empty
              cy.get("#reject_reason_id").should("have.value", "");

              // Thus button remains disabled
              cy.contains("cds-modal-footer-button", rejectLabel).should("have.attr", "disabled");
            }
          });
        });
        describe("and it is a conflict eror", () => {
          beforeEach(() => {
            testDisable(409);
          });
          it("should not re-enable the buttons", () => {
            cy.wait("@action");

            // all approve/reject buttons remain disabled.
            cy.contains("cds-button", approveLabel).should("have.attr", "disabled");
            cy.contains("cds-modal-footer-button", approveLabel).should("have.attr", "disabled");
            cy.contains("cds-button", rejectLabel).should("have.attr", "disabled");
            cy.contains("cds-modal-footer-button", rejectLabel).should("have.attr", "disabled");
          });
          it("should display the corresponding error message", () => {
            cy.wait("@action");

            cy.get("cds-actionable-notification")
              .shadow()
              .contains("Submission already approved or rejected")
              .should("be.visible");
          });
        });
      });
    });
  });

  const testGoodStanding = () => {
    // Load the fixture for the details
    cy.intercept("GET", "api/clients/submissions/*", {
      fixture: "test-case-review-goodstanding.json",
    }).as("loadSubmission");

    // Click any submission
    cy.get('[sort-id="0"] > :nth-child(2)').click();

    cy.wait("@loadSubmission").its("response.body.submissionStatus").should("eq", "New");
  };

  // TODO: have e2e tests running with BCEID_MULTI_ADDRESS enabled
  // describe("when BCEID_MULTI_ADDRESS is enabled", () => {
  //   it("should render 'Associated location' information", () => {
  //     testGoodStanding();
  //     cy.contains("Associated location").should("be.visible");
  //   });
  // });

  describe("when BCEID_MULTI_ADDRESS is disabled", () => {
    it("should not render 'Associated location' information", () => {
      testGoodStanding();
      cy.contains("Associated location").should("not.exist");
    });
  });

  afterEach(() => {
    //Go to the submission list page
    cy.visit("/submissions");

    //Some simple checks to make sure we are on the list page
    cy.contains("Submission");

    cy.get("h1").should("exist").should("contain", "Submissions");

    cy.get(".body-compact-01")
      .should("exist")
      .should("contain", "Check and manage client submissions");
  });
});
