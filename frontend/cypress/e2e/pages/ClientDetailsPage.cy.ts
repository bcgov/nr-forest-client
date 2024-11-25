describe("Client Details Page", () => {
  beforeEach(() => {
    cy.visit("/");

    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_VIEWER"],
    });

    cy.visit("/clients/0");
  });

  it("renders the page skeleton", () => {
    cy.get("cds-breadcrumb").should("contain", "Client search");
    cy.contains("h2", "Client summary");
    cy.contains("cds-tab", "Client locations");
    cy.contains("cds-tab", "Client contacts");
    cy.contains("cds-tab", "Related clients");
    cy.contains("cds-tab", "Activity log");
  });
});
