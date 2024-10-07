describe("Search Page", () => {
  beforeEach(() => {
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

    })

    describe("and user clicks a result", () => {
      it("navigates to the client details", () => {

      });
    });
  });
});