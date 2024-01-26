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

    cy.get("h3").should("exist").should("contain", "Submissions");

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

        cy.get(".submission-details--title > span").should(
          "contain",
          "Auto approved client:"
        );

        cy.get('[data-testid="display-row-icon"]')
          .should("exist")
          .should("have.prop", "tagName", "svg")
          .should("have.attr", "alt", "Auto approved client");

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

        //Go to the submission list page
        cy.visit("/submissions");

        //Some simple checks to make sure we are on the list page
        cy.contains("Submission");

        cy.get("h3").should("exist").should("contain", "Submissions");

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

        cy.get(".submission-details--title > span").should(
          "contain",
          "Review new client:"
        );

        cy.get('[data-testid="display-row-icon"]')
          .should("exist")
          .should("have.prop", "tagName", "svg")
          .should("have.attr", "alt", "Review new client");

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

        if (testCase.incorporationNumber) {
          cy.get("cds-actionable-notification")
            .should("exist")
            .should(
              "contain",
              "Review their information in the Client Management System to determine if this submission should be approved or rejected:"
            )
            .should(
              "contain",
              `Partial match on incorporation number - Client number: ${testCase.incorporationNumber}`
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

        cy.get('.grouping-15 > [kind="primary"]').should("exist");

        cy.get('.grouping-15 > [kind="danger"]').should("exist");
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
    const testDisable = (success: boolean) => {
      // Load the fixture for the details
      cy.intercept("GET", "api/clients/submissions/*", {
        fixture: "test-case-review-goodstanding.json",
      }).as("loadSubmission");

      const statusCode = success ? 202 : 400;
      cy.intercept("POST", "api/clients/submissions/*", (req) => {
        req.reply({
          statusCode,
          delay: 500, // allow some time to make some assertions before the response.
        });
      }).as("action");

      // Click any submission
      cy.get('[sort-id="0"] > :nth-child(2)').click();

      cy.wait("@loadSubmission").its("response.body.submissionStatus").should("eq", "New");

      cy.get('[data-testid="display-row-icon"]')
        .should("exist")
        .should("have.prop", "tagName", "svg")
        .should("have.attr", "alt", "Review new client");

      cy.contains("cds-button", buttonLabel).click();

      if (action === "reject") {
        cy.get("#reject_reason_id").click();
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
        testDisable(true);
      });
      describe("when the action fails", () => {
        beforeEach(() => {
          testDisable(false);
        });
        it("should re-enable the buttons", () => {
          cy.wait("@action");

          // the approve/reject buttons are enabled again
          cy.contains("cds-button", approveLabel).should("not.have.attr", "disabled");
          cy.contains("cds-modal-footer-button", approveLabel).should("not.have.attr", "disabled");
          cy.contains("cds-button", rejectLabel).should("not.have.attr", "disabled");

          /*
          For the Reject in the modal, it depends if the reason input is alread filled.
          */
          if (action === "reject") {
            // Value is not empty
            cy.get("#reject_reason_id").should("not.have.value", "");

            // Thus button was properly re-enabled
            cy.contains("cds-modal-footer-button", rejectLabel).should("not.have.attr", "disabled");
          } else {
            // Value is empty
            cy.get("#reject_reason_id").should("have.value", "");

            // Thus button remains disabled
            cy.contains("cds-modal-footer-button", rejectLabel).should("have.attr", "disabled");
          }
        });
      });
    });
  });

  afterEach(() => {
    //Go to the submission list page
    cy.visit("/submissions");

    //Some simple checks to make sure we are on the list page
    cy.contains("Submission");

    cy.get("h3").should("exist").should("contain", "Submissions");

    cy.get(".body-compact-01")
      .should("exist")
      .should("contain", "Check and manage client submissions");
  });
});
