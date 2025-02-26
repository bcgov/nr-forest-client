describe("Client Details Page", () => {
  const greenDomain = Cypress.env("VITE_GREEN_DOMAIN");

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

  let currentRole: string;

  function init() {
    const testRole = getTestRole(this) || "CLIENT_VIEWER";

    cy.hasLoggedIn().then((hasLoggedIn) => {
      let hasLoggedOut = false;
      if (hasLoggedIn && testRole !== currentRole) {
        cy.clearAllCookies();
        hasLoggedOut = true;
      }
      if (!hasLoggedIn || hasLoggedOut) {
        cy.visit("/");

        cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
          given_name: "James",
          family_name: "Baxter",
          "cognito:groups": [testRole],
        });
        currentRole = testRole;
      }
    });
  }

  beforeEach(init);

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

  const testReadonly = (rawSelector: string, value?: string) => {
    const selector = `:is(div, span)${rawSelector}`;
    cy.get(selector).should("be.visible");
    if (value !== undefined) {
      cy.get(selector).contains(value);
      expect(value.length).to.be.greaterThan(0);
    }
  };

  const testHidden = (selector: string) => {
    cy.get(selector).should("not.exist");
  };

  describe("summary (role:CLIENT_EDITOR)", () => {
    describe("save", () => {
      describe("on success", { testIsolation: false }, () => {
        const getClientDetailsCounter = {
          count: 0,
        };

        before(function () {
          init.call(this);

          cy.intercept(
            {
              method: "GET",
              pathname: "/api/clients/details/*",
            },
            (req) => {
              getClientDetailsCounter.count++;
              req.continue();
            },
          ).as("getClientDetails");

          cy.visit("/clients/details/g");
          cy.get("#summaryEditBtn").click();
          cy.clearFormEntry("#input-workSafeBCNumber");
          cy.get("#summarySaveBtn").click();
        });

        it("shows the success toast", () => {
          cy.get("cds-toast-notification[kind='success']").should("be.visible");
        });

        it("reloads data", () => {
          // Called twice - one for the initial loading and one after saving.
          cy.wrap(getClientDetailsCounter).its("count").should("eq", 2);
        });

        it("gets back into view mode", () => {
          testHidden("#input-workSafeBCNumber");
          testHidden("#input-clientStatus");
          testHidden("[data-id='input-input-notes']");

          cy.get("#summarySaveBtn").should("not.exist");

          testReadonly("#workSafeBCNumber");
          testReadonly("#clientStatus");
          testReadonly("#notes");

          cy.get("#summaryEditBtn").should("be.visible");
        });
      });

      describe("on failure", { testIsolation: false }, () => {
        before(function () {
          init.call(this);

          cy.visit("/clients/details/g");
          cy.get("#summaryEditBtn").click();
          cy.fillFormEntry("[data-id='input-input-notes']", "error", { area: true });
          cy.get("#summarySaveBtn").click();
        });

        it("shows the error toast", () => {
          cy.get("cds-toast-notification[kind='error']").should("be.visible");
        });

        it("stays in edit mode", () => {
          cy.get("#input-workSafeBCNumber").should("be.visible");
          cy.get("#input-clientStatus").should("be.visible");
          cy.get("[data-id='input-input-notes']").should("be.visible");

          cy.get("#summarySaveBtn").should("be.visible");
        });
      });
    });
  });

  describe("locations tab", () => {
    describe("non-user action tests", { testIsolation: false }, () => {
      describe("3 active locations", () => {
        before(function () {
          init.call(this);
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
          cy.get("#location-00 [slot='title']").contains("00 - Mailing address");
          cy.get("#location-01 [slot='title']").contains("01 - Accountant address");
          cy.get("#location-02 [slot='title']").contains("02 - Warehouse");
        });

        it("displays the address on the accordion's title while it's collapsed", () => {
          cy.get("#location-00-title-address").should("be.visible");
          cy.get("#location-01-title-address").should("be.visible");
          cy.get("#location-02-title-address").should("be.visible");
        });
      });

      describe("2 locations - 1 active and 1 deactivated", () => {
        before(function () {
          init.call(this);
          cy.visit("/clients/details/gd");
        });
        it("doesn't display the tag Deactivated when location is not expired", () => {
          cy.get("cds-tag#location-00-deactivated").should("not.exist");
        });

        it("displays the tag Deactivated when location is expired", () => {
          cy.get("cds-tag#location-01-deactivated").contains("Deactivated");
        });
      });

      describe("location without name", () => {
        before(function () {
          init.call(this);
          cy.visit("/clients/details/se");
        });
        it("displays only the location code, without the dash", () => {
          cy.get("#location-00 [slot='title']").contains("00");
          cy.get("#location-00 [slot='title']").contains("-").should("not.exist");
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

        // Switch to another tab (Contacts)
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

    describe("when role:CLIENT_EDITOR", () => {
      describe("name duplication", () => {
        beforeEach(() => {
          cy.visit("/clients/details/g");

          // Clicks to expand the accordion
          cy.get("#location-00 [slot='title']").click();

          cy.get("#location-00-EditBtn").click();

          cy.clearFormEntry("#name_0");

          // This is the same name of the third location
          cy.fillFormEntry("#name_0", "Warehouse");
        });

        it("shows the error on field Location name", () => {
          cy.checkInputErrorMessage("#name_0", "This value is already in use");

          cy.get("#location-00-SaveBtn").shadow().find("button").should("be.disabled");
        });
      });
      describe("save", () => {
        describe("on success", { testIsolation: false }, () => {
          const getClientDetailsCounter = {
            count: 0,
          };

          let patchClientDetailsRequest;
          before(function () {
            init.call(this);

            cy.intercept(
              {
                method: "GET",
                pathname: "/api/clients/details/*",
              },
              (req) => {
                getClientDetailsCounter.count++;
                req.continue();
              },
            ).as("getClientDetails");

            cy.intercept(
              {
                method: "PATCH",
                pathname: "/api/clients/details/*",
              },
              (req) => {
                patchClientDetailsRequest = req;
                req.continue();
              },
            ).as("patchClientDetails");

            cy.visit("/clients/details/g");

            // Clicks to expand the accordion
            cy.get("#location-00 [slot='title']").click();

            cy.get("#location-00-EditBtn").click();
            cy.clearFormEntry("#emailAddress_0");
            cy.get("#location-00-SaveBtn").click();
          });

          it("prefixes the path with the corresponding location code", () => {
            expect(patchClientDetailsRequest.body[0].path).to.eq("/addresses/00/emailAddress");
          });

          it("shows the success toast", () => {
            cy.get("cds-toast-notification[kind='success']").should("be.visible");
          });

          it("reloads data", () => {
            // Called twice - one for the initial loading and one after saving.
            cy.wrap(getClientDetailsCounter).its("count").should("eq", 2);
          });

          it("gets back into view mode", () => {
            // Fields that belong to the form (edit mode)
            testHidden("#city_0");
            testHidden("#emailAddress_0");
            testHidden("[data-id='input-notes_0']");

            cy.get("#location-00-SaveBtn").should("not.exist");

            testReadonly("#location-00-city-province");
            testReadonly("#location-00-emailAddress");
            testReadonly("#location-00-notes");

            cy.get("#location-00-EditBtn").should("be.visible");
          });
        });

        describe("on failure", { testIsolation: false }, () => {
          before(function () {
            init.call(this);

            cy.visit("/clients/details/g");

            // Clicks to expand the accordion
            cy.get("#location-00 [slot='title']").click();

            cy.get("#location-00-EditBtn").click();
            cy.fillFormEntry("[data-id='input-notes_0']", "error", { area: true });
            cy.get("#location-00-SaveBtn").click();
          });

          it("shows the error toast", () => {
            cy.get("cds-toast-notification[kind='error']").should("be.visible");
          });

          it("stays in edit mode", () => {
            cy.get("#city_0").should("be.visible");
            cy.get("#emailAddress_0").should("be.visible");
            cy.get("[data-id='input-notes_0']").should("be.visible");

            cy.get("#location-00-SaveBtn").should("be.visible");
          });
        });
      });
    });
  });

  describe("contacts tab", () => {
    describe("non-user action tests", { testIsolation: false }, () => {
      before(function () {
        init.call(this);
        cy.visit("/clients/details/g");

        // Switch to the Contacts tab
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

      it("displays the contacts names on the accordions' titles sorted by contact name", () => {
        cy.get("#contact-0 [slot='title']").contains("Cheryl Bibby");
        cy.get("#contact-1 [slot='title']").contains("Christoffer Stewart");
        cy.get("#contact-2 [slot='title']").contains("Edward Burns");
      });

      it("displays the associated locations on the accordion's title while it's collapsed", () => {
        cy.get("#contact-0-title-locations").should("be.visible");
        cy.get("#contact-1-title-locations").should("be.visible");
        cy.get("#contact-2-title-locations").should("be.visible");
      });
    });

    describe("regular, isolated tests", () => {
      describe("3 contacts", () => {
        beforeEach(() => {
          cy.visit("/clients/details/g");

          // Switch to the Contacts tab
          cy.get("#tab-contacts").click();

          // Make sure the current tab panel was effectively switched
          cy.get("#panel-locations").should("have.attr", "hidden");
          cy.get("#panel-contacts").should("not.have.attr", "hidden");
        });

        it("hides the associated locations on the accordion's title when it's expanded", () => {
          // Clicks to expand the accordion
          cy.get("#contact-0 [slot='title']").click();
          cy.get("#contact-0-title-locations").should("not.be.visible");
        });

        it("keeps accordions' states while tabs are switched", () => {
          // Expand first and third contacts, leave second one collapsed
          cy.get("#contact-0 [slot='title']").click();
          cy.get("#contact-2 [slot='title']").click();

          // Switch to another tab (Locations)
          cy.get("#tab-locations").click();

          // Make sure the current tab panel was effectively switched
          cy.get("#panel-contacts").should("have.attr", "hidden");
          cy.get("#panel-locations").should("not.have.attr", "hidden");

          // Switch back to tab Contacts
          cy.get("#tab-contacts").click();

          // First contact is still open
          cy.get("#contact-0 cds-accordion-item").should("have.attr", "open");

          // Second contact is still closed
          cy.get("#contact-1 cds-accordion-item").should("not.have.attr", "open");

          // Third contact is still open
          cy.get("#contact-2 cds-accordion-item").should("have.attr", "open");
        });
      });
      describe("no contacts", () => {
        beforeEach(() => {
          cy.visit("/clients/details/gd");

          // Switch to the Contacts tab
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

      describe("1 contact, location without name", () => {
        beforeEach(() => {
          cy.visit("/clients/details/se");

          // Switch to the Contacts tab
          cy.get("#tab-contacts").click();

          // Make sure the current tab panel was effectively switched
          cy.get("#panel-locations").should("have.attr", "hidden");
          cy.get("#panel-contacts").should("not.have.attr", "hidden");
        });

        it("displays the location code in the contact subtitle, without a dash", () => {
          cy.get("#contact-0-title-locations").contains("00");
          cy.get("#contact-0-title-locations").contains("-").should("not.exist");
        });

        /*
        Note: this test would not be possible on the ContactView component, since the calculation
        is performed outside that component.
        */
        it("displays the location code in the contact's Associated locations, without a dash", () => {
          // expands the accordion
          cy.get("#contact-0 cds-accordion-item").click();

          cy.get("#contact-0-associatedLocations").contains("00").should("be.visible");
          cy.get("#contact-0-associatedLocations").contains("-").should("not.exist");
        });
      });
    });
  });

  describe("related clients tab", () => {
    const clientNumber = "12321";
    beforeEach(() => {
      cy.visit(`/clients/details/${clientNumber}`);

      cy.window().then((win) => {
        cy.stub(win, "open").as("windowOpen");
      });

      // Switch to the Related Clients tab
      cy.get("#tab-related").click();

      // Make sure the current tab panel was effectively switched
      cy.get("#panel-locations").should("have.attr", "hidden");
      cy.get("#panel-related").should("not.have.attr", "hidden");
    });

    it("should display the Under construction message", () => {
      cy.get("#panel-related").contains("Under construction").should("be.visible");
    });

    it("should open the Related Client page in the legacy application", () => {
      cy.get("#open-related-clients-btn").click();
      cy.get("@windowOpen").should(
        "be.calledWith",
        `https://${greenDomain}/int/client/client04RelatedClientListAction.do?bean.clientNumber=${clientNumber}`,
        "_blank",
        "noopener",
      );
    });
  });

  describe("activity log tab", () => {
    const clientNumber = "12321";
    beforeEach(() => {
      cy.visit(`/clients/details/${clientNumber}`);

      cy.window().then((win) => {
        cy.stub(win, "open").as("windowOpen");
      });

      // Switch to the Activity log tab
      cy.get("#tab-activity").click();

      // Make sure the current tab panel was effectively switched
      cy.get("#panel-locations").should("have.attr", "hidden");
      cy.get("#panel-activity").should("not.have.attr", "hidden");
    });

    it("should display the Under construction message", () => {
      cy.get("#panel-activity").contains("Under construction").should("be.visible");
    });

    it("should open the Maintenance page in the legacy application", () => {
      cy.get("#open-maintenance-btn").click();
      cy.get("@windowOpen").should(
        "be.calledWith",
        `https://${greenDomain}/int/client/client02MaintenanceAction.do?bean.clientNumber=${clientNumber}`,
        "_blank",
        "noopener",
      );
    });
  });
});
