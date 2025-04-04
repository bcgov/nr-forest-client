describe("unauthorized access scenarios", () => {
  describe("when user role is Viewer", () => {
    beforeEach(() => {
      cy.visit("/");

      cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
        given_name: "James",
        family_name: "Baxter",
        "cognito:groups": ["CLIENT_VIEWER"],
      });
    });
    describe("and tries to access a route that requires role Editor", () => {
      beforeEach(() => {
        cy.visit("/new-client-staff");
      });

      it("doesn't redirect but shows the unauthorized banner instead of the default content of Create client page", () => {
        cy.get("cds-actionable-notification").contains(
          "You are not authorized to access this system or page",
        );

        // adjusts with the side-menu
        cy.get("#screen").should("have.attr", "class", "idir-content");

        cy.location().its("pathname").should("eq", "/new-client-staff");

        cy.contains("Create client").should("not.exist");
      });
    });
    describe("and tries to access a route that requires a provider other than IDIR", () => {
      beforeEach(() => {
        // route for BCeID users
        cy.visit("/new-client");
      });

      it("redirects to the Search page", () => {
        cy.contains("Search");

        cy.location().its("pathname").should("eq", "/search");
      });
    });
  });

  const noRoleScenarios = [[], ["unknown"]];

  noRoleScenarios.forEach((roles) => {
    describe(`when idir user has ${roles.length > 0 ? "unknown role" : "no role at all"}`, () => {
      beforeEach(() => {
        cy.visit("/");

        cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
          given_name: "James",
          family_name: "Baxter",
          "cognito:groups": roles,
        });
      });
      describe("and tries to access any staff route", () => {
        beforeEach(() => {
          cy.visit("/search");
        });

        it("redirects to the Unauthorized page", () => {
          cy.location().its("pathname").should("eq", "/unauthorized");

          cy.get("cds-actionable-notification").contains(
            "You are not authorized to access this system or page",
          );

          // no side-menu
          cy.get("#screen").should("have.attr", "class", "no-menu-content");
        });
      });
      describe("and tries to access a route that requires a provider other than IDIR", () => {
        beforeEach(() => {
          cy.visit("/new-client");
        });

        it("redirects to the Unauthorized page", () => {
          cy.location().its("pathname").should("eq", "/unauthorized");

          cy.get("cds-actionable-notification").contains(
            "You are not authorized to access this system or page",
          );

          // no side-menu
          cy.get("#screen").should("have.attr", "class", "no-menu-content");
        });
      });
    });
  });

  describe("when user is BCeID", () => {
    beforeEach(() => {
      cy.visit("/");

      cy.login("uattest@forest.client", "Uat Test", "bceidbusiness");
    });
    describe("and tries to access a route that requires the IDIR provider", () => {
      beforeEach(() => {
        // route for BCeID users
        cy.visit("/search");
      });

      it("redirects to the New client application page", () => {
        cy.contains("New client application");

        cy.location().its("pathname").should("eq", "/new-client");
      });
    });
  });
});
