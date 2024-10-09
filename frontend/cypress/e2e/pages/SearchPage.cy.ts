import type { ClientSearchResult } from "@/dto/CommonTypesDto";

describe("Search Page", () => {
  const predictiveSearchCounter = {
    count: 0,
  };

  const checkDisplayedResults = (clientList: ClientSearchResult[]) => {
    clientList.forEach((client) => {
      cy.get("#search-box")
        .find(`cds-combo-box-item[data-value^="${client.clientNumber}"]`)
        .should("exist");
    });
  };
  beforeEach(() => {
    // reset counter
    predictiveSearchCounter.count = 0;

    cy.intercept("/api/clients/search?keyword=*", (req) => {
      predictiveSearchCounter.count++;
      req.continue();
    }).as("predictiveSearch");

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

    cy.window().then((win) => {
      cy.stub(win, "open").as("windowOpen");
    });
  });

  describe("when user fills in the search box with a valid value", () => {
    beforeEach(() => {
      cy.fillFormEntry("#search-box", "car", { skipBlur: true });
    });

    it("makes the API call with the entered keywords", () => {
      cy.wait("@predictiveSearch").then((interception) => {
        expect(interception.request.query.keyword).to.eq("car");
      });
      cy.wrap(predictiveSearchCounter).its("count").should("eq", 1);
    });

    it("displays autocomplete results", () => {
      cy.get("#search-box")
        .find("cds-combo-box-item")
        .should("have.length", 3)
        .should("be.visible");

      cy.wait("@predictiveSearch").then((interception) => {
        const data = interception.response.body;

        cy.wrap(data).should("be.an", "array").and("have.length", 3);

        cy.get("#search-box")
          .find("cds-combo-box-item")
          .should("have.length", data.length)
          .should("be.visible");

        checkDisplayedResults(data);
      });
    });

    describe("and types more characters", () => {
      beforeEach(() => {
        cy.wait("@predictiveSearch");
        cy.fillFormEntry("#search-box", "d", { skipBlur: true });
      });

      it("makes another the API call with the updated keywords", () => {
        cy.wait("@predictiveSearch").then((interception) => {
          expect(interception.request.query.keyword).to.eq("card");
        });
        cy.wrap(predictiveSearchCounter).its("count").should("eq", 2);
      });

      it("updates the autocomplete results", () => {
        cy.wait("@predictiveSearch").then((interception) => {
          const data = interception.response.body;

          cy.wrap(data).should("be.an", "array").and("have.length.greaterThan", 3);

          cy.get("#search-box")
            .find("cds-combo-box-item")
            .should("have.length", data.length)
            .should("be.visible");

          checkDisplayedResults(data);
        });
      });
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
        const greenDomain = "green-domain.com";
        cy.get("@windowOpen").should(
          "be.calledWith",
          `https://${greenDomain}/int/client/client02MaintenanceAction.do?bean.clientNumber=${clientNumber}`,
          "_blank",
          "noopener",
        );
      });
    });
  });

  describe("when user fills in the search box with an invalid value", () => {
    beforeEach(() => {
      cy.fillFormEntry("#search-box", "até", { skipBlur: true });
    });

    it("shows an error message", () => {
      cy.contains("The search terms can only contain: A-Z, a-z, 0-9, space or common symbols");
    });
    it("makes no API call", () => {
      cy.wait(500); // This time has to be greater than the debouncing time
      cy.wrap(predictiveSearchCounter).its("count").should("eq", 0);
    });
  });
});
