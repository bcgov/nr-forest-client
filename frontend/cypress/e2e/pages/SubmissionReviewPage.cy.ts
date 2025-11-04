import testCases from "../../fixtures/test-cases-review-submissions.json";

/* eslint-disable no-undef */
describe("Submission Review Page", () => {

  const beforeInit = (fixtureName: string) => {
  
      cy.intercept("GET", "**api/clients/submissions?page=0&size=10", {
        body:[
          {
            "id": "1d676db1-7b48-4ed3-bdd3-10718bb39c5b",
            "requestType": "Review new client",
            "name": "Test Case",
            "clientType": "Corporation",
            "submittedAt": "2024-07-24 15:27:12",
            "status": "New",
            "user": "Test User",
            "district": "DCC - Cariboo-Chilcotin Natural Resource District"
          }
        ],
      }).as("loadSubmissions");

      cy.intercept("GET", "**api/clients/submissions/1d676db1-7b48-4ed3-bdd3-10718bb39c5b", {
        fixture: fixtureName,
      }).as("loadSubmission");
  
      cy.visit("/submissions");
        
      cy.get("#landing-title").should("contain", "Forests Client Management System");
  
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
  
      cy.wait("@loadSubmissions");
  
      //Click any submission
      cy.get('[sort-id="0"] > :nth-child(2)').click();
  };

  const approveLabel = "Approve submission";
  const rejectLabel = "Reject submission";
  const actions = [
    { action: "approve", buttonLabel: approveLabel, },
    { action: "reject", buttonLabel: rejectLabel, },
  ];

  const testDisable = (statusCode = 202) => {

    cy.intercept("POST", "**api/clients/submissions/*", (req) => {
      req.reply({
        statusCode,
        delay: 500, // allow some time to make some assertions before the response.
      });
    }).as("action");
  };

  describe("when user is reviewing submission",() =>{

    beforeEach(function() {
      const testName = this.currentTest.title.replace("Should check for ", "");
      const fixtureName = testCases.find((testCase) => testCase.name === testName).fixture
      beforeInit(fixtureName);      
    });
  
    testCases.forEach((testCase) => {
      if (testCase.approved) {

        it(`Should check for ${testCase.name}`, () => {

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

          cy.get("cds-tag > span").should("contain", "Approved");

        });

      } else {

        it(`Should check for ${testCase.name}`, () => {

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
                "Review their information in the Forests Client Management System to determine if this submission should be approved or rejected:"
              )
              .should(
                "contain",
                `Possible match with existing business name and/or ID - Client number: ${testCase.incorporationName}`
              );
          }

          if (testCase.registrationNumber) {
            cy.get("cds-actionable-notification")
              .should("exist")
              .should(
                "contain",
                "Review their information in the Forests Client Management System to determine if this submission should be approved or rejected:"
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
                "Review their information in the Forests Client Management System to determine if this submission should be approved or rejected:"
              )
              .should(
                "contain",
                `Matching one or more contacts - Client number: ${testCase.contact}`
              );
          }

          cy.get("cds-tag > span").should("contain", "New");
  
          cy.get('.grouping-15 > [kind="primary"]').should("exist");
  
          cy.get('.grouping-15 > [kind="danger--tertiary"]').should("exist");

        });

      }
    });

  });

  describe("when staff is checking pending submission completion",() =>{
    let incomplete = true;

    beforeEach(() => {
      let incomplete = true;
      cy.intercept("GET", "**api/clients/submissions/4444",(req) => {
        if(incomplete){
          req.reply({ fixture: 'test-case-review-staff-pending.json' })
        } else {
          req.reply({ fixture: 'test-case-review-staff-completed.json' })
        }
        incomplete = !incomplete;
      }).as("loadSubmissionData");

      beforeInit(`test-case-review-staff-${incomplete ? "pending" : "completed"}`);      
    });

    it("Should show notification and refresh if staff submitted is pending", () => {

      cy.visit("/submissions/4444");
      
      cy.wait("@loadSubmissionData")
        .its("response.body.submissionStatus")
        .should("eq", "Approved");
  
        cy.get("cds-actionable-notification")
        .should("exist")
        .should(
          "contain",
          "It may take a few minutes. Once completed, the client number will display in the \“Client summary\” section below."
        );
        
        cy.wait(10005);
        cy.wait("@loadSubmissionData")
        .its("response.body.business.clientNumber")
        .should("eq", "00140791");
        
  
        cy.get("cds-actionable-notification")
        .should("not.exist");
  
        cy.get('.grouping-10 > :nth-child(2) > .title-group-01 > .label-02')
        .should("contain", "Client number");
          
        cy.get(".grouping-10 > :nth-child(2) > .body-compact-01")
        .should("contain", "00140791");
  
  
    });

  });

  describe("when BCEID_MULTI_ADDRESS is disabled", () => {

    beforeEach(() => {
      beforeInit('test-case-review-goodstanding');
    });

    it("should not render 'Associated location' information", () => {
      cy.wait("@loadSubmission")
      .its("response.body.submissionStatus")
      .should("eq", "New");

      cy.contains("Associated location").should("not.exist");
    });

    // TODO: have e2e tests running with BCEID_MULTI_ADDRESS enabled
    // describe("when BCEID_MULTI_ADDRESS is enabled", () => {
    //   it("should render 'Associated location' information", () => {
    //     testGoodStanding();
    //     cy.contains("Associated location").should("be.visible");
    //   });
    // });

  });

  actions.forEach(({ action, buttonLabel }) => {
    
    describe(`when the user clicks to ${action} the submission`, () => {

      beforeEach(function() {
        const testStatus = this.currentTest.title.split("by returning ")[1];        
        testDisable(parseInt(testStatus));
        beforeInit('test-case-review-goodstanding');

        cy.wait("@loadSubmission").its("response.body.submissionStatus").should("eq", "New");

        cy.get("cds-tag > span").should("contain", "New");

        cy.contains("cds-button", buttonLabel).click();
  
        if (action === "reject") {
          cy.get("#reject_reason_id").find("[part='trigger-button']").click();
          cy.get("[data-id='goodstanding']").click();
        }

        cy.contains("cds-modal-footer-button", buttonLabel).click();
      });

      it("should approve/reject by returning 200", () => { 
        
        actions.forEach(({ buttonLabel }) => {
          cy.contains("cds-button", buttonLabel).should("have.attr", "disabled");
          cy.contains("cds-modal-footer-button", buttonLabel).should("have.attr", "disabled");
        });

        cy.wait("@action");
        cy.contains("Submission");  
        cy.get("h1").should("exist").should("contain", "Submissions");  
        cy.get(".body-compact-01")
          .should("exist")
          .should("contain", "Check and manage client submissions");
  
      });

      describe("and the action fails", () => {
        
        describe("and it's not a conflict error", () => {

          it("should re-enable the buttons by returning 400", () => {
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

        describe("and it is a conflict error", () => {

          it("should not re-enable the buttons by returning 409", () => {
            cy.wait("@action");

            // all approve/reject buttons remain disabled.
            cy.contains("cds-button", approveLabel).should("have.attr", "disabled");
            cy.contains("cds-modal-footer-button", approveLabel).should("have.attr", "disabled");
            cy.contains("cds-button", rejectLabel).should("have.attr", "disabled");
            cy.contains("cds-modal-footer-button", rejectLabel).should("have.attr", "disabled");
          });

          it("should display the corresponding error message by returning 409", () => {
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

  it("displays the birthdate when clientType is Individual", () => {
    beforeInit("test-case-review-staff-pending");
    cy.contains("h2", "Client summary")
      .parent()
      .within(() => {
        cy.contains("Client type").parents(".grouping-11").first().contains("Individual");
        cy.contains("Birthdate").should("be.visible");
      });
  });

  it("displays the birthdate when clientType is Registered sole proprietorship", () => {
    beforeInit("test-case-review-rsp");
    cy.contains("h2", "Client summary")
      .parent()
      .within(() => {
        cy.contains("Client type")
          .parents(".grouping-11")
          .first()
          .contains("Registered sole proprietorship");
        cy.contains("Birthdate").should("be.visible");
      });
  });
  
});
