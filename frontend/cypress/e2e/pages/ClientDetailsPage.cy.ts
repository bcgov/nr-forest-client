describe("Client Details Page", () => {
  const getTestRole = (ctx: Mocha.Context) => {
    const titlePath = ctx.currentTest.titlePath();
    for (const title of titlePath.reverse()) {
      if (!title.includes("role:")) continue;

      const suffix = title.split("role:")[1];
      const words = suffix.replace(/[^\w\s]/g, "").split(/\s+/);
      const role = words[0];

      return role;
    }
    return undefined;
  };

  beforeEach(function () {
    cy.location().then((location) => {
      if (location.pathname === "blank") {
        cy.visit("/");

        const role = getTestRole(this) || "CLIENT_VIEWER";
        cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
          given_name: "James",
          family_name: "Baxter",
          "cognito:groups": [role],
        });
      }
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

  describe("locations tab", () => {
    describe("non-user action tests", { testIsolation: false }, () => {
      describe("3 active locations", () => {
        before(() => {
          cy.visit("/clients/details/g");
        });

        it("displays the number of locations", () => {
          cy.get("#panel-locations").contains("03 locations");
        });

        it("displays one collapsed accordion component for each location", () => {
          cy.get("#panel-locations").within(() => {
            // There are 3 accordions
            cy.get("cds-accordion").should("have.length", 3);

            // All accordions are initially collapsed
            cy.get("cds-accordion cds-accordion-item").each(($el) => {
              expect($el).not.to.have.attr("open");
            });
          });
        });

        it("displays the location name on the accordion's title", () => {
          cy.get("#location-00 [slot='title']").contains("Mailing address");
          cy.get("#location-01 [slot='title']").contains("Accountant's address");
          cy.get("#location-02 [slot='title']").contains("Warehouse");
        });

        it("displays the address on the accordion's title while it's collapsed", () => {
          cy.get("#location-00-title-address").should("be.visible");
          cy.get("#location-01-title-address").should("be.visible");
          cy.get("#location-02-title-address").should("be.visible");
        });
      });

      describe("2 locations - 1 deactivated and 1 active", () => {
        before(() => {
          cy.visit("/clients/details/gd");
        });
        it("displays the tag Deactivated when location is expired", () => {
          cy.get("cds-tag#location-00-deactivated").contains("Deactivated");
        });

        it("doesn't display the tag Deactivated when location is not expired", () => {
          cy.get("cds-tag#location-01-deactivated").should("not.exist");
        });
      });
    });

    describe("regular, isolated tests", () => {
      beforeEach(() => {
        cy.visit("/clients/details/g");
      });

      it("hides the address on the accordion's title when it's expanded", () => {
        cy.visit("/clients/details/g");

        // Clicks to expand the accordion
        cy.get("#location-00 [slot='title']").click();

        cy.get("#location-00-title-address").should("not.be.visible");
      });

      it("keeps accordions' states while tabs are switched", () => {
        cy.visit("/clients/details/g");

        // Expand first and third locations, leave second one collapsed
        cy.get("#location-00 [slot='title']").click();
        cy.get("#location-02 [slot='title']").click();

        // Switch to tab another tab (Contacts)
        cy.get("#tab-contacts").click();

        // Make sure the current tab panel was effectively switched
        cy.get("#panel-locations").should("have.attr", "hidden");
        cy.get("#panel-contacts").should("not.have.attr", "hidden");

        // Switch back to tab Locations
        cy.get("#tab-locations").click();

        // First location is still open
        cy.get("#location-00 cds-accordion-item").should("have.attr", "open");

        // Second location is still closed
        cy.get("#location-01 cds-accordion-item").should("not.have.attr", "open");

        // Third location is still open
        cy.get("#location-02 cds-accordion-item").should("have.attr", "open");
      });
    });
  });

  describe("contacts tab", () => {
    describe("non-user action tests", { testIsolation: false }, () => {
      before(() => {
        cy.visit("/clients/details/g");

        // Switch to tab another tab (Contacts)
        cy.get("#tab-contacts").click();

        // Make sure the current tab panel was effectively switched
        cy.get("#panel-locations").should("have.attr", "hidden");
        cy.get("#panel-contacts").should("not.have.attr", "hidden");
      });

      it("displays the number of contacts", () => {
        cy.get("#panel-contacts").contains("03 contacts");
      });

      it("displays one collapsed accordion component for each contact", () => {
        cy.get("#panel-contacts").within(() => {
          // There are 3 accordions
          cy.get("cds-accordion").should("have.length", 3);

          // All accordions are initially collapsed
          cy.get("cds-accordion cds-accordion-item").each(($el) => {
            expect($el).not.to.have.attr("open");
          });
        });
      });

      it("displays the contact name on the accordion's title", () => {
        cy.get("#contact-00 [slot='title']").contains("Cheryl Bibby");
        cy.get("#contact-01 [slot='title']").contains("Edward Burns");
        cy.get("#contact-02 [slot='title']").contains("Christoffer Stewart");
      });

      it("displays the associated locations on the accordion's title while it's collapsed", () => {
        cy.get("#contact-00-title-locations").should("be.visible");
        cy.get("#contact-01-title-locations").should("be.visible");
        cy.get("#contact-02-title-locations").should("be.visible");
      });
    });

    describe("regular, isolated tests", () => {
      describe("3 contacts", () => {
        beforeEach(() => {
          cy.visit("/clients/details/g");

          // Switch to tab another tab (Contacts)
          cy.get("#tab-contacts").click();

          // Make sure the current tab panel was effectively switched
          cy.get("#panel-locations").should("have.attr", "hidden");
          cy.get("#panel-contacts").should("not.have.attr", "hidden");
        });

        it("hides the associated locations on the accordion's title when it's expanded", () => {
          // Clicks to expand the accordion
          cy.get("#contact-00 [slot='title']").click();
          cy.get("#contact-00-title-locations").should("not.be.visible");
        });

        it("keeps accordions' states while tabs are switched", () => {
          // Expand first and third contacts, leave second one collapsed
          cy.get("#contact-00 [slot='title']").click();
          cy.get("#contact-02 [slot='title']").click();
          // Switch to tab another tab (Locations)
          cy.get("#tab-locations").click();
          // Make sure the current tab panel was effectively switched
          cy.get("#panel-contacts").should("have.attr", "hidden");
          cy.get("#panel-locations").should("not.have.attr", "hidden");
          // Switch back to tab Contacts
          cy.get("#tab-contacts").click();
          // First contact is still open
          cy.get("#contact-00 cds-accordion-item").should("have.attr", "open");
          // Second contact is still closed
          cy.get("#contact-01 cds-accordion-item").should("not.have.attr", "open");
          // Third contact is still open
          cy.get("#contact-02 cds-accordion-item").should("have.attr", "open");
        });
      });
      describe("no contacts", () => {
        beforeEach(() => {
          cy.visit("/clients/details/gd");

          // Switch to tab another tab (Contacts)
          cy.get("#tab-contacts").click();

          // Make sure the current tab panel was effectively switched
          cy.get("#panel-locations").should("have.attr", "hidden");
          cy.get("#panel-contacts").should("not.have.attr", "hidden");
        });

        it("displays the empty view", () => {
          cy.contains("#panel-contacts", "Nothing to show yet!");
        });

        const addContactText = "Click “Add contact” button to start";
        const alternateText = "No contacts have been added to this client account";

        describe("when role:CLIENT_VIEWER", () => {
          it("doesn't display instruction for adding contacts", () => {
            cy.contains("#panel-contacts", addContactText).should("not.exist");

            // displays the alternate text
            cy.contains("#panel-contacts", alternateText);
          });
        });

        describe("when user has authority", () => {
          ["CLIENT_EDITOR", "CLIENT_SUSPEND", "CLIENT_ADMIN"].forEach((role) => {
            describe(`role:${role}`, () => {
              it("displays instruction for adding contacts", () => {
                cy.contains("#panel-contacts", addContactText);

                // doesn't display the alternate text
                cy.contains("#panel-contacts", alternateText).should("not.exist");
              });
            });
          });
        });
      });
    });
  });
});
