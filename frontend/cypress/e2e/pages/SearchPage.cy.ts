import type { ClientSearchResult } from "@/dto/CommonTypesDto";

describe("Search Page", () => {
  let predictiveSearchCount = 0;

  const checkDisplayedResults = (clientList: ClientSearchResult[]) => {
    clientList.forEach((client) => {
      cy.get("#search-box")
        .find(`cds-combo-box-item[data-value^="${client.clientNumber}"]`)
        .should("exist");
    });
  };
  beforeEach(() => {
    cy.intercept("https://green-domain.com/**", {
      body: `<html>
        <body>
          <h1>Green interface stub</h1>
        </body>
      </html>`,
    });

    // reset counter
    predictiveSearchCount = 0;

    cy.intercept("/api/clients/predictive-search?keyword=*", (req) => {
      predictiveSearchCount++;
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
  });

  describe("when user fills in the search box with a valid value", () => {
    beforeEach(() => {
      cy.fillFormEntry("#search-box", "car", { skipBlur: true });
    });

    it("makes the API call", () => {
      cy.wait("@predictiveSearch").then((interception) => {
        expect(interception.request.query.keyword).to.eq("car");
      });
      cy.wrap(predictiveSearchCount).should("eq", 1);
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

      it("makes another the API call", () => {
        cy.wait("@predictiveSearch").then((interception) => {
          expect(interception.request.query.keyword).to.eq("card");
        });
        cy.wrap(predictiveSearchCount).should("eq", 2);
      });

      it("updates the autocomplete results", () => {
        cy.wait("@predictiveSearch").then((interception) => {
          const data = interception.response.body;
          cy.log(interception.response.body);
          console.log(interception.response.body);

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
        cy.origin("https://green-domain.com", { args: { clientNumber } }, ({ clientNumber }) => {
          cy.url().should(
            "include",
            `/int/client/client02MaintenanceAction.do?bean.clientNumber=${clientNumber}`,
          );
        });
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
      cy.wrap(predictiveSearchCount).should("eq", 0);
    });
  });
});
