import type { CyHttpMessages } from "cypress/types/net-stubbing";

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

  let resumePatch: () => void;

  /**
   * This structure allows to both interrupt and resume the Patch request.
   *
   * @returns function to resume the Patch request
   */
  const interruptPatch = (requestCallback?: (req: CyHttpMessages.IncomingHttpRequest) => void) => {
    let resolvePatchIntercepted: () => void;
    const promisePatchIntercepted = new Promise<void>((resolve) => {
      resolvePatchIntercepted = resolve;
    });

    let resumePatch: () => void;

    const createUnresolvedPromise = () =>
      new Promise<void>((resolve) => {
        resumePatch = resolve;
        resolvePatchIntercepted();
      });

    /**
     * Resolves the promise once the Patch has been intercepted.
     * i.e. Waits in case the Patch has not been intercepted yet, and resolves it after that.
     *
     * This is very handy because it's not trivial to know if the request was already intercepted,
     * since you can't use cy.wait, since the request would be in an interrupted state.
     *
     * Of course there are cases where this is not needed - the moment you would want to resume it,
     * you know there would be enough time for the request to be intercepted...
     * This was created to other cases, where that wouldn't work.
     *
     * As a last note. We could also just don't care and wait for it to auto-resolve after some
     * time - which seems to be 2 seconds - but then we would have this time penalty in our tests.
     */
    const resumePatchAsap = () =>
      promisePatchIntercepted.then(() => {
        resumePatch();
      });

    cy.intercept("PATCH", "/api/clients/details/*", (req) => {
      req.continue(createUnresolvedPromise);
      requestCallback?.(req);
    }).as("saveClientDetails");

    return resumePatchAsap;
  };

  it("shows text skeletons only while data is not available", () => {
    let resolveGetClientDetails: () => void;

    const promiseGetClientDetails = new Promise<void>((resolve) => {
      resolveGetClientDetails = resolve;
    });

    cy.intercept(
      {
        method: "GET",
        pathname: "/api/clients/details/*",
      },
      (req) => {
        req.continue(() => promiseGetClientDetails);
      },
    ).as("getClientDetails");

    cy.visit("/clients/details/0");

    cy.get(".heading-03-skeleton").should("be.visible");
    cy.get("h1.resource-details--title").should("not.exist");

    cy.get(".label-skeleton").should("be.visible");
    cy.get(".value-skeleton").should("be.visible");
    cy.get("#clientNumber").should("not.exist");

    cy.get("#panel-locations .heading-05-skeleton").should("be.visible");
    cy.get("#panel-locations h3").should("not.exist");

    cy.get("#panel-contacts .heading-05-skeleton").should("exist"); // exists but it's not visible - second tab
    cy.get("#panel-contacts h3").should("not.exist");

    resolveGetClientDetails();
    cy.wait("@getClientDetails");

    // Now data is already available, so text skeletons should not exist anymore
    cy.get("cds-skeleton-text").should("not.exist");

    cy.get(".heading-03-skeleton").should("not.exist");
    cy.get("h1.resource-details--title").should("be.visible");

    cy.get(".label-skeleton").should("not.exist");
    cy.get(".value-skeleton").should("not.exist");
    cy.get("#clientNumber").should("be.visible");

    cy.get("#panel-locations").scrollIntoView();
    cy.get("#panel-locations .heading-05-skeleton").should("not.exist");
    cy.get("#panel-locations h3").should("be.visible");

    cy.get("#panel-contacts .heading-05-skeleton").should("not.exist");
    cy.get("#panel-contacts h3").should("exist"); // exists but it's not visible - second tab
  });

  it("renders the page structure", () => {
    cy.visit("/clients/details/0");

    cy.get("cds-breadcrumb").should("contain", "Client search");
    cy.contains("h2", "Client summary");
    cy.contains("cds-tab", "Client locations");
    cy.contains("cds-tab", "Client contacts");
    cy.contains("cds-tab", "Related clients");
    cy.contains("cds-tab", "History");
  });

  const nameScenarios = [
    {
      id: "g",
      type: "corporation",
      expected: "Kovacek, Thompson And Boyer",
      expectedDescription: "Client name",
    },
    {
      id: "i",
      type: "individual without middle name",
      expected: "John Silver",
      expectedDescription: "First name + Last name",
    },
    {
      id: "s",
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

        // There should be no skeletons on screen after the error
        cy.get("cds-skeleton-text").should("not.exist");
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

        const registerInterceptors = () => {
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
        };

        before(function () {
          init.call(this);

          registerInterceptors();

          cy.visit("/clients/details/p");
          cy.get("#summaryEditBtn").click();
          cy.clearFormEntry("#input-workSafeBCNumber");
          resumePatch = interruptPatch();
          cy.get("#summarySaveBtn").click();
        });

        beforeEach(() => {
          registerInterceptors();
        });

        it("disables the Save button while waiting for the response", () => {
          cy.get("#summarySaveBtn").shadow().find("button").should("be.disabled");
          resumePatch();
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

      describe("on generic failure", { testIsolation: false }, () => {
        before(function () {
          init.call(this);

          cy.visit("/clients/details/p");
          cy.get("#summaryEditBtn").click();
          cy.fillFormEntry("[data-id='input-input-notes']", "error", { area: true });
          resumePatch = interruptPatch();
          cy.get("#summarySaveBtn").click();
        });

        it("disables the Save button while waiting for the response", () => {
          cy.get("#summarySaveBtn").shadow().find("button").should("be.disabled");
          resumePatch();
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

        it("re-enables the Save button", () => {
          cy.get("#summarySaveBtn").shadow().find("button").should("be.enabled");
        });
      });

      describe("on duplicated acronym failure ", { testIsolation: false }, () => {
        before(function () {
          init.call(this);

          cy.visit("/clients/details/p");
          cy.get("#summaryEditBtn").click();
          cy.clearFormEntry("#input-acronym");
          cy.fillFormEntry("#input-acronym", "ERR");
          cy.get("#summarySaveBtn").click();
        });

        it("shows the error toast", () => {
          cy.get("cds-toast-notification[kind='error']").should("be.visible");
        });

        it("shows a custom error message on the Acronym input field", () => {
          cy.get("#input-acronym")
            .find("[slot='invalid-text']")
            .contains("Looks like this acronym belongs to client 00000001. Try another acronym");

          cy.get("#input-acronym").find("[slot='invalid-text'] a").should("have.attr", "href");

          cy.get("#input-acronym").find("[slot='invalid-text'] a").should("have.text", "00000001");
        });
      });
    });
  });

  describe("summary (role:CLIENT_EDITOR)", () => {
    describe("save", () => {
      describe("with reason modal", { testIsolation: false }, () => {
        const registerInterceptors = () => {
          cy.intercept("GET", "/api/codes/update-reasons/*/*").as("getReasonsList");
        };

        before(function () {
          init.call(this);

          registerInterceptors();

          cy.visit("/clients/details/p");

          cy.get("#summaryEditBtn")
            .click();
  
          cy.get("#input-clientStatus")
            .find('[part="trigger-button"]')
            .click();

          cy.get("#input-clientStatus")
            .find('cds-dropdown-item[data-id="DAC"]')
            .should("be.visible")
            .click()
            .and("have.value", "Deactivated");

          cy.get("#summarySaveBtn")
            .click();

          cy.wait("@getReasonsList");
        });

        beforeEach(() => {
          registerInterceptors();
        });

        it("renders the reason modal properly", () => {
          cy.get("#reason-modal")
            .should("be.visible");
        
          cy.get("#input-reason-0")
            .should("exist");

          cy.get("#input-reason-0")
            .find('[part="trigger-button"]')
            .click();

          /*
          Make sure the amount of options would get the dropdown list to its maximum size.
          i.e. a size that requires a scroll bar in it.
          */
          cy.get("#input-reason-0").find("cds-dropdown-item").should("have.length.above", 7);

          // Double-checking to make sure the dropdown list has a scrollbar
          cy.get("#input-reason-0")
            .shadow()
            .find("#menu-body")
            .then(($el) => {
              expect($el.prop("scrollHeight")).greaterThan(0);
              expect($el.prop("clientHeight")).greaterThan(0);
              expect($el.prop("scrollHeight")).greaterThan($el.prop("clientHeight"));
            });

          /*
          And now checking the modal body has no scrollbar.
          Otherwise the dropdown list would be covered by the modal footer and users have to use
          two scrollbars in order to view all the options, and it fails if the user drags the
          scrollbar instead of using the mouse-wheel, because then the dropdown gets closed.
          */
          cy.get("#reason-modal")
            .find("cds-modal-body")
            .then(($el) => {
              expect($el.prop("scrollHeight")).greaterThan(0);
              expect($el.prop("clientHeight")).greaterThan(0);
              expect($el.prop("scrollHeight")).eq($el.prop("clientHeight"));
            });
        });

        it("keeps the Summary Save button enabled in case the user hits the Cancel button", () => {
          cy.get("#reasonCancelBtn").click();

          // Should be still enabled
          cy.get("#summarySaveBtn").shadow().find("button").should("be.enabled");
        });

        it("sends the correct PATCH request with reasons", () => {
          cy.get("#summarySaveBtn").click();

          cy.wait("@getReasonsList");

          cy.get("#input-reason-0").find('[part="trigger-button"]').click();

          cy.get("#input-reason-0")
            .find("cds-dropdown-item")
            .first()
            .should("be.visible")
            .click();

          resumePatch = interruptPatch();

          cy.get("#reasonSaveBtn").click();

          // Should disable the dialog button to prevent multiple clicks
          cy.get("#reasonSaveBtn").shadow().find("button").should("be.disabled");

          // Should disable the Summary button prevent multiple clicks
          cy.get("#summarySaveBtn").shadow().find("button").should("be.disabled");

          resumePatch();
        
          cy.wait("@saveClientDetails").then((interception) => {
            const requestBody = interception.request.body;

            cy.log("Request Body:", JSON.stringify(requestBody));
        
            expect(requestBody).to.deep.include({
              op: "add",
              path: "/reasons/0",
              value: {
                field: "/client/clientStatusCode",
                reason: "R1",
              },
            });
          });
        });        

      });

    });
  });

  describe("summary (role:CLIENT_ADMIN)", () => {
    describe("save client type Individual", () => {
      describe("name change", { testIsolation: false }, () => {
        beforeEach(function () {
          init.call(this);

          cy.intercept("GET", "/api/codes/update-reasons/*/*").as("getReasonsList");

          cy.visit("/clients/details/i");

          cy.get("#summaryEditBtn").click();

          cy.clearFormEntry("#input-legalFirstName");
          cy.fillFormEntry("#input-legalFirstName", "Jack");

          cy.clearFormEntry("#input-legalMiddleName");
          cy.fillFormEntry("#input-legalMiddleName", "Johnson");

          cy.get("#summarySaveBtn").click();

          cy.wait("@getReasonsList").then(({ request }) => {
            // requests the list of options related to Name change
            expect(request.url.endsWith("/NAME")).to.eq(true);
          });
        });

        it("opens the reason modal and sends the correct PATCH request with reasons", () => {
          cy.get("#reason-modal").should("be.visible");

          cy.get("#input-reason-0").should("exist");

          cy.get("#input-reason-0").find('[part="trigger-button"]').click();

          cy.get("#input-reason-0").find("cds-dropdown-item").first().should("be.visible").click();

          resumePatch = interruptPatch();

          cy.get("#reasonSaveBtn").click();

          // Should disable the dialog button to prevent multiple clicks
          cy.get("#reasonSaveBtn").shadow().find("button").should("be.disabled");

          // Should disable the Summary button prevent multiple clicks
          cy.get("#summarySaveBtn").shadow().find("button").should("be.disabled");

          resumePatch();

          cy.wait("@saveClientDetails").then((interception) => {
            const requestBody = interception.request.body;

            cy.log("Request Body:", JSON.stringify(requestBody));

            expect(requestBody).to.deep.include({
              op: "add",
              path: "/reasons/0",
              value: {
                field: "/client/name",
                reason: "R1",
              },
            });
          });
        });
      });

      describe("ID change", { testIsolation: false }, () => {
        beforeEach(function () {
          init.call(this);

          cy.intercept("GET", "/api/codes/update-reasons/*/*").as("getReasonsList");

          cy.visit("/clients/details/i");

          cy.get("#summaryEditBtn").click();

          cy.selectFormEntry("#input-clientIdType", "Alaska Drivers Licence");

          cy.clearFormEntry("#input-clientIdentification");
          cy.fillFormEntry("#input-clientIdentification", "ABC12345");

          cy.get("#summarySaveBtn").click();

          cy.wait("@getReasonsList").then(({ request }) => {
            // requests the list of options related to Name change
            expect(request.url.endsWith("/ID")).to.eq(true);
          });
        });

        it("opens the reason modal and sends the correct PATCH request with reasons", () => {
          cy.get("#reason-modal").should("be.visible");

          cy.get("#input-reason-0").should("exist");

          cy.get("#input-reason-0").find('[part="trigger-button"]').click();

          cy.get("#input-reason-0").find("cds-dropdown-item").first().should("be.visible").click();

          resumePatch = interruptPatch();

          cy.get("#reasonSaveBtn").click();

          // Should disable the dialog button to prevent multiple clicks
          cy.get("#reasonSaveBtn").shadow().find("button").should("be.disabled");

          // Should disable the Summary button prevent multiple clicks
          cy.get("#summarySaveBtn").shadow().find("button").should("be.disabled");

          resumePatch();

          cy.wait("@saveClientDetails").then((interception) => {
            const requestBody = interception.request.body;

            cy.log("Request Body:", JSON.stringify(requestBody));

            expect(requestBody).to.deep.include({
              op: "add",
              path: "/reasons/0",
              value: {
                field: "/client/id",
                reason: "R1",
              },
            });
          });
        });
      });
    });

    describe("change client type from Individual to Corporation", { testIsolation: false }, () => {
      before(function () {
        init.call(this);

        cy.visit("/clients/details/i");

        cy.get("#summaryEditBtn").click();

        cy.selectFormEntry("#input-clientType", "Corporation");
      });

      beforeEach(() => {
        cy.intercept("GET", "/api/codes/update-reasons/*/*").as("getReasonsList");
      });

      it("asks both reasons for NAME and ID change", () => {
        cy.get("#summarySaveBtn").click();

        cy.wait("@getReasonsList").then(({ request }) => {
          // requests the list of options related to Name change
          expect(request.url.endsWith("/ID")).to.eq(true);
        });

        cy.wait("@getReasonsList").then(({ request }) => {
          // requests the list of options related to Name change
          expect(request.url.endsWith("/NAME")).to.eq(true);
        });

        cy.get("#input-reason-0").find('[part="trigger-button"]').click();
        cy.get("#input-reason-0").find("cds-dropdown-item").first().should("be.visible").click();

        cy.get("#input-reason-1").find('[part="trigger-button"]').click();
        cy.get("#input-reason-1").find("cds-dropdown-item").first().should("be.visible").click();
      });

      it("removes the values from Individual related fields", () => {
        resumePatch = interruptPatch();

        cy.get("#reasonSaveBtn").click();

        // Should disable the dialog button to prevent multiple clicks
        cy.get("#reasonSaveBtn").shadow().find("button").should("be.disabled");

        // Should disable the Summary button prevent multiple clicks
        cy.get("#summarySaveBtn").shadow().find("button").should("be.disabled");

        resumePatch();

        cy.wait("@saveClientDetails").then((interception) => {
          const requestBody = interception.request.body;

          expect(requestBody).to.be.an("array");

          expect(requestBody).to.deep.include({
            op: "replace",
            path: "/client/legalFirstName",
            value: null,
          });

          expect(requestBody).to.deep.include({
            op: "replace",
            path: "/client/legalMiddleName",
            value: null,
          });

          expect(requestBody).to.deep.include({
            op: "replace",
            path: "/client/birthdate",
            value: null,
          });

          expect(requestBody).to.deep.include({
            op: "replace",
            path: "/client/clientIdTypeCode",
            value: null,
          });

          expect(requestBody).to.deep.include({
            op: "replace",
            path: "/client/clientIdentification",
            value: null,
          });
        });
      });
    });

    describe("on duplicated registration number failure ", { testIsolation: false }, () => {
      before(function () {
        init.call(this);

        cy.visit("/clients/details/p");
        cy.get("#summaryEditBtn").click();
        cy.clearFormEntry("#input-registryNumber");
        cy.fillFormEntry("#input-registryNumber", "9090");
        cy.get("#summarySaveBtn").click();
      });

      it("shows the error toast", () => {
        cy.get("cds-toast-notification[kind='error']").should("be.visible");
      });

      it("shows a custom error message on the Registration number input group", () => {
        cy.get("#registration-number .field-error").contains(
          "Looks like this registration number belongs to client 00000001. Try another registration number",
        );

        cy.get("#registration-number .field-error").find("a").should("have.attr", "href");

        cy.get("#registration-number .field-error").find("a").should("have.text", "00000001");
      });
    });
  });

  describe("locations tab", () => {
    describe("non-user action tests", { testIsolation: false }, () => {
      describe("3 active locations", () => {
        before(function () {
          init.call(this);
          cy.visit("/clients/details/p");
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

        it("includes the street address on the accordion's title while it's collapsed", () => {
          cy.get("#location-00-title-address").contains("Richmond Ave");
          cy.get("#location-01-title-address").contains("Oak St");
          cy.get("#location-02-title-address").contains("Joy St");
        });

        it("doesn't include the delivery information on the accordion's title", () => {
          cy.get("#location-00-title-address").contains("C/O").should("not.exist");
          cy.get("#location-01-title-address").contains("C/O").should("not.exist");
          cy.get("#location-02-title-address").contains("C/O").should("not.exist");
        });
      });

      describe("2 locations - 1 active and 1 deactivated", () => {
        before(function () {
          init.call(this);
          cy.visit("/clients/details/pd");
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
          cy.get("#location-01 [slot='title']").contains("01");
          cy.get("#location-01 [slot='title']").contains("-").should("not.exist");
        });
      });
    });

    describe("regular, isolated tests", () => {
      beforeEach(() => {
        cy.visit("/clients/details/p");
      });

      it("hides the address on the accordion's title when it's expanded", () => {
        // Clicks to expand the accordion
        cy.get("#location-00 [slot='title']").click();

        cy.get("#location-00-title-address").should("not.be.visible");
      });

      it("keeps accordions' states while tabs are switched", () => {
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
          cy.visit("/clients/details/p");

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

      const scenarios = [{ name: "edit" }, { name: "create" }];
      scenarios.forEach((scenario) => {
        const indexString = scenario.name === "edit" ? "00" : "null";
        const saveButtonSelector = `#location-${indexString}-SaveBtn`;

        describe(`${scenario.name} - save`, () => {
          describe("on success", { testIsolation: false }, () => {
            const getClientDetailsCounter = {
              count: 0,
            };

            let patchClientDetailsRequest;

            const registerInterceptors = () => {
              cy.intercept(
                {
                  method: "GET",
                  pathname: "/api/clients/details/*",
                },
                (req) => {
                  getClientDetailsCounter.count++;
                  req.continue((res) => {
                    if (getClientDetailsCounter.count > 1) {
                      const jsonBody = JSON.parse(res.body);

                      // location name updated
                      jsonBody.addresses[0].clientLocnName = "Main address";

                      res.body = JSON.stringify(jsonBody);
                    }
                  });
                },
              ).as("getClientDetails");
            };

            before(function () {
              init.call(this);

              registerInterceptors();

              resumePatch = interruptPatch((req) => {
                patchClientDetailsRequest = req;
              });

              cy.visit("/clients/details/p");
              cy.wait("@getClientDetails");

              if (scenario.name === "edit") {
                // Clicks to expand the accordion
                cy.get("#location-00 [slot='title']").click();

                cy.get("#location-00-EditBtn").click();
                cy.clearFormEntry("#emailAddress_0");

                cy.get("#location-00-SaveBtn").click();
              }

              if (scenario.name === "create") {
                cy.get("#addLocationBtn").click();

                cy.get("[data-scroll='location-3-heading']").then(($el) => {
                  const element = $el[0];
                  cy.spy(element, "scrollIntoView").as("scrollToNewLocation");
                });

                // test: new collapsible item was added
                cy.get("cds-accordion[id|='location']").should("have.length", 4);

                // test: it scrolls down to the new form
                cy.get("@scrollToNewLocation").should("be.called");

                /*
                Wait to have a focused element.
                Prevents error with focus switching.
                */
                // cy.get("[data-focus='location-3-heading']:focus");
                cy.wait(500);

                cy.fillFormEntry("#name_null", "Beach office");

                cy.selectAutocompleteEntry("#addr_null", "123", "V8V8V8");

                cy.get("#location-null-SaveBtn").click();
              }
            });

            beforeEach(() => {
              registerInterceptors();
            });

            it("disables the Save button while waiting for the response", () => {
              cy.get(saveButtonSelector).shadow().find("button").should("be.disabled");
              resumePatch();
              cy.wait("@getClientDetails");
            });

            it("prefixes the path with the corresponding location code", () => {
              if (scenario.name === "edit") {
                expect(patchClientDetailsRequest.body[0].path).to.eq("/addresses/00/emailAddress");
              } else {
                expect(patchClientDetailsRequest.body[0].path).to.eq("/addresses/null");
              }
            });

            if (scenario.name === "edit") {
              it("sends one or more 'replace' operations", () => {
                expect(patchClientDetailsRequest.body[0].op).to.eq("replace");
              });
            } else {
              it("sends an 'add' operation", () => {
                expect(patchClientDetailsRequest.body[0].op).to.eq("add");
              });
            }

            it("shows the success toast", () => {
              if (scenario.name === "edit") {
                cy.get("cds-toast-notification[kind='success']")
                  .should("be.visible")
                  .contains("00 - Mailing address");

                cy.get("cds-toast-notification[kind='success']").contains("updated");
              } else {
                cy.get("cds-toast-notification[kind='success']")
                  .should("be.visible")
                  .contains("Beach office");

                cy.get("cds-toast-notification[kind='success']").contains("created");
              }
            });

            it("reloads data", () => {
              // Called twice - one for the initial loading and one after saving.
              cy.wrap(getClientDetailsCounter).its("count").should("eq", 2);
            });

            if (scenario.name === "edit") {
              it("updates the title according to the response data", () => {
                // Note: this could happen if the value was updated in the meantime by another user.
                cy.get("#contact-10 [slot='title']").contains("Main address");
              });
            }

            if (scenario.name === "edit") {
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
            }
          });

          describe("on failure", { testIsolation: false }, () => {
            before(function () {
              init.call(this);

              cy.visit("/clients/details/p");

              if (scenario.name === "edit") {
                // Clicks to expand the accordion
                cy.get("#location-00 [slot='title']").click();

                cy.get("#location-00-EditBtn").click();

                cy.fillFormEntry("[data-id='input-notes_0']", "error", { area: true });
              } else {
                cy.get("#addLocationBtn").click();

                cy.get("cds-accordion[id|='location']").should("have.length", 4);

                /*
                Wait to have a focused element.
                Prevents error with focus switching.
                */
                // cy.get("[data-focus='location-3-heading']:focus");
                cy.wait(500);

                cy.fillFormEntry("#name_null", "Beach office");

                cy.selectAutocompleteEntry("#addr_null", "123", "V8V8V8");

                cy.fillFormEntry("[data-id='input-notes_null']", "error", { area: true });
              }

              resumePatch = interruptPatch();
              if (scenario.name === "edit") {
                cy.get("#location-00-SaveBtn").click();
              } else {
                cy.get("#location-null-SaveBtn").click();
              }
            });

            it("disables the Save button while waiting for the response", () => {
              cy.get(saveButtonSelector).shadow().find("button").should("be.disabled");
              resumePatch();
            });

            it("shows the error toast", () => {
              cy.get("cds-toast-notification[kind='error']").should("be.visible");

              if (scenario.name === "edit") {
                cy.get("cds-toast-notification[kind='error']").contains("Failed to update");
              } else {
                cy.get("cds-toast-notification[kind='error']").contains("Failed to create");
              }
            });

            it("stays in edit mode", () => {
              const id = scenario.name === "edit" ? 0 : null;
              cy.get(`#city_${id}`).should("be.visible");
              cy.get(`#emailAddress_${id}`).should("be.visible");
              cy.get(`[data-id='input-notes_${id}'`).should("be.visible");

              if (scenario.name === "edit") {
                cy.get("#location-00-SaveBtn").should("be.visible");
              } else {
                cy.get("#location-null-SaveBtn").should("be.visible");
              }
            });

            it("re-enables the Save button", () => {
              cy.get(saveButtonSelector).shadow().find("button").should("be.enabled");
            });
          });

          if (scenario.name === "edit") {
            describe("with reason modal", { testIsolation: false }, () => {
              beforeEach(function () {
                init.call(this);

                cy.intercept("PATCH", "/api/clients/details/*").as("saveClientDetails");

                cy.intercept("GET", "/api/codes/update-reasons/*/*").as("getReasonsList");

                cy.visit("/clients/details/p");

                // Clicks to expand the accordion
                cy.get("#location-00 [slot='title']").click();

                cy.get("#location-00-EditBtn").click();
                cy.fillFormEntry("#addr_0", "2 Update Av");
                cy.fillFormEntry("#city_0", "Updateland");
                cy.selectFormEntry("#province_0", "Quebec");
                cy.get("#location-00-SaveBtn").click();

                cy.wait("@getReasonsList").then(({ request }) => {
                  // requests the list of options related to Address change
                  expect(request.url.endsWith("/ADDR")).to.eq(true);
                });
              });

              it("opens the reason modal and sends the correct PATCH request with reasons", () => {
                cy.get("#reason-modal").should("be.visible");

                cy.get("#input-reason-0").should("exist");

                // Only one reason should be required
                cy.get("#input-reason-1").should("not.exist");

                cy.get("#input-reason-0").find('[part="trigger-button"]').click();

                cy.get("#input-reason-0")
                  .find("cds-dropdown-item")
                  .first()
                  .should("be.visible")
                  .click();

                resumePatch = interruptPatch();

                cy.get("#reasonSaveBtn").click();

                // Should disable the dialog button to prevent multiple clicks
                cy.get("#reasonSaveBtn").shadow().find("button").should("be.disabled");

                // Should disable the Summary button prevent multiple clicks
                cy.get(saveButtonSelector).shadow().find("button").should("be.disabled");

                resumePatch();

                cy.wait("@saveClientDetails").then((interception) => {
                  const requestBody = interception.request.body;

                  cy.log("Request Body:", JSON.stringify(requestBody));

                  expect(requestBody).to.deep.include({
                    op: "add",
                    path: "/reasons/0",
                    value: {
                      field: "/addresses/00",
                      reason: "R1",
                    },
                  });

                  // Only 1 "add" operation (the reason one)
                  expect(
                    (requestBody as any[]).filter((item) => item.op === "add"),
                  ).to.have.lengthOf(1);
                });
              });
            });
          }
        });
      });
    });
  });

  describe("contacts tab", () => {
    describe("non-user action tests", { testIsolation: false }, () => {
      before(function () {
        init.call(this);
        cy.visit("/clients/details/p");

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
        cy.get("#panel-contacts cds-accordion-item").eq(0).contains("Cheryl Bibby");
        cy.get("#panel-contacts cds-accordion-item").eq(1).contains("Christoffer Stewart");
        cy.get("#panel-contacts cds-accordion-item").eq(2).contains("Edward Burns");
      });

      it("displays the associated locations on the accordion's title while it's collapsed", () => {
        cy.get("#contact-10-title-locations").should("be.visible");
        cy.get("#contact-11-title-locations").should("be.visible");
        cy.get("#contact-12-title-locations").should("be.visible");
      });
    });

    describe("regular, isolated tests", () => {
      describe("3 contacts", () => {
        beforeEach(() => {
          cy.visit("/clients/details/p");

          // Switch to the Contacts tab
          cy.get("#tab-contacts").click();

          // Make sure the current tab panel was effectively switched
          cy.get("#panel-locations").should("have.attr", "hidden");
          cy.get("#panel-contacts").should("not.have.attr", "hidden");
        });

        it("hides the associated locations on the accordion's title when it's expanded", () => {
          // Clicks to expand the accordion
          cy.get("#contact-10 [slot='title']").click();
          cy.get("#contact-10-title-locations").should("not.be.visible");
        });

        it("keeps accordions' states while tabs are switched", () => {
          // Expand first and third contacts, leave second one collapsed
          cy.get("#panel-contacts cds-accordion-item [slot='title']").first().click();
          cy.get("#panel-contacts cds-accordion-item [slot='title']").last().click();

          // Switch to another tab (Locations)
          cy.get("#tab-locations").click();

          // Make sure the current tab panel was effectively switched
          cy.get("#panel-contacts").should("have.attr", "hidden");
          cy.get("#panel-locations").should("not.have.attr", "hidden");

          // Switch back to tab Contacts
          cy.get("#tab-contacts").click();

          // First contact is still open
          cy.get("#panel-contacts cds-accordion-item").eq(0).should("have.attr", "open");

          // Second contact is still closed
          cy.get("#panel-contacts cds-accordion-item").eq(1).should("not.have.attr", "open");

          // Third contact is still open
          cy.get("#panel-contacts cds-accordion-item").eq(2).should("have.attr", "open");
        });
      });
      describe("no contacts", () => {
        beforeEach(() => {
          cy.visit("/clients/details/pd");

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
          cy.get("#contact-1-title-locations").contains("01");
          cy.get("#contact-1-title-locations").contains("-").should("not.exist");
        });

        /*
        Note: this test would not be possible on the ContactView component, since the calculation
        is performed outside that component.
        */
        it("displays the location code in the contact's Associated locations, without a dash", () => {
          // expands the accordion
          cy.get("#contact-1 cds-accordion-item").click();

          cy.get("#contact-1-associatedLocations").contains("01").should("be.visible");
          cy.get("#contact-1-associatedLocations").contains("-").should("not.exist");
        });
      });
    });

    describe("when role:CLIENT_EDITOR", () => {
      describe("name duplication", () => {
        beforeEach(() => {
          cy.visit("/clients/details/p");

          // Switch to the Contacts tab
          cy.get("#tab-contacts").click();

          // Clicks to expand the accordion
          cy.get("#contact-10 [slot='title']").click();

          cy.get("#contact-10-EditBtn").click();

          cy.clearFormEntry("#fullName_10");

          // This is the same name of another contact
          cy.fillFormEntry("#fullName_10", "Christoffer Stewart");
        });

        it("shows the error on field Full name", () => {
          cy.checkInputErrorMessage("#fullName_10", "This value is already in use");

          cy.get("#contact-10-SaveBtn").shadow().find("button").should("be.disabled");
        });
      });

      const scenarios = [{ name: "edit" }, { name: "create" }];
      scenarios.forEach((scenario) => {
        const indexString = scenario.name === "edit" ? "10" : "null";
        const saveButtonSelector = `#contact-${indexString}-SaveBtn`;

        describe(`${scenario.name} - save`, () => {
          describe("on success", { testIsolation: false }, () => {
            const getClientDetailsCounter = {
              count: 0,
            };

            let patchClientDetailsRequest;

            const registerInterceptors = () => {
              cy.intercept(
                {
                  method: "GET",
                  pathname: "/api/clients/details/*",
                },
                (req) => {
                  getClientDetailsCounter.count++;
                  req.continue((res) => {
                    if (getClientDetailsCounter.count > 1) {
                      const jsonBody = JSON.parse(res.body);

                      // contactName updated
                      jsonBody.contacts[0].contactName = `${jsonBody.contacts[0].contactName} Married`;

                      res.body = JSON.stringify(jsonBody);
                    }
                  });
                },
              ).as("getClientDetails");
            };

            before(function () {
              init.call(this);

              registerInterceptors();

              resumePatch = interruptPatch((req) => {
                patchClientDetailsRequest = req;
              });

              cy.visit("/clients/details/p");
              cy.wait("@getClientDetails");

              // Switch to the Contacts tab
              cy.get("#tab-contacts").click();

              if (scenario.name === "edit") {
                // Clicks to expand the accordion
                cy.get("#contact-10 [slot='title']").click();

                cy.get("#contact-10-EditBtn").click();

                cy.clearFormEntry("#emailAddress_10");
                cy.fillFormEntry("#emailAddress_10", "something@else.com");

                cy.get("#contact-10-SaveBtn").click();
              }

              if (scenario.name === "create") {
                cy.get("#addContactBtn").click();

                cy.get("[data-scroll='contact-null-heading']").then(($el) => {
                  const element = $el[0];
                  cy.spy(element, "scrollIntoView").as("scrollToNewContact");
                });

                // test: new collapsible item was added
                cy.get("cds-accordion[id|='contact']").should("have.length", 4);

                // test: it scrolls down to the new form
                cy.get("@scrollToNewContact").should("be.called");

                /*
                Wait to have a focused element.
                Prevents error with focus switching.
                */
                // cy.get("[data-focus='contact-null-heading']:focus");
                cy.wait(500);

                cy.fillFormEntry("#fullName_null", "Steve New");

                cy.selectFormEntry("#role_null", "Billing");

                cy.selectFormEntry("#addressname_null", "02 - Warehouse");

                cy.fillFormEntry("#emailAddress_null", "snew@corp.com");

                cy.fillFormEntry("#businessPhoneNumber_null", "1234567890");

                cy.get("#contact-null-SaveBtn").click();
              }
            });

            beforeEach(() => {
              registerInterceptors();
            });

            it("disables the Save button while waiting for the response", () => {
              cy.get(saveButtonSelector).shadow().find("button").should("be.disabled");
              resumePatch();
              cy.wait("@getClientDetails");
            });

            it("prefixes the path with the corresponding contact code", () => {
              if (scenario.name === "edit") {
                expect(patchClientDetailsRequest.body[0].path).to.eq("/contacts/10/emailAddress");
              } else {
                expect(patchClientDetailsRequest.body[0].path).to.eq("/contacts/null");
              }
            });

            if (scenario.name === "edit") {
              it("sends one or more 'replace' operations", () => {
                expect(patchClientDetailsRequest.body[0].op).to.eq("replace");
              });
            } else {
              it("sends an 'add' operation", () => {
                expect(patchClientDetailsRequest.body[0].op).to.eq("add");
              });
            }

            it("shows the success toast", () => {
              if (scenario.name === "edit") {
                cy.get("cds-toast-notification[kind='success']")
                  .should("be.visible")
                  .contains("Cheryl Bibby");

                cy.get("cds-toast-notification[kind='success']").contains("updated");
              } else {
                cy.get("cds-toast-notification[kind='success']")
                  .should("be.visible")
                  .contains("Steve New");

                cy.get("cds-toast-notification[kind='success']").contains("created");
              }
            });

            it("reloads data", () => {
              // Called twice - one for the initial loading and one after saving.
              cy.wrap(getClientDetailsCounter).its("count").should("eq", 2);
            });

            if (scenario.name === "edit") {
              it("updates the title according to the response data", () => {
                // Note: this could happen if the value was updated in the meantime by another user.
                cy.get("#contact-10 [slot='title']").contains("Cheryl Bibby Married");
              });
            }

            if (scenario.name === "edit") {
              it("gets back into view mode", () => {
                // Fields that belong to the form (edit mode)
                testHidden("#fullName_10");
                testHidden("#role_10");
                testHidden("#emailAddress_10");

                cy.get("#contact-10-SaveBtn").should("not.exist");

                testReadonly("#contact-10-contactType");
                testReadonly("#contact-10-emailAddress");

                cy.get("#contact-10-EditBtn").should("be.visible");
              });
            }
          });

          describe("on failure", { testIsolation: false }, () => {
            before(function () {
              init.call(this);

              resumePatch = interruptPatch();

              cy.visit("/clients/details/p");

              // Switch to the Contacts tab
              cy.get("#tab-contacts").click();

              if (scenario.name === "edit") {
                // Clicks to expand the accordion
                cy.get("#contact-10 [slot='title']").click();

                cy.get("#contact-10-EditBtn").click();
                cy.clearFormEntry("#emailAddress_10");
                cy.fillFormEntry("#emailAddress_10", "error@error.com");
                cy.get("#contact-10-SaveBtn").click();
              } else {
                cy.get("#addContactBtn").click();

                cy.get("cds-accordion[id|='contact']").should("have.length", 4);

                /*
                Wait to have a focused element.
                Prevents error with focus switching.
                */
                // cy.get("[data-focus='contact-null-heading']:focus");
                cy.wait(500);

                cy.fillFormEntry("#fullName_null", "Steve New");

                cy.selectFormEntry("#role_null", "Billing");

                cy.selectFormEntry("#addressname_null", "02 - Warehouse");

                cy.fillFormEntry("#emailAddress_null", "error@error.com");

                cy.fillFormEntry("#businessPhoneNumber_null", "1234567890");

                cy.get("#contact-null-SaveBtn").click();
              }
            });

            it("disables the Save button while waiting for the response", () => {
              cy.get(saveButtonSelector).shadow().find("button").should("be.disabled");
              resumePatch();
            });

            it("shows the error toast", () => {
              cy.get("cds-toast-notification[kind='error']").should("be.visible");

              if (scenario.name === "edit") {
                cy.get("cds-toast-notification[kind='error']").contains("Failed to update");
              } else {
                cy.get("cds-toast-notification[kind='error']").contains("Failed to create");
              }
            });

            it("stays in edit mode", () => {
              const id = scenario.name === "edit" ? 10 : null;
              cy.get(`#fullName_${id}`).should("be.visible");
              cy.get(`#role_${id}`).should("be.visible");
              cy.get(`#emailAddress_${id}`).should("be.visible");

              cy.get(`#contact-${id}-SaveBtn`).should("be.visible");
            });

            it("re-enables the Save button", () => {
              cy.get(saveButtonSelector).shadow().find("button").should("be.enabled");
            });
          });
        });
      });

      describe("delete and create a new contact with the same name", () => {
        const getClientDetailsCounter = {
          count: 0,
        };

        let contactName: string;
        beforeEach(function () {
          init.call(this);

          cy.intercept(
            {
              method: "GET",
              pathname: "/api/clients/details/*",
            },
            (req) => {
              getClientDetailsCounter.count++;
              req.continue((res) => {
                const jsonBody = JSON.parse(res.body);

                contactName = jsonBody.contacts[0].contactName;

                if (getClientDetailsCounter.count === 2) {
                  // removes the first contact
                  jsonBody.contacts.shift();

                  res.body = JSON.stringify(jsonBody);
                }
              });
            },
          ).as("getClientDetails");

          cy.visit("/clients/details/p");
          cy.wait("@getClientDetails");

          // Switch to the Contacts tab
          cy.get("#tab-contacts").click();
        });

        it("should not complain about having the same name of a contact that has been just deleted", () => {
          cy.wrap(contactName).should("eq", "Cheryl Bibby");

          cy.get("#contact-10 [slot='title']").contains(contactName);

          // Clicks to expand the accordion
          cy.get("#contact-10 [slot='title']").click();

          cy.get("#contact-10-EditBtn").click();

          // Delete contact
          cy.get("#contact-10-DeleteBtn").click();
          cy.get("#modal-delete .cds--modal-submit-btn").filter(":visible").click();

          cy.wait("@getClientDetails");

          cy.get("#addContactBtn").click();

          cy.get("cds-accordion[id|='contact']").should("have.length", 3);

          /*
          Wait to have a focused element.
          Prevents error with focus switching.
          */
          // cy.get("[data-focus='contact-null-heading']:focus");
          cy.wait(500);

          // Use the same contact name
          cy.fillFormEntry("#fullName_null", contactName);

          // No error in the field
          cy.get("#fullName_null").should("not.have.attr", "invalid");

          cy.selectFormEntry("#role_null", "Billing");

          cy.selectFormEntry("#addressname_null", "02 - Warehouse");

          cy.fillFormEntry("#emailAddress_null", "snew@corp.com");

          cy.fillFormEntry("#businessPhoneNumber_null", "1234567890");

          cy.get("#contact-null-SaveBtn").shadow().find("button").should("be.enabled");
        });
      });
    });
  });

  describe('related clients tab - "Under construction"', () => {
    const clientNumber = "12321";

    beforeEach(() => {
      cy.addToLocalStorage("VITE_FEATURE_FLAGS", { RELATED_CLIENTS: false });

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

    after(() => {
      cy.addToLocalStorage("VITE_FEATURE_FLAGS", { RELATED_CLIENTS: true });
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

  describe("related clients tab", () => {
    describe("non-user action tests", { testIsolation: false }, () => {
      describe("4 relationships under 2 active locations", () => {
        before(function () {
          init.call(this);
          cy.visit("/clients/details/p");

          // Switch to the Related clients tab
          cy.get("#tab-related").click();
        });

        it('displays the title "Client relationships"', () => {
          cy.get("#panel-related").contains("Client relationships");
        });

        it("displays one collapsed accordion component for each location", () => {
          cy.get("#panel-related").within(() => {
            // There are 2 accordions
            cy.get("cds-accordion").should("have.length", 2);

            // All accordions are initially collapsed
            cy.get("cds-accordion cds-accordion-item").each(($el) => {
              expect($el).not.to.have.attr("open");
            });
          });
        });

        it("displays the location name on the accordion's title", () => {
          cy.get("#relationships-location-00 [slot='title']").contains("00 - Mailing address");
          cy.get("#relationships-location-01 [slot='title']").contains("01 - Accountant address");
        });
      });

      describe("4 relationships under 2 locations - 1 active and 1 deactivated", () => {
        before(function () {
          init.call(this);
          cy.visit("/clients/details/pd");

          // Switch to the Related clients tab
          cy.get("#tab-related").click();
        });
        it("doesn't display the tag Deactivated when location is not expired", () => {
          cy.get("cds-tag#relationships-location-00-deactivated").should("not.exist");
        });
        it("displays the tag Deactivated when location is expired", () => {
          cy.get("cds-tag#relationships-location-01-deactivated").contains("Deactivated");
        });
      });

      describe("location without name", () => {
        before(function () {
          init.call(this);
          cy.visit("/clients/details/se");

          // Switch to the Related clients tab
          cy.get("#tab-related").click();
        });
        it("displays only the location code, without the dash", () => {
          cy.get("#relationships-location-01 [slot='title']").contains("01");
          cy.get("#relationships-location-01 [slot='title']").contains("-").should("not.exist");
        });
      });
    });

    describe("regular, isolated tests", () => {
      beforeEach(function () {
        init.call(this);
        cy.visit("/clients/details/p");
      });

      describe("after switching to the Related clients tab", () => {
        beforeEach(function () {
          // Switch to the Related clients tab
          cy.get("#tab-related").click();
        });

        it("keeps accordions' states while tabs are switched", () => {
          // Expand first location, leave second one collapsed
          cy.get("#relationships-location-00 [slot='title']").click();

          // Switch to another tab (Contacts)
          cy.get("#tab-contacts").click();

          // Make sure the current tab panel was effectively switched
          cy.get("#panel-related").should("have.attr", "hidden");
          cy.get("#panel-contacts").should("not.have.attr", "hidden");

          // Switch back to tab Related clients
          cy.get("#tab-related").click();

          // First location is still open
          cy.get("#relationships-location-00 cds-accordion-item").should("have.attr", "open");

          // Second location is still closed
          cy.get("#relationships-location-01 cds-accordion-item").should("not.have.attr", "open");
        });
      });
    });

    describe("when role:CLIENT_EDITOR", () => {
      const fillInRequiredFields = (locationText: string) => {
        cy.selectFormEntry("cds-dropdown#rc-null-null-location", locationText);

        cy.wait("@getRelationshipTypes");

        // For some reason some tests fail without this extra wait time
        cy.wait(50);

        cy.selectFormEntry("cds-dropdown#rc-null-null-relationship", "Shareholder");

        cy.selectAutocompleteEntry("#rc-null-null-relatedClient", "james", "00000007");

        cy.wait("@getClientDetails");

        cy.selectFormEntry(
          "cds-dropdown#rc-null-null-relatedClient-location",
          "00 - Mailing address",
        );
      };

      describe("combined data duplication", () => {
        beforeEach(() => {
          cy.intercept({
            method: "GET",
            pathname: "/api/clients/details/*",
          }).as("getClientDetails");

          cy.intercept("GET", "/api/codes/relationship-types/*").as("getRelationshipTypes");

          cy.visit("/clients/details/se");

          cy.wait("@getClientDetails");

          // Switch to the Related clients tab
          cy.get("#tab-related").click();

          cy.get("#addClientRelationshipBtn").click();

          /*
          Wait to have a focused element.
          Prevents error with focus switching.
          */
          // cy.get("[data-focus='relationships-location-null-heading']:focus");
          cy.wait(500);

          fillInRequiredFields("01");
        });

        it("shows the errors on the unique validation-related set of fields", () => {
          cy.get("#rc-null-null-location").should("have.attr", "invalid");
          cy.get("#rc-null-null-relationship").should("have.attr", "invalid");
          cy.get("#rc-null-null-relatedClient").should("have.attr", "invalid");
          cy.get("#rc-null-null-relatedClient-location").should("have.attr", "invalid");

          cy.get("cds-button#rc-null-null-SaveBtn").shadow().find("button").should("be.disabled");
        });
      });

      const scenarios = [
        { name: "create", shouldInterruptOnFailure: true },
        { name: "delete", shouldInterruptOnFailure: false },
        { name: "edit", shouldInterruptOnFailure: true },
      ];

      scenarios.forEach((scenario) => {
        const index = scenario.name === "create" ? "null" : 0;
        const locationIndex = scenario.name === "create" ? "null" : "01";
        const saveButtonSelector = `#rc-${locationIndex}-${index}-SaveBtn`;
        const deleteButtonSelector = `#rc-${locationIndex}-${index}-DeleteBtn`;

        describe(`${scenario.name}`, () => {
          describe("on success", { testIsolation: false }, () => {
            const getClientDetailsCounter = {
              count: 0,
            };

            let patchClientDetailsRequest;

            const registerInterceptors = () => {
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

              cy.intercept("GET", "/api/codes/relationship-types/*").as("getRelationshipTypes");
            };

            before(function () {
              init.call(this);

              registerInterceptors();

              resumePatch = interruptPatch((req) => {
                patchClientDetailsRequest = req;
              });

              cy.visit("/clients/details/p");
              cy.wait("@getClientDetails");

              // Switch to the Related clients tab
              cy.get("#tab-related").click();

              if (scenario.name === "edit") {
                // Clicks to expand the accordion
                cy.get("#relationships-location-01 [slot='title']").click();

                // Edit the relationship with index=0
                cy.get("#location-01-row-0-EditBtn").click();
                cy.clearFormEntry("#rc-01-0-percentageOwnership");

                cy.get(saveButtonSelector).click();
              }

              if (scenario.name === "delete") {
                // Clicks to expand the accordion
                cy.get("#relationships-location-01 [slot='title']").click();

                // Edit the relationship with index=0
                cy.get("#location-01-row-0-EditBtn").click();

                cy.get(deleteButtonSelector).click();

                cy.get("#modal-delete .cds--modal-submit-btn").filter(":visible").click();
              }

              if (scenario.name === "create") {
                cy.get("#addClientRelationshipBtn").click();

                cy.get("[data-scroll='relationships-location-null-heading']").then(($el) => {
                  const element = $el[0];
                  cy.spy(element, "scrollIntoView").as("scrollToNewRelationship");
                });

                // test: new collapsible item was added
                cy.get("cds-accordion[id|='relationships-location']").should("have.length", 3);

                // test: it scrolls down to the new form
                cy.get("@scrollToNewRelationship").should("be.called");

                /*
                Wait to have a focused element.
                Prevents error with focus switching.
                */
                // cy.get("[data-focus='relationships-location-null-heading']:focus");
                cy.wait(500);

                fillInRequiredFields("01 - Accountant address");

                cy.get("#rc-null-null-SaveBtn").click();
              }
            });

            beforeEach(() => {
              registerInterceptors();
            });

            // Isolated scope
            {
              const testBeforePatchResponse = (test: () => void) => {
                test();

                resumePatch();

                // Reloading data
                cy.wait("@getClientDetails");
              };
              if (scenario.name === "create") {
                it("disables the Save button while waiting for the response", () => {
                  testBeforePatchResponse(() => {
                    cy.get(saveButtonSelector).shadow().find("button").should("be.disabled");
                  });
                });
              } else {
                it("disables the Save and the Delete buttons while waiting for the response", () => {
                  testBeforePatchResponse(() => {
                    /*
                    Note: these two assertions need to be in the same test, otherwise there seems
                    to be some kind of sync issue with the intercept.
                    */
                    cy.get(saveButtonSelector).shadow().find("button").should("be.disabled");
                    cy.get(deleteButtonSelector).shadow().find("button").should("be.disabled");
                  });
                });
              }
            }

            it("prefixes the path with the corresponding location code and relationship index", () => {
              if (scenario.name === "edit") {
                expect(patchClientDetailsRequest.body[0].path).to.eq(
                  "/relatedClients/01/0/percentageOwnership",
                );
              } else if (scenario.name === "delete") {
                expect(patchClientDetailsRequest.body[0].path).to.eq("/relatedClients/01/0");
              } else if (scenario.name === "create") {
                expect(patchClientDetailsRequest.body[0].path).to.eq("/relatedClients/01/null");
              }
            });

            if (scenario.name === "edit") {
              it("sends one or more 'replace' operations", () => {
                expect(patchClientDetailsRequest.body[0].op).to.eq("replace");
              });
            } else if (scenario.name === "delete") {
              it("sends a 'remove' operation", () => {
                expect(patchClientDetailsRequest.body[0].op).to.eq("remove");
              });
            } else if (scenario.name === "create") {
              it("sends an 'add' operation", () => {
                expect(patchClientDetailsRequest.body[0].op).to.eq("add");
              });
            }

            it("shows the success toast", () => {
              if (scenario.name === "edit") {
                cy.get("cds-toast-notification[kind='success']")
                  .should("be.visible")
                  .contains("00000172, Nature Nurturers");

                cy.get("cds-toast-notification[kind='success']").contains("updated");
              } else if (scenario.name === "delete") {
                cy.get("cds-toast-notification[kind='success']")
                  .should("be.visible")
                  .contains("00000172, Nature Nurturers");

                cy.get("cds-toast-notification[kind='success']").contains("deleted");
              } else if (scenario.name === "create") {
                cy.get("cds-toast-notification[kind='success']")
                  .should("be.visible")
                  .contains("00000007, James Bond Bond");

                cy.get("cds-toast-notification[kind='success']").contains("created");
              }
            });

            it("reloads data", () => {
              /*
              Called three times - one for the initial loading, one for the related client and one
              after saving.
              */
              cy.wrap(getClientDetailsCounter).its("count").should("eq", 3);
            });

            if (["edit", "delete"].includes(scenario.name)) {
              it("gets back into view mode", () => {
                // Fields that belong to the form (edit mode)
                testHidden(`#rc-${locationIndex}-${index}-location`);
                testHidden(`#rc-${locationIndex}-${index}-relationship`);
                testHidden(`#rc-${locationIndex}-${index}-relatedClient`);
                testHidden(`#rc-${locationIndex}-${index}-relatedClient-location`);
                testHidden(`#rc-${locationIndex}-${index}-percentageOwnership`);

                cy.get(saveButtonSelector).should("not.exist");

                cy.get("#location-01-row-0-EditBtn").should("be.visible");
              });
            }
          });

          describe("on failure", { testIsolation: false }, () => {
            const registerInterceptors = () => {
              cy.intercept(
                {
                  method: "GET",
                  pathname: "/api/clients/details/*",
                },
                (req) => {
                  req.continue();
                },
              ).as("getClientDetails");

              cy.intercept("GET", "/api/codes/relationship-types/*").as("getRelationshipTypes");
            };

            before(function () {
              init.call(this);

              registerInterceptors();

              cy.visit("/clients/details/p");
              cy.wait("@getClientDetails");

              // Switch to the Related clients tab
              cy.get("#tab-related").click();

              if (scenario.name === "edit") {
                // Clicks to expand the accordion
                cy.get("#relationships-location-01 [slot='title']").click();

                // Edit the relationship with index=0
                cy.get("#location-01-row-0-EditBtn").click();

                cy.clearFormEntry("#rc-01-0-percentageOwnership");

                // The value "88" triggers the error on the stub server
                cy.fillFormEntry(`#rc-${locationIndex}-${index}-percentageOwnership`, "88");
              } else if (scenario.name === "delete") {
                // Clicks to expand the accordion
                cy.get("#relationships-location-01 [slot='title']").click();

                // Edit the relationship with index=0
                cy.get("#location-01-row-0-EditBtn").click();

                // Override the API response
                cy.intercept("PATCH", "/api/clients/details/*", {
                  statusCode: 500,
                  body: "Sample error message",
                  delay: 250,
                }).as("saveClientDetails");
              } else if (scenario.name === "create") {
                cy.get("#addClientRelationshipBtn").click();

                cy.get("cds-accordion[id|='relationships-location']").should("have.length", 3);

                /*
                Wait to have a focused element.
                Prevents error with focus switching.
                */
                // cy.get("[data-focus='location-3-heading']:focus");
                cy.wait(500);

                fillInRequiredFields("01 - Accountant address");

                // The value "88" triggers the error on the stub server
                cy.fillFormEntry(`#rc-${locationIndex}-${index}-percentageOwnership`, "88");
              }

              if (scenario.shouldInterruptOnFailure) {
                resumePatch = interruptPatch();
              }
              if (scenario.name === "edit") {
                cy.get("#rc-01-0-SaveBtn").click();
              } else if (scenario.name === "delete") {
                cy.get(deleteButtonSelector).click();
                cy.get("#modal-delete .cds--modal-submit-btn").filter(":visible").click();
              } else if (scenario.name === "create") {
                cy.get("#rc-null-null-SaveBtn").click();
              }
            });

            // Isolated scope
            {
              const testBeforePatchResponse = (test: () => void) => {
                test();

                if (scenario.shouldInterruptOnFailure) {
                  resumePatch();
                } else {
                  cy.wait("@saveClientDetails");
                }
              };
              if (scenario.name === "create") {
                it("disables the Save button while waiting for the response", () => {
                  testBeforePatchResponse(() => {
                    cy.get(saveButtonSelector).shadow().find("button").should("be.disabled");
                  });
                });
              } else {
                it("disables the Save and the Delete buttons while waiting for the response", () => {
                  testBeforePatchResponse(() => {
                    /*
                    Note: these two assertions need to be in the same test, otherwise there seems
                    to be some kind of sync issue with the intercept.
                    */
                    cy.get(saveButtonSelector).shadow().find("button").should("be.disabled");
                    cy.get(deleteButtonSelector).shadow().find("button").should("be.disabled");
                  });
                });
              }
            }

            it("shows the error toast", () => {
              cy.get("cds-toast-notification[kind='error']").should("be.visible");

              if (scenario.name === "edit") {
                cy.get("cds-toast-notification[kind='error']").contains("Failed to update");
              } else if (scenario.name === "delete") {
                cy.get("cds-toast-notification[kind='error']").contains("Failed to delete");
              } else if (scenario.name === "create") {
                cy.get("cds-toast-notification[kind='error']").contains("Failed to create");
              }
            });

            it("stays in edit mode", () => {
              cy.get(`#rc-${locationIndex}-${index}-location`).should("be.visible");
              cy.get(`#rc-${locationIndex}-${index}-relationship`).should("be.visible");
              cy.get(`#rc-${locationIndex}-${index}-relatedClient`).should("be.visible");
              cy.get(`#rc-${locationIndex}-${index}-relatedClient-location`).should("be.visible");
              cy.get(`#rc-${locationIndex}-${index}-percentageOwnership`).should("be.visible");

              if (scenario.name === "create") {
                cy.get("#rc-null-null-SaveBtn").should("be.visible");
              } else {
                cy.get("#rc-01-0-SaveBtn").should("be.visible");
                cy.get(deleteButtonSelector).should("be.visible");
              }
            });

            it("re-enables the Save button", () => {
              cy.get(saveButtonSelector).shadow().find("button").should("be.enabled");
            });
          });
        });
      });
    });
  });

  describe("History tab", () => {
    const clientNumber = "12321";
  
    let resolveGetClientHistory: () => void;
  
    beforeEach(() => {
      cy.visit(`/clients/details/${clientNumber}`);
  
      cy.window().then((win) => {
        cy.stub(win, "open").as("windowOpen");
      });

      const promiseGetClientHistory = new Promise<void>((resolve) => {
        resolveGetClientHistory = resolve;
      });
  
      cy.intercept(
        {
          method: "GET",
          pathname: `/api/clients/history-logs/${clientNumber}`,
        },
        (req) => {
          req.continue(() => promiseGetClientHistory);
        }
      ).as("getClientHistory");
  
      // Now that the intercept is ready, click the tab
      cy.get("#tab-history").click();
  
      cy.get("#panel-locations").should("have.attr", "hidden");
      cy.get("#panel-history").should("not.have.attr", "hidden");
    });
  
    it("shows text skeletons only while data is loading", () => {
      cy.get(".heading-03-skeleton").should("be.visible");
      cy.get(".history-indicator-line").should("be.visible");
      cy.contains("h5", "Client created").should("not.exist");
  
      resolveGetClientHistory();
      cy.wait("@getClientHistory");
  
      cy.get(".heading-03-skeleton").should("not.exist");
      //At least a 'Client created' should be displayed
      cy.contains("h5", "Client created").should("be.visible");
      //The logs should the present
      cy.get("#historyLogsId").should("be.visible");
      //The details should not be visible
      cy.get("#logDetails0").should("not.exist");
    });

    it("shows empty state page when there is no data for the selected filter", () => {
      cy.selectFormEntry("#filterById", "Doing business as");

      resolveGetClientHistory();
      cy.wait("@getClientHistory");

      cy.get(".empty-table-list").should("be.visible");
      cy.get(".standard-svg").should("be.visible");
    });
  });

  describe("hash links", { testIsolation: false }, () => {
    before(function () {
      init.call(this);
    });

    it("opens the locations tab by default", () => {
      cy.visit("/clients/details/p");
      cy.get("#panel-locations").should("not.have.attr", "hidden");
    });

    it("opens the contacts tab", () => {
      cy.visit("/clients/details/p#contacts");
      cy.get("#panel-contacts").should("not.have.attr", "hidden");

      cy.get("#panel-locations").should("have.attr", "hidden");
    });

    it("opens the Related clients tab", () => {
      cy.visit("/clients/details/p#related");
      cy.get("#panel-related").should("not.have.attr", "hidden");

      cy.get("#panel-contacts").should("have.attr", "hidden");
    });

    it("opens the History tab", () => {
      cy.visit("/clients/details/p#history");
      cy.get("#panel-history").should("not.have.attr", "hidden");

      cy.get("#panel-related").should("have.attr", "hidden");
    });

    it("opens the locations tab", () => {
      cy.visit("/clients/details/p#locations");
      cy.get("#panel-locations").should("not.have.attr", "hidden");

      cy.get("#panel-history").should("have.attr", "hidden");
    });
  });
});
