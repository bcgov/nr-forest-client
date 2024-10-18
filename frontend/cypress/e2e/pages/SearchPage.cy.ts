import type { ClientSearchResult } from "@/dto/CommonTypesDto";

describe("Search Page", () => {
  const predictiveSearchCounter = {
    count: 0,
  };

  const fullSearchCounter = {
    count: 0,
  };

  const checkAutocompleteResults = (clientList: ClientSearchResult[]) => {
    clientList.forEach((client, index) => {
      cy.get("#search-box")
        .find("cds-combo-box-item")
        .eq(index)
        .should("exist")
        .should("have.attr", "data-id", client.clientNumber);
    });
  };

  const checkTableResults = (clientList: ClientSearchResult[]) => {
    clientList.forEach((client, index) => {
      cy.get("cds-table")
        .find("cds-table-row")
        .eq(index)
        .contains(client.clientNumber)
        .should("be.visible");

      const acronymColumnIndex = 3;

      // only the first client has an acronym
      const expectedValue = index === 0 ? client.clientAcronym : "-";

      cy.get("cds-table")
        .find("cds-table-row")
        .eq(index)
        .find(`cds-table-cell:nth-child(${acronymColumnIndex})`)
        .contains(expectedValue)
        .should("be.visible");
    });
  };

  beforeEach(() => {
    // reset counters
    predictiveSearchCounter.count = 0;
    fullSearchCounter.count = 0;

    cy.intercept(
      {
        pathname: "/api/clients/search",
        query: {
          keyword: "*",
        },
      },
      (req) => {
        predictiveSearchCounter.count++;
        req.continue();
      },
    ).as("predictiveSearch");

    cy.intercept(
      {
        pathname: "/api/clients/search",
        query: {
          page: "*",
          size: "*",
        },
      },
      (req) => {
        fullSearchCounter.count++;
        req.continue();
      },
    ).as("fullSearch");

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
      cy.wait("@predictiveSearch").then((interception) => {
        const data = interception.response.body;

        cy.wrap(data).should("be.an", "array").and("have.length.greaterThan", 0);

        cy.get("#search-box")
          .find("cds-combo-box-item")
          .should("have.length", data.length)
          .should("be.visible");

        checkAutocompleteResults(data);
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

          cy.wrap(data).should("be.an", "array").and("have.length.greaterThan", 0);

          cy.get("#search-box")
            .find("cds-combo-box-item")
            .should("have.length", data.length)
            .should("be.visible");

          checkAutocompleteResults(data);
        });
      });
    });

    describe("and user clicks an Autocomplete result", () => {
      const clientNumber = "00054076";
      beforeEach(() => {
        cy.get("#search-box")
          .find("cds-combo-box-item")
          .should("have.length.greaterThan", 0)
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

    describe("and clicks the Search button", () => {
      beforeEach(() => {
        cy.wait("@predictiveSearch");
        cy.get("#search-button").click();
      });
      it("makes one API call with the entered keywords", () => {
        cy.wait("@fullSearch").then((interception) => {
          const { query } = interception.request;
          expect(query.keyword).to.eq("car");
          expect(query.page).to.eq("0");
        });

        cy.wait(100); // Waits additional time to make sure there's no duplicate API calls.
        cy.wrap(fullSearchCounter).its("count").should("eq", 1);
      });

      it("displays the results on the table", () => {
        cy.wait("@fullSearch").then((interception) => {
          const data = interception.response.body;

          cy.wrap(data).should("be.an", "array").and("have.length.greaterThan", 0);

          cy.get("cds-table")
            .find("cds-table-row")
            .should("have.length", data.length)
            .should("be.visible");

          checkTableResults(data);
        });
      });

      describe("and user clicks a result on the table", () => {
        const clientNumber = "00191086";
        beforeEach(() => {
          cy.get("cds-table").contains("cds-table-row", clientNumber).click();
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

      describe("and clicks the Next page button on the table footer", () => {
        beforeEach(() => {
          cy.wait("@fullSearch");
          cy.get('[tooltip-text="Next page"]').click();
        });
        it("makes an API call for the second page of results", () => {
          cy.wait("@fullSearch").then((interception) => {
            const { query } = interception.request;
            expect(query.keyword).to.eq("car");
            expect(query.page).to.eq("1");
          });
          cy.wrap(fullSearchCounter).its("count").should("eq", 2);
        });

        it("updates the results on the table", () => {
          cy.wait("@fullSearch").then((interception) => {
            const data = interception.response.body;

            cy.wrap(data).should("be.an", "array").and("have.length.greaterThan", 0);

            cy.get("cds-table")
              .find("cds-table-row")
              .should("have.length", data.length)
              .should("be.visible");

            checkTableResults(data);
          });
        });

        describe("and clicks the Search button again", () => {
          beforeEach(() => {
            cy.wait("@fullSearch");

            // sanity check
            cy.get("#pages-select").should("have.value", "2");

            cy.get("#search-button").click();
          });
          it("makes a new API call for the first page of results", () => {
            cy.wait("@fullSearch").then((interception) => {
              const { query } = interception.request;
              expect(query.keyword).to.eq("car");
              expect(query.page).to.eq("0");
            });
            cy.wrap(fullSearchCounter).its("count").should("eq", 3);
          });

          it("updates the results on the table", () => {
            cy.wait("@fullSearch").then((interception) => {
              // reset to page 1
              cy.get("#pages-select").should("have.value", "1");

              const data = interception.response.body;

              cy.wrap(data).should("be.an", "array").and("have.length.greaterThan", 0);

              cy.get("cds-table")
                .find("cds-table-row")
                .should("have.length", data.length)
                .should("be.visible");

              checkTableResults(data);
            });
          });
        });
      });
    });

    describe("and hits enter on the search box", () => {
      beforeEach(() => {
        cy.wait("@predictiveSearch");
        cy.fillFormEntry("#search-box", "{enter}", { skipBlur: true });
      });
      it("makes the API call with the entered keywords", () => {
        cy.wait("@fullSearch").then((interception) => {
          const { query } = interception.request;
          expect(query.keyword).to.eq("car");
          expect(query.page).to.eq("0");
        });
        cy.wrap(fullSearchCounter).its("count").should("eq", 1);
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

    describe("and clicks the Search button", () => {
      beforeEach(() => {
        cy.get("#search-button").click();
      });
      it("makes no API call", () => {
        cy.wait(500); // This time has to be greater than the debouncing time
        cy.wrap(fullSearchCounter).its("count").should("eq", 0);
      });
    });
  });

  describe("when user fills in the search box with less than 3 characters", () => {
    beforeEach(() => {
      cy.fillFormEntry("#search-box", "12", { skipBlur: true });
    });

    describe("and hits enter on the search box", () => {
      beforeEach(() => {
        cy.fillFormEntry("#search-box", "{enter}", { skipBlur: true });
      });
      it("shows error message and makes no API call", () => {
        cy.contains("The search terms must contain at least 3 characters");

        cy.wait(500); // This time has to be greater than the debouncing time
        cy.wrap(fullSearchCounter).its("count").should("eq", 0);
      });
    });
  });

  describe("Search with no keywords", () => {
    beforeEach(() => {
      cy.get("#search-button").click();
    });
    it("makes an API call even without any keywords", () => {
      cy.wait("@fullSearch").then((interception) => {
        const { query } = interception.request;
        expect(query.keyword).to.eq("");
        expect(query.page).to.eq("0");
      });
    });
  });

  describe("when the API is returning errors", () => {
    beforeEach(() => {
      // The "error" value actually triggers the error response
      cy.fillFormEntry("#search-box", "error");
    });
    describe("and user clicks the Search button", () => {
      beforeEach(() => {
        cy.get("#search-button").click();
      });

      it("displays an error notification", () => {
        cy.wait("@fullSearch");

        cy.get("cds-actionable-notification")
          .shadow()
          .contains("Something went wrong")
          .should("be.visible");
      });

      describe("and the API stops returning errors", () => {
        beforeEach(() => {
          cy.wait("@fullSearch");

          // Replacing the "error" value actually triggers a successful response
          cy.fillFormEntry("#search-box", "okay");
        });
        describe("and user clicks the Search button", () => {
          beforeEach(() => {
            cy.get("#search-button").click();
          });

          it("hides the error notification", () => {
            cy.wait("@fullSearch");

            cy.get("cds-actionable-notification").should("not.exist");
          });
        });
      });
    });
  });
});
