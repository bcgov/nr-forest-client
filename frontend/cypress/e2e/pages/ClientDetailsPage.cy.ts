describe("Client Details Page", () => {
  beforeEach(() => {
    cy.visit("/");

    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_VIEWER"],
    });
  });

  it("renders the page skeleton", () => {
    cy.visit("/clients/details/0");

    cy.get("cds-breadcrumb").should("contain", "Client search");
    cy.contains("h2", "Client summary");
    cy.contains("cds-tab", "Client locations");
    cy.contains("cds-tab", "Client contacts");
    cy.contains("cds-tab", "Related clients");
    cy.contains("cds-tab", "Activity log");
  });

  const nameScenarios = [
    {
      id: "G",
      type: "corporation",
      expected: "Kovacek, Thompson And Boyer",
      expectedDescription: "Client name",
    },
    {
      id: "I",
      type: "individual without middle name",
      expected: "John Silver",
      expectedDescription: "First name + Last name",
    },
    {
      id: "S",
      type: "individual with middle name",
      expected: "Michael Gary Scott",
      expectedDescription: "First name + Middle name + Last name",
    },
  ];

  nameScenarios.forEach((scenario) => {
    const { type, id, expected, expectedDescription } = scenario;
    describe(`when client is a ${type}`, () => {
      beforeEach(() => {
        cy.visit(`/clients/details/${id}`);
      });
      it(`displays the full client name in the header as: ${expectedDescription}`, () => {
        cy.contains("h1", expected);
      });
    });
  });

  const errorScenarios = [
    {
      clientId: "enet",
      elId: "internalServerError",
      description: "a network error",
    },
    {
      clientId: "e400",
      elId: "badRequestError",
      description: "4xx",
      detail: "There seems to be a problem with the information you entered",
    },
    {
      clientId: "e500",
      elId: "internalServerError",
      description: "5xx",
    },
  ];

  errorScenarios.forEach((scenario) => {
    const { clientId, elId, description, detail } = scenario;
    describe(`when error is ${description}`, () => {
      beforeEach(() => {
        cy.visit(`/clients/details/${clientId}`);
      });
      const suffix = detail ? ` with detail "${detail}"` : "";
      it(`displays the error message "Something went wrong"${suffix}`, () => {
        cy.get(`#${elId}`).should("be.visible");
        if (detail) {
          cy.get(`#${elId}`).contains(detail);
        }
      });
    });
  });
});
