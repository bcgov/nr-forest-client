import UnauthorizedErrorPage from "@/pages/UnauthorizedErrorPage.vue";

describe("UnauthorizedErrorPage", () => {
  let routeName = "anything";
  beforeEach(() => {
    cy.mount(UnauthorizedErrorPage, {
      global: {
        config: {
          globalProperties: {
            $route: {
              name: routeName,
            },
          },
        },
      },
    })
      .its("wrapper")
      .as("wrapper");
  });

  it("shows the error banner with a link", () => {
    cy.get("cds-actionable-notification").contains(
      "You are not authorized to access this system or page",
    );
    cy.get("cds-actionable-notification").should("have.attr", "kind", "error");
    cy.get("cds-actionable-notification").find("a");
  });

  describe("when route name is unauthorized-idir-role", () => {
    before(() => {
      routeName = "unauthorized-idir-role";
    });

    it("displays #screen with class 'no-menu-content'", () => {
      cy.get("#screen").should("have.attr", "class", "no-menu-content");
    });
  });

  describe("when route name is anything else", () => {
    before(() => {
      routeName = "anything";
    });

    it("displays #screen with class 'idir-content'", () => {
      cy.get("#screen").should("have.attr", "class", "idir-content");
    });
  });
});
