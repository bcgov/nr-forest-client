/* eslint-disable no-undef */
describe("Staff Form", () => {
  beforeEach(() => {
    cy.visit("/");
    cy.wait(500);
    cy.get("#landing-title").should(
      "contain",
      "Forests Client Management System"
    );
    cy.get("#landing-subtitle").should(
      "contain",
      "Create and manage client accounts"
    );

    cy.viewport(1920, 1080);
  });

  it("CLIENT_EDITOR should be able to see the button", () => {
    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_EDITOR"],
    });

    // Check if the Create client button is visible
    cy.get("#menu-list-staff-form")
      .should("be.visible")
      .find("span")
      .should("contain", "Create client");
  });

  it("CLIENT_ADMIN should be able to see the button", () => {
    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_ADMIN"],
    });

    // Check if the Create client button is visible
    cy.get("#menu-list-staff-form")
      .should("be.visible")
      .find("span")
      .should("contain", "Create client");
  });

  it("CLIENT_VIEWER should not be able to see the button", () => {
    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_VIEWER"],
    });

    // Check if the Create client button is not visible and cannot be found
    cy.get("#menu-list-staff-form").should("not.exist");
  });

  it("CLIENT_EDITOR should be able to click the button", () => {
    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_EDITOR"],
    });

    // Check if the Create client button is visible
    cy.get("#menu-list-staff-form").should("be.visible").click();

    cy.get("h1").should("be.visible").should("contain", " Create client ");
  });

  it("CLIENT_ADMIN should be able to click the button", () => {
    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_ADMIN"],
    });

    // Check if the Create client button is visible
    cy.get("#menu-list-staff-form").should("be.visible").click();

    cy.get("h1").should("be.visible").should("contain", " Create client ");
  });

  describe("when the user clicks the Create client button", () => {
    beforeEach(() => {
      cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
        given_name: "James",
        family_name: "Baxter",
        "cognito:groups": ["CLIENT_ADMIN"],
      });

      // Check if the Create client button is visible
      cy.get("#menu-list-staff-form").should("be.visible").click();

      cy.intercept("GET", "/api/codes/countries/CA/provinces?page=0&size=250", {
        fixture: "provinces.json",
      }).as("getProvinces");

      cy.intercept(
        "GET",
        "/api/addresses?country=CA&maxSuggestions=10&searchTerm=*",
        {
          fixture: "addressSearch.json",
        }
      ).as("searchAddress");

      cy.intercept("GET", "/api/addresses/V8T5J9", {
        fixture: "address.json",
      }).as("getAddress");

      cy.intercept("GET", "/api/codes/contact-types?page=0&size=250", {
        fixture: "roles.json",
      }).as("getContactTypes");
    });

    it("should display the Client type input field", () => {
      cy.get("#clientType").should("be.visible").and("have.value", "");
    });

    it("should not display any Client type specific input fields", () => {
      cy.get("#firstName").should("not.exist");
    });

    const individualBaseData = {
      firstName: "John",
      middleName: "Michael",
      lastName: "Silver",
      birthdateYear: "2001",
      birthdateMonth: "05",
      birthdateDay: "30",
      identificationTypeValue: "Canadian passport",
      identificationProvinceValue: undefined,
      clientIdentification: "AB345678",
    };

    const fillIndividual = (data = individualBaseData) => {
      cy.get("#firstName").shadow().find("input").type(data.firstName);

      cy.get("#middleName").shadow().find("input").type(data.middleName);

      cy.get("#lastName").shadow().find("input").type(data.lastName);

      cy.get("#identificationType").find("[part='trigger-button']").click();
      cy.get("#identificationType")
        .find(
          `cds-combo-box-item[data-value="${data.identificationTypeValue}"]`
        )
        .click();

      if (data.identificationProvinceValue) {
        cy.get("#identificationProvince")
          .find("[part='trigger-button']")
          .click();
        cy.get("#identificationProvince")
          .find(
            `cds-combo-box-item[data-value="${data.identificationProvinceValue}"]`
          )
          .click();
      }

      cy.get("#clientIdentification")
        .shadow()
        .find("input")
        .type(data.clientIdentification);

      cy.get("#clientIdentification").shadow().find("input").blur();

      cy.get("#birthdateYear").shadow().find("input").type(data.birthdateYear);

      cy.get("#birthdateMonth")
        .shadow()
        .find("input")
        .type(data.birthdateMonth);

      cy.get("#birthdateDay").shadow().find("input").type(data.birthdateDay);
    };

    describe("when option Individual gets selected", () => {
      beforeEach(() => {
        cy.get("#clientType")
          .should("be.visible")
          .and("have.value", "")
          .find("[part='trigger-button']")
          .click();

        cy.get("#clientType")
          .find('cds-combo-box-item[data-id="I"]')
          .should("be.visible")
          .click()
          .and("have.value", "Individual");
      });

      it("should display the Individual information input fields", () => {
        cy.contains("h2", "Client information");
        cy.get("#firstName").should("be.visible");
        cy.get("#middleName").should("be.visible");
        cy.get("#lastName").should("be.visible");
        cy.get("#birthdate").should("be.visible");
        cy.get("#identificationType").should("be.visible");
        cy.get("#clientIdentification").should("be.visible");
      });

      describe("when all the required information is filled in", () => {
        const scenarios = [
          {
            name: "and the selected ID type doesn't require Issuing province",
            data: {
              ...individualBaseData,
            },
          },
          {
            name: "and the selected ID type requires Issuing province",
            data: {
              ...individualBaseData,
              identificationTypeValue: "Canadian driver's licence",
              identificationProvinceValue: "Nova Scotia",
            },
          },
        ];
        scenarios.forEach(({ name, data }) => {
          describe(name, () => {
            beforeEach(() => {
              fillIndividual(data);
            });
            it("enables the button Next", () => {
              cy.get("[data-test='wizard-next-button']")
                .shadow()
                .find("button")
                .should("be.enabled");
            });

            describe("and the button Next is clicked", () => {
              beforeEach(() => {
                cy.get("[data-test='wizard-next-button']").click();
              });
              it("hides the Client information section", () => {
                cy.contains("h2", "Client information").should("not.exist");
              });
              describe("and the button Back is clicked", () => {
                beforeEach(() => {
                  cy.get("[data-test='wizard-back-button']").click();
                });
                it("renders the Individual input fields with the same data", () => {
                  cy.get("#firstName")
                    .shadow()
                    .find("input")
                    .should("have.value", data.firstName);

                  cy.get("#middleName")
                    .shadow()
                    .find("input")
                    .should("have.value", data.middleName);

                  cy.get("#lastName")
                    .shadow()
                    .find("input")
                    .should("have.value", data.lastName);

                  cy.get("#birthdateYear")
                    .shadow()
                    .find("input")
                    .should("have.value", data.birthdateYear);
                  cy.get("#birthdateMonth")
                    .shadow()
                    .find("input")
                    .should("have.value", data.birthdateMonth);
                  cy.get("#birthdateDay")
                    .shadow()
                    .find("input")
                    .should("have.value", data.birthdateDay);

                  cy.get("#identificationType").should(
                    "have.value",
                    data.identificationTypeValue
                  );

                  if (data.identificationProvinceValue) {
                    cy.get("#identificationProvince").should(
                      "have.value",
                      data.identificationProvinceValue
                    );
                  }

                  cy.get("#clientIdentification")
                    .shadow()
                    .find("input")
                    .should("have.value", data.clientIdentification);
                });
              });
            });
          });
        });
      });
    });

    const locationBaseData = {
      name_0: "Headquarters",
      addr_0: "2975",
    };

    const fillLocation = (data = locationBaseData) => {
      cy.get("#name_0").shadow().find("input").type(data.name_0);

      cy.get("#addr_0").shadow().find("input").type(data.addr_0, { delay: 0 });

      cy.wait("@searchAddress");

      cy.get("#addr_0 cds-combo-box-item").contains(data.addr_0).click();

      cy.wait("@getAddress");
    };

    const fillFormEntry = (field: string, value: string) => {
      cy.get(field).should("exist").shadow().find("input").type(value);

      cy.get(field).shadow().find("input").blur();
    };

    const selectFormEntry = (field: string, value: string, box: boolean) => {
      cy.get(field).find("[part='trigger-button']").click();

      if (!box) {
        cy.get(field).find(`cds-combo-box-item[data-value="${value}"]`).click();
      } else {
        cy.get(field)
          .find(`cds-multi-select-item[data-value="${value}"]`)
          .click();
        cy.get(field).click();
      }
    };

    const contactBaseData = {
      mail: "contact1@mail.ca",
      phone1: "1234567890",
      phone2: "1234567890",
      fax: "1234567890",
      role: "Person",
      address: locationBaseData.name_0,
    };

    const fillContact = (data = contactBaseData) => {
      fillFormEntry("#emailAddress_0", data.mail);
      fillFormEntry("#businessPhoneNumber_0", data.phone1);
      fillFormEntry("#secondaryPhoneNumber_0", data.phone2);
      fillFormEntry("#faxNumber_0", data.fax);

      cy.wait("@getContactTypes");

      selectFormEntry("#role_0", data.role, false);
      selectFormEntry("#addressname_0", data.address, true);
    };

    describe("locations step", () => {
      beforeEach(() => {
        cy.get("#clientType")
          .should("be.visible")
          .and("have.value", "")
          .find("[part='trigger-button']")
          .click();

        cy.get("#clientType")
          .find('cds-combo-box-item[data-id="I"]')
          .should("be.visible")
          .click()
          .and("have.value", "Individual");

        fillIndividual();

        cy.get("[data-test='wizard-next-button']").click();
      });

      it("displays the Locations section", () => {
        cy.contains("h2", "Locations");
      });

      describe("when all the required fields are filled in", () => {
        beforeEach(() => {
          cy.contains("h2", "Locations");
          fillLocation();
        });

        describe("and there is a simple input validation error", () => {
          beforeEach(() => {
            // Add invalid character to the city
            cy.get("#city_0").shadow().find("input").type("é");
          });

          describe("and the user goes back to the Client information step and returns to the Locations step", () => {
            beforeEach(() => {
              cy.get("[data-test='wizard-back-button']").click();
              cy.contains("h2", "Client information");
              cy.get("[data-test='wizard-next-button']").click();
              cy.contains("h2", "Locations");
            });

            describe("and the user attempts to go to the Next step without fixing the invalid character", () => {
              beforeEach(() => {
                cy.get("[data-test='wizard-next-button']").click();
              });

              it("should display an error message at the top of the page", () => {
                cy.wait(10);
                cy.contains("h2", "Locations");
                
                cy.get("#city_0")
                  .find("input")
                  .should("have.class", "cds--text-input--invalid");
              });
            });
          });
        });

        describe("and there is an error due to a bad combination of the values provided in the two input fields related to delivery information", () => {
          beforeEach(() => {
            cy.contains("Add more delivery information").click();
          });

          describe("and the user goes back to the Client information step and returns to the Locations step", () => {
            beforeEach(() => {
              cy.get("[data-test='wizard-back-button']").click();
              cy.contains("h2", "Client information");
              cy.get("[data-test='wizard-next-button']").click();
              cy.contains("h2", "Locations");
            });

            it("should prevent the user from moving to the next step by disabling the next button", () => {
              cy.get("[data-test='wizard-next-button']")
                .shadow()
                .find("button")
                .should("be.disabled");

              cy.get("#complementaryAddressOne_0")
                .find("input")
                .should("have.class", "cds--text-input--invalid");
            });
          });
        });
      });
    });

    describe("contacts step", () => {
      beforeEach(() => {
        cy.get("#clientType")
          .should("be.visible")
          .and("have.value", "")
          .find("[part='trigger-button']")
          .click();

        cy.get("#clientType")
          .find('cds-combo-box-item[data-id="I"]')
          .should("be.visible")
          .click()
          .and("have.value", "Individual");

        fillIndividual();

        cy.get("[data-test='wizard-next-button']").click();

        cy.contains("h2", "Locations");
        fillLocation();

        cy.get("[data-test='wizard-next-button']").click();
      });

      it("displays the Contacts section", () => {
        cy.contains("h2", "Contacts");
      });

      describe("when all the required fields are filled in", () => {
        beforeEach(() => {
          cy.contains("h2", "Contacts");
          fillContact();
        });

        describe("and there is a simple input validation error", () => {
          beforeEach(() => {
            cy.get("#emailAddress_0").shadow().find("input").type("é");
          });

          describe("and the user goes back to the Client information step and returns to the Contacts step", () => {
            beforeEach(() => {
              cy.get("[data-test='wizard-back-button']").click();
              cy.contains("h2", "Locations");
              cy.get("[data-test='wizard-back-button']").click();
              cy.contains("h2", "Client information");
              cy.get("[data-test='wizard-next-button']").click();
              cy.contains("h2", "Locations");
              cy.get("[data-test='wizard-next-button']").click();
              cy.contains("h2", "Contacts");
            });

            describe("and the user attempts to go to the Next step without fixing the invalid character", () => {
              beforeEach(() => {
                cy.get("[data-test='wizard-next-button']").click();
              });

              it("should display an error message at the top of the page", () => {
                cy.wait(10);
                cy.contains("h2", "Contacts");
        
                cy.get("#emailAddress_0")
                  .find("input")
                  .should("have.class", "cds--text-input--invalid");
              });
            });
          });
        });
      });
    });

    describe("review step", () => {
      beforeEach(() => {
        cy.get("#clientType")
          .should("be.visible")
          .and("have.value", "")
          .find("[part='trigger-button']")
          .click();

        cy.get("#clientType")
          .find('cds-combo-box-item[data-id="I"]')
          .should("be.visible")
          .click()
          .and("have.value", "Individual");

        fillIndividual({
          ...individualBaseData,
          identificationTypeValue: "Canadian driver's licence",
          identificationProvinceValue: "Nova Scotia",
          clientIdentification: "12345678"
        });
        cy.get("[data-test='wizard-next-button']").click();

        cy.contains("h2", "Locations");
        fillLocation();
        cy.get("[data-test='wizard-next-button']").click();
        
        cy.contains("h2", "Contacts");
        fillContact();
        cy.get("[data-test='wizard-next-button']").click();
      });

      it("displays the Review section", () => {
        cy.contains("h2", "Review");
      });

      it("should allow notes to be added", () => {
        cy.get("cds-textarea")
        .shadow()
        .find("textarea")
        .type("This is a note!");

        cy.get("cds-textarea")
        .shadow()
        .find(".cds--text-area__label-wrapper")
        .should("contain", "15/4000");
        
        // Even if I try to add more than 4k characters, it will stop at 4k
        cy.get("cds-textarea")
        .shadow()
        .find("textarea")
        .type("A".repeat(4010), {delay: 0});
        
        cy.get("cds-textarea")
        .shadow()
        .find(".cds--text-area__label-wrapper")
        .should("contain", "4000/4000");

      });

    });

  });
});
