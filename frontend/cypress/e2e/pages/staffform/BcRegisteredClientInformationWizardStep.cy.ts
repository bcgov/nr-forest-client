import testCases from "../../../fixtures/staff/bcregisteredscenarios.json";

/* eslint-disable no-undef */
describe("BC Registered Staff Wizard Step", () => {
  beforeEach(() => {
    cy.viewport(1920, 1080);

    cy.intercept("GET", "**/api/codes/identification-types", {
      fixture: "identificationTypes.json",
    }).as("getIdentificationTypes");

    cy.intercept("GET", "/api/clients/name/exi", {
      fixture: "clients/bcreg_ac_list1.json",
    });
    
    cy.intercept("GET", "/api/clients/name/Koalll%C3%A1", {
      fixture: "clients/bcreg_ac_list4.json",
    });

    cy.intercept("GET", "/api/clients/C1234567", {
      fixture: "clients/bcreg_C1234567.json",
    });

    cy.fixture("clients/bcreg_ac_list2.json").then((data) =>
      data.forEach((element) =>
        cy.intercept(
          "GET",
          `**/api/clients/name/${encodeURIComponent(element.name)}`,
          {
            fixture: "clients/bcreg_ac_list2.json",
          }
        )
      )
    );

    cy.fixture("clients/bcreg_ac_list3.json").then((data) =>
      data.forEach((element) =>
        cy.intercept(
          "GET",
          `**/api/clients/name/${encodeURIComponent(element.name)}`,
          {
            fixture: "clients/bcreg_ac_list3.json",
          }
        )
      )
    );
    cy.intercept("GET", "**/api/clients/name/Corporation", {
      statusCode: 200,
      fixture: "clients/bcreg_ac_list3.json",
    }).as("clientSearchCORPORATION");
  });

  it("should render the component", () => {
    loginAndNavigateToStaffForm();
  });

  describe("Validation", () => {
    beforeEach(() => {
      // Existing Corporation 1
      cy.intercept("GET", "**/api/clients/name/cor", {
        statusCode: 200,
        fixture: "clients/bcreg_ac_list3.json",
      }).as("clientSearchCOR");

      cy.intercept("GET", `**/api/clients/C1234567`, {
        fixture: "clients/bcreg_C1234567.json",
      }).as("clientDetailsC1234567");

      // Long name corporation
      cy.intercept("GET", "**/api/clients/name/lon", {
        statusCode: 200,
        fixture: "clients/bcreg_ac_list4.json",
      }).as("clientSearchLON");

      cy.intercept(
        "GET",
        `**/api/clients/name/${encodeURIComponent(
          "Existing Corporation with a super long name that should not be allowed here"
        )}`,
        {
          fixture: "clients/bcreg_ac_list4.json",
        }
      ).as("clientSearchEncodedLON");

      cy.intercept("GET", "**/api/clients/name/all", {
        statusCode: 200,
        fixture: "clients/bcreg_ac_list4.json",
      }).as("clientSearchALLLA1");

      cy.intercept("GET", "**/api/clients/name/alll", {
        statusCode: 200,
        fixture: "clients/bcreg_ac_list4.json",
      }).as("clientSearchALLLA2");

      cy.intercept("GET", "**/api/clients/name/alllá", {
        statusCode: 200,
        fixture: "clients/bcreg_ac_list4.json",
      }).as("clientSearchALLLA3");

      cy.intercept("GET", `**/api/clients/name/Koalll%C3%A1}`, {
        fixture: "clients/bcreg_ac_list4.json",
      }).as("clientSearchEncoded");

      cy.intercept(
        "GET",
        `**/api/clients/name/${encodeURIComponent("alllá")}`,
        {
          fixture: "clients/bcreg_ac_list4.json",
        }
      ).as("clientSearchEncodedALLLA4");

      cy.intercept("GET", `**/api/clients/C1231231`, {
        fixture: "clients/bcreg_C1231231.json",
      }).as("clientDetailsC1231231");

      // Existing Sole Proprietorship
      cy.intercept("GET", "**/api/clients/name/sol", {
        statusCode: 200,
        fixture: "clients/bcreg_ac_list4.json",
      }).as("clientSearchSOL");

      cy.intercept("GET", `**/api/clients/FM123456`, {
        fixture: "clients/bcreg_FM123456.json",
      }).as("clientDetailsFM123456");
      loginAndNavigateToStaffForm();
    });

    it("should validate the client name", () => {
      cy.selectAutocompleteEntry(
        "#businessName",
        "cor",
        "C1234567",
        "@clientSearchCOR"
      );

      cy.get("#businessName").shadow().find("div#selection-button").click();

      cy.checkAutoCompleteErrorMessage(
        "#businessName",
        "Client name cannot be empty"
      );

      cy.wait(2);

      cy.selectAutocompleteEntry(
        "#businessName",
        "lon",
        "C1231231",
        "@clientSearchLON"
      );

      cy.checkAutoCompleteErrorMessage(
        "#businessName",
        "The client name has a 60 character limit"
      );

      cy.wait(2);

      cy.get("#businessName").shadow().find("div#selection-button").click();

      cy.checkAutoCompleteErrorMessage(
        "#businessName",
        "Client name cannot be empty"
      );
      cy.wait(2);

      cy.selectAutocompleteEntry(
        "#businessName",
        "alllá",
        "FM999457",
        "@clientSearchEncodedALLLA4"
      );
      cy.wait(2);

      cy.checkAutoCompleteErrorMessage(
        "#businessName",
        "The client name can only contain: A-Z, a-z, 0-9, space or common symbols"
      );
    });

    it("should validate work safe bc number", () => {
      cy.selectAutocompleteEntry(
        "#businessName",
        "cor",
        "C1234567",
        "@clientSearchCOR"
      );

      cy.wait("@clientDetailsC1234567");

      cy.get("#workSafeBCNumber").should("exist").and("have.value", "");

      cy.get("#workSafeBCNumber").shadow().find("input").clear();

      cy.fillFormEntry("#workSafeBCNumber", "potato");

      cy.checkInputErrorMessage(
        "#workSafeBCNumber",
        "WorkSafeBC number should contain only numbers"
      );

      cy.wait(2);

      cy.get("#workSafeBCNumber").shadow().find("input").clear();

      cy.fillFormEntry("#workSafeBCNumber", "234567");
      cy.fillFormEntry("#doingBusinessAs", "Doing");
      cy.fillFormEntry("#workSafeBCNumber", "1");

      cy.checkInputErrorMessage(
        "#workSafeBCNumber",
        "The WorkSafeBC has a 6 character limit"
      );
    });

    it("should validate doing business as", () => {
      cy.selectAutocompleteEntry(
        "#businessName",
        "cor",
        "C1234567",
        "@clientSearchCOR"
      );

      cy.wait("@clientDetailsC1234567");

      cy.get("#doingBusinessAs").should("exist").and("have.value", "");

      cy.fillFormEntry("#doingBusinessAs", "1".repeat(200));

      cy.checkInputErrorMessage(
        "#doingBusinessAs",
        "The doing business as has a 120 character limit"
      );

      cy.get("#doingBusinessAs").shadow().find("input").clear();

      cy.fillFormEntry("#doingBusinessAs", "lá");

      cy.checkInputErrorMessage(
        "#doingBusinessAs",
        "The doing business as can only contain: A-Z, a-z, 0-9, space or common symbols"
      );
    });

    it("should validate acronym", () => {
      cy.selectAutocompleteEntry(
        "#businessName",
        "cor",
        "C1234567",
        "@clientSearchCOR"
      );

      cy.wait("@clientDetailsC1234567");

      cy.get("#acronym").should("exist").and("have.value", "");

      cy.fillFormEntry("#acronym", "1".repeat(10));

      cy.checkInputErrorMessage(
        "#acronym",
        "The acronym has a 8 character limit"
      );

      cy.get("#acronym").shadow().find("input").clear();

      cy.fillFormEntry("#acronym", "láe");

      cy.checkInputErrorMessage(
        "#acronym",
        "The acronym can only contain: A-Z or 0-9"
      );

      cy.get("#acronym").shadow().find("input").clear();

      cy.fillFormEntry("#acronym", "I");
      
      cy.checkInputErrorMessage(
        "#acronym",
        "The acronym must contain at least 3 characters"
      );
    });

    it("should validate birthdate", () => {
      cy.selectAutocompleteEntry(
        "#businessName",
        "sol",
        "FM123456",
        "@clientSearchSOL"
      );
      cy.wait("@clientDetailsFM123456");

      cy.get("#birthdateYear").should("exist").and("have.value", "");
      cy.get("#birthdateMonth").should("exist").and("have.value", "");
      cy.get("#birthdateDay").should("exist").and("have.value", "");

      cy.fillFormEntry("#birthdateYear", "2021");
      cy.fillFormEntry("#birthdateMonth", "12");
      cy.fillFormEntry("#birthdateDay", "12");

      cy.get(".cds--form-requirement")
        .should("exist")
        .and("have.class", "field-error")
        .and(
          "include.text",
          "The applicant must be at least 19 years old to apply"
        );

      cy.wait(15);

      cy.get("#birthdateYear").shadow().find("input").clear();
      cy.get("div.frame-01").should("exist").click();
      cy.wait(15);

      cy.get(".cds--form-requirement")
        .should("exist")
        .and("have.class", "field-error")
        .and("include.text", "Date of birth must include a year");

      cy.fillFormEntry("#birthdateYear", "1970");
      cy.get("#birthdateMonth").shadow().find("input").clear();
      cy.get("div.frame-01").should("exist").click();
      cy.wait(15);

      cy.get(".cds--form-requirement")
        .should("exist")
        .and("have.class", "field-error")
        .and("include.text", "Date of birth must include a month");

      cy.fillFormEntry("#birthdateMonth", "12");
      cy.get("#birthdateDay").shadow().find("input").clear();
      cy.get("div.frame-01").should("exist").click();
      cy.wait(15);

      cy.get(".cds--form-requirement")
        .should("exist")
        .and("have.class", "field-error")
        .and("include.text", "Date of birth must include a day");
    });
  });

  const interceptClientsApi = (companySearch: string, companyCode: string) => {
    const detailsCode = (code: string) => {
      switch (code) {
        case "bcd":
          return 408;
        case "dup":
          return 409;
        case "cnf":
          return 404;
        default:
          return 200;
      }
    };

    //We need to intercept the client search for the scenario, as param 1
    cy.intercept("GET", `**/api/clients/name/${companySearch}`, {
      statusCode: 200,
      fixture: "clients/bcreg_ac_list2.json",
    }).as(`clientSearch${companySearch}`);

    //We load the fixture beforehand due to the different content types and extensions based on the response
    cy.fixture(
      `clients/bcreg_${companyCode}.${
        detailsCode(companySearch) === 200 ? "json" : "txt"
      }`,
      "utf-8"
    ).then((data) => {
      cy.log("data", data);
      cy.intercept("GET", `**/api/clients/${companyCode}`, {
        statusCode: detailsCode(companySearch),
        body: data,
        headers: {
          "content-type":
            detailsCode(companySearch) !== 200
              ? "text/plain"
              : "application/json",
        },
      }).as(`clientDetails${companySearch}`);
    });
  };

  describe("Scenario combinations", () => {
    beforeEach(function () {
      //The title contains some parameters that we need to extract
      const params: string[] = this.currentTest.title
        .split(":")[1]
        .trim()
        .replace(" should return ", " ")
        .split(" ");

      interceptClientsApi(params[0], params[1]);
      loginAndNavigateToStaffForm();
    });

    testCases.forEach((scenario) => {
      it(`${scenario.scenarioName} : ${scenario.companySearch} should return ${scenario.companyCode}`, () => {
        // Initially, only the client name and the info notification should exist
        cy.get("#businessName").should("exist");
        cy.get("cds-inline-notification").should("exist");

        //Just a check to make sure the fields are not visible
        cy.get(".read-only-box").should("not.exist");

        cy.get("cds-inline-notification#bcRegistrySearchNotification").should(
          "exist"
        );

        // Then, when a client is selected, the rest of the form should appear
        cy.selectAutocompleteEntry(
          "#businessName",
          scenario.companySearch,
          scenario.companyCode,
          `@clientSearch${scenario.companySearch}`
        );

        cy.wait(`@clientDetails${scenario.companySearch}`);

        if (scenario.showDuplicatedNotification) {
          cy.get("#businessName")
            .should("have.attr", "aria-invalid", "true")
            .should("have.attr", "invalid-text", "Client already exists");

          cy.get("#businessName").shadow().find("svg").should("exist");
        }

        cy.get("cds-inline-notification#bcRegistrySearchNotification").should(
          "not.exist"
        );

        if (scenario.showBcRegDownNotification) {
          cy.wait(`@clientDetails${scenario.companySearch}`);
          cy.wait(5000);
          cy.get("cds-inline-notification#bcRegistryDownNotification").should(
            "exist"
          );
        }

        if (scenario.showData) {
          /*
          This variable might be useful in the future to test the button Next gets enabled on
          success. But we'll probably need to fix FSADT1-1496 first.
          */
          // const success = Object.entries(scenario)
          //   .filter(
          //     ([key, value]) =>
          //       key.startsWith("show") && key.endsWith("Notification")
          //   )
          //   .map(([key, value]) => value)
          //   .every((value) => value === false);

          cy.get(
            ".read-only-box > cds-inline-notification#readOnlyNotification"
          ).should("exist");

          cy.get(`.read-only-box > #legalType > .title-group-01 > .label-02`)
            .should("exist")
            .and("have.text", "Type");

          cy.get(`.read-only-box > #legalType > .body-compact-01`)
            .should("exist")
            .and("have.text", scenario.type);

          cy.get(
            `.read-only-box > #registrationNumber > .title-group-01 > .label-02`
          )
            .should("exist")
            .and("have.text", "Registration number");

          cy.get(`.read-only-box > #registrationNumber > .body-compact-01`)
            .should("exist")
            .and("have.text", scenario.companyCode);

          if (scenario.showUnknowNotification) {
            cy.get(
              ".read-only-box > cds-inline-notification#unknownStandingNotification"
            ).should("exist");
            //TODO: check the text and style maybe?!
          }

          if (scenario.showNotGoodStandingNotification) {
            cy.get(
              ".read-only-box > cds-inline-notification#notGoodStandingNotification"
            ).should("exist");
            //TODO: check the text and style maybe?!
          }

          cy.get(`.read-only-box > #goodStanding > .title-group-01 > .label-02`)
            .should("exist")
            .and("have.text", "BC Registries standing");

          cy.get(
            `.read-only-box > #goodStanding > div.internal-grouping-01 > .body-compact-01`
          )
            .should("exist")
            .and("have.text", scenario.standing);
        }

        if (scenario.showBirthdate) {
          cy.get("#birthdate").should("be.visible");
        }

        if (scenario.showNotOwnedByPersonError) {
          cy.get("#businessName")
            .should("have.attr", "aria-invalid", "true")
            .should(
              "have.attr",
              "invalid-text",
              "This sole proprietor is not owned by a person"
            );

          cy.get("#businessName").shadow().find("svg").should("exist");
        }

        cy.get("#workSafeBCNumber").should("exist").and("have.value", "");
        cy.get("#doingBusinessAs").should(scenario.dba ? "not.exist" : "exist");
        cy.get("#acronym").should("exist").and("have.value", "");
      });
    });
  });

  describe("Synchronize rendered data with business name input value", () => {
    const scenarios = [
      {
        name: "the client type gets changed",
        action: () => {
          cy.selectFormEntry("#clientType", "Individual", false);
        },
        clientTypeChanged: true,
      },
      {
        name: "the business name gets manually changed",
        action: () => {
          cy.get("#businessName").shadow().find("input").type("{backspace}");
        },
      },
      {
        name: "the business name gets cleared",
        action: () => {
          cy.get("#businessName")
            .shadow()
            .find("#selection-button") // The X clear button
            .click();
        },
      },
    ];
    beforeEach(() => {
      const companySearch = "dup";
      const companyCode = "C7775745";
      interceptClientsApi(companySearch, companyCode);

      loginAndNavigateToStaffForm();

      // When a client is selected, the rest of the form should appear
      cy.selectAutocompleteEntry(
        "#businessName",
        companySearch,
        companyCode,
        `@clientSearch${companySearch}`
      );

      cy.get("cds-inline-notification#bcRegistrySearchNotification").should(
        "not.exist"
      );

      cy.get("#fuzzy-match-notification-global").should("be.visible");

      cy.get(
        ".read-only-box > cds-inline-notification#readOnlyNotification"
      ).should("exist");

      cy.get(".read-only-box > #legalType > .title-group-01 > .label-02")
        .should("exist")
        .and("have.text", "Type");

      cy.get(
        ".read-only-box > #registrationNumber > .title-group-01 > .label-02"
      )
        .should("exist")
        .and("have.text", "Registration number");

      cy.get(".read-only-box > #goodStanding > .title-group-01 > .label-02")
        .should("exist")
        .and("have.text", "BC Registries standing");

      cy.get("#workSafeBCNumber").should("exist").and("have.value", "");
      cy.get("#acronym").should("exist").and("have.value", "");
    });

    scenarios.forEach((scenario) => {
      it(`resets the form when ${scenario.name}`, () => {
        scenario.action();

        if (!scenario.clientTypeChanged) {
          cy.get("cds-inline-notification#bcRegistrySearchNotification").should(
            "be.visible"
          );
        }

        cy.get("#fuzzy-match-notification-global").should("not.exist");

        cy.get(
          ".read-only-box > cds-inline-notification#readOnlyNotification"
        ).should("not.exist");

        cy.get(
          ".read-only-box > #legalType > .title-group-01 > .label-02"
        ).should("not.exist");

        cy.get(
          ".read-only-box > #registrationNumber > .title-group-01 > .label-02"
        ).should("not.exist");

        cy.get(
          ".read-only-box > #goodStanding > .title-group-01 > .label-02"
        ).should("not.exist");

        cy.get("#workSafeBCNumber").should("not.exist");
        cy.get("#acronym").should("not.exist");
      });
    });
  });

  // See FSADT1-1511 (https://apps.nrs.gov.bc.ca/int/jira/browse/FSADT1-1511)
  describe("when the selected Client name is replaced from a Sole proprietorship to something else", () => {
    it("clears the Doing Business As field", () => {
      loginAndNavigateToStaffForm();

      cy.get("cds-inline-notification#bcRegistrySearchNotification").should(
        "exist"
      );

      const sppSearch = "spp";
      const sppCode = "FM123123";

      interceptClientsApi(sppSearch, sppCode);

      cy.selectAutocompleteEntry(
        "#businessName",
        sppSearch,
        sppCode,
        `@clientSearch${sppSearch}`
      );

      cy.get(`.read-only-box > #legalType`)
        .should("exist")
        .and("contains.text", "Sole Proprietorship");

      cy.clearFormEntry("#businessName");

      const cmpSearch = "cmp";
      const cmpCode = "C1231231";

      interceptClientsApi(cmpSearch, cmpCode);

      cy.selectAutocompleteEntry(
        "#businessName",
        cmpSearch,
        cmpCode,
        `@clientSearch${cmpSearch}`
      );

      cy.get(`.read-only-box > #legalType`)
        .should("exist")
        .and("contains.text", "Continued In Corporation");

      /*
      Doing Business As is cleared, instead of holding the name of the previously selected Sole
      proprietorship.
      */
      cy.get("#doingBusinessAs").should("exist").and("have.value", "");
    });
  });

  describe("when the selected Client name is a Sole proprietorship", () => {
    beforeEach(() => {
      loginAndNavigateToStaffForm();

      cy.get("cds-inline-notification#bcRegistrySearchNotification").should("exist");

      const sppSearch = "spp";
      const sppCode = "FM123123";

      interceptClientsApi(sppSearch, sppCode);

      cy.selectAutocompleteEntry("#businessName", sppSearch, sppCode, `@clientSearch${sppSearch}`);

      cy.get(".read-only-box > #legalType")
        .should("exist")
        .and("contains.text", "Sole Proprietorship");
    });
    it("should not enable the button Next while Date of birth is empty", () => {
      cy.get("[data-test='wizard-next-button']").find("button").should("be.disabled");
    });
    it("should enable the button Next when Date of birth is filled in", () => {
      cy.fillFormEntry("#birthdateYear", "2001");
      cy.fillFormEntry("#birthdateMonth", "10");
      cy.fillFormEntry("#birthdateDay", "25");
      cy.get("[data-test='wizard-next-button']").find("button").should("be.enabled");
    });
    describe("and there is an error on the Date of birth", () => {
      beforeEach(() => {
        cy.get("#birthdateYear").find("input").focus().blur();

        cy.contains("#birthdate + .field-error", "You must enter a date of birth");
      });
      it("enables the button Next when a new Client name from a different type gets selected", () => {
        cy.clearFormEntry("#businessName");

        const cmpSearch = "cmp";
        const cmpCode = "C1231231";

        interceptClientsApi(cmpSearch, cmpCode);

        cy.selectAutocompleteEntry(
          "#businessName",
          cmpSearch,
          cmpCode,
          `@clientSearch${cmpSearch}`,
        );

        cy.get(".read-only-box > #legalType")
          .should("exist")
          .and("contains.text", "Continued In Corporation");

        cy.get("[data-test='wizard-next-button']").find("button").should("be.enabled");
      });
    });
  });

  describe("when the selected Client name is not a Sole proprietorship and there is an error on the Doing business as", () => {
    beforeEach(() => {
      loginAndNavigateToStaffForm();

      cy.get("cds-inline-notification#bcRegistrySearchNotification").should("exist");

      const cmpSearch = "cmp";
      const cmpCode = "C1231231";

      interceptClientsApi(cmpSearch, cmpCode);

      cy.selectAutocompleteEntry("#businessName", cmpSearch, cmpCode, `@clientSearch${cmpSearch}`);

      cy.get(".read-only-box > #legalType")
        .should("exist")
        .and("contains.text", "Continued In Corporation");

      cy.fillFormEntry("#doingBusinessAs", "Enchanté");

      cy.checkInputErrorMessage("#doingBusinessAs", "The doing business as can only contain");
    });
    it("enables the button Next when a new Client name with type Sole proprietorship gets selected", () => {
      cy.clearFormEntry("#businessName");

      const sppSearch = "spp";
      const sppCode = "FM123123";

      interceptClientsApi(sppSearch, sppCode);

      cy.selectAutocompleteEntry("#businessName", sppSearch, sppCode, `@clientSearch${sppSearch}`);

      cy.get(".read-only-box > #legalType")
        .should("exist")
        .and("contains.text", "Sole Proprietorship");

      cy.fillFormEntry("#birthdateYear", "2001");
      cy.fillFormEntry("#birthdateMonth", "10");
      cy.fillFormEntry("#birthdateDay", "23");

      cy.get("[data-test='wizard-next-button']").find("button").should("be.enabled");
    });
  });

  const loginAndNavigateToStaffForm = () => {
    cy.visit("/");

    cy.get("#landing-title").should(
      "contain",
      "Forests Client Management System"
    );

    cy.get("#landing-subtitle").should(
      "contain",
      "Create and manage client accounts"
    );

    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_ADMIN"],
    });

    // Check if the Create client button is visible
    cy.get("#menu-list-staff-form").should("be.visible").click();

    cy.get("h1").should("be.visible").should("contain", " Create client ");

    cy.get("#clientType").should("be.visible").and("have.value", "");
    cy.selectFormEntry("#clientType", "BC registered business", false);
  };
});
