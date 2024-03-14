/* eslint-disable no-undef */
describe("General Form", () => {
  beforeEach(() => {
    cy.intercept("GET", "/api/districts?page=0&size=250", {
      fixture: "districts.json",
    }).as("getDistricts");

    cy.intercept("http://localhost:8080/api/clients/name/*", {
      fixture: "business.json",
    }).as("searchCompany");

    cy.intercept("GET", "/api/clients/XX9016140", {
      fixture: "example.json",
    }).as("selectCompany");
  });

  it("should render the component", () => {
    cy.visit("/");
    cy.get("#landing-title").should("contain", "Forest Client Management System");

    cy.get("#landing-subtitle").should(
      "contain",
      "Create and manage client accounts"
    );
  });

  it("should render the form", () => {
    cy.visit("/");
    cy.wait(500);

    cy.login("uattest@forest.client", "Uat Test", "bceidbusiness");

    cy.wait("@getDistricts");

    cy.get("#district")
      .should("be.visible")
      .and("have.value", "")
      .find("[part='trigger-button']")
      .click();

    cy.get("#district")
      .find('cds-combo-box-item[data-id="DCC"]')
      .should("be.visible")
      .click()
      .and("have.value", "DCC - Cariboo-Chilcotin Natural Resource District");

    cy.get("#business").should("not.exist");

    cy.get("cds-inline-notification").should("not.exist");

    cy.get(
      '[label-text="I have a BC registered business (corporation, sole proprietorship, society, etc.)"]'
    )
      .should("be.visible")
      .click();

    cy.get("cds-inline-notification").should("be.visible");

    cy.get("#business")
      .should("be.visible")
      .shadow()
      .find("input")
      .should("have.value", "")
      .type("Valid Company");
    cy.wait("@searchCompany");

    cy.get('cds-combo-box-item[data-id="XX9016140"]').click();

    cy.wait("@selectCompany");

    cy.get("#birthdate").should("be.visible");

    cy.get("#birthdateYear").shadow().find("input").should("have.value", "").type("2001");
    cy.get("#birthdateMonth").shadow().find("input").should("have.value", "").type("05");
    cy.get("#birthdateDay").shadow().find("input").should("have.value", "").type("30");

    cy.get('[data-test="wizard-next-button"]').should("be.visible").click();

    cy.logout();
  });

  describe("Progress Indicator", () => {
    type Orientation = "horizontal" | "vertical";
    beforeEach(() => {
      cy.visit("/");
      cy.wait(500);

      cy.login("uattest@forest.client", "Uat Test", "bceidbusiness");
    });
    [
      {
        viewportWidth: 400,
        orientation: "vertical",
      },
      {
        viewportWidth: 671,
        orientation: "vertical",
      },
      {
        viewportWidth: 672,
        orientation: "horizontal",
      },
      {
        viewportWidth: 1200,
        orientation: "horizontal",
      },
    ].forEach(({ viewportWidth, orientation }) => {
      describe(`when viewport width is ${viewportWidth}`, () => {
        before(() => {
          const viewportHeight = 600;
          cy.viewport(viewportWidth, viewportHeight);
        });
        it(`should display steps ${orientation}ly`, () => {
          cy.contains("Step 1").as("step1");
          cy.contains("Step 2").as("step2");
          cy.contains("Step 3").as("step3");
          cy.contains("Step 4").as("step4");

          cy.getMany(["@step1", "@step2", "@step3", "@step4"]).then(
            ([...elements]) => {
              const rects = (elements as JQuery<HTMLElement>[]).map((el) => {
                return el.get(0).getBoundingClientRect();
              });
              if (orientation === "horizontal") {
                expect(rects[0].top)
                  .to.eq(rects[1].top)
                  .and.eq(rects[2].top)
                  .and.eq(rects[3].top);

                expect(rects[0].left).to.be.lessThan(rects[1].left);
                expect(rects[1].left).to.be.lessThan(rects[2].left);
                expect(rects[2].left).to.be.lessThan(rects[3].left);
              } else {
                expect(rects[0].left)
                  .to.eq(rects[1].left)
                  .and.eq(rects[2].left)
                  .and.eq(rects[3].left);

                expect(rects[0].top).to.be.lessThan(rects[1].top);
                expect(rects[1].top).to.be.lessThan(rects[2].top);
                expect(rects[2].top).to.be.lessThan(rects[3].top);
              }
            }
          );
        });
      });
    });
  });
});
