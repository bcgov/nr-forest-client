describe("Search Page", () => {
  beforeEach(() => {
    cy.intercept("https://green-domain.com/**", {
      body: `<html>
        <body>
          <h1>Green interface stub</h1>
        </body>
      </html>`,
    });

    cy.viewport(1920, 1080);
    cy.visit("/");

    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_VIEWER"],
    });

    // Check if the Client search button is visible
    cy.get("#menu-list-search").should("be.visible").click();

    cy.get("h1").should("be.visible").should("contain", "Client search");
  });

  describe("when user fills in the search box", () => {
    beforeEach(() => {
      cy.fillFormEntry("#search-box", "car", { skipBlur: true });
    });
    it("displays autocomplete results", () => {
      cy.get("#search-box")
        .find("cds-combo-box-item")
        .should("have.length", 3)
        .should("be.visible");
    });

    describe("and user clicks a result", () => {
      const clientNumber = "00001297";
      beforeEach(() => {
        cy.get("#search-box")
          .find("cds-combo-box-item")
          .should("have.length", 3)
          .should("be.visible");

        cy.get("#search-box").find(`cds-combo-box-item[data-value^="${clientNumber}"]`).click();
      });
      it("navigates to the client details", () => {
        cy.origin("https://green-domain.com", { args: { clientNumber } }, ({ clientNumber }) => {
          cy.url().should(
            "include",
            `/int/client/client02MaintenanceAction.do?bean.clientNumber=${clientNumber}`,
          );
        });
      });
    });
  });
});
