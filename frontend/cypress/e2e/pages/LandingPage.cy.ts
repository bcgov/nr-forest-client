describe("LandingPage", () => {
  const init = (features = {}) => {
    cy.addToLocalStorage("VITE_FEATURE_FLAGS", features);
    cy.visit("/");
  };

  it("hides dev buttons if their feature flags are disabled", () => {
    init();

    cy.get("cds-button").its("length").should("eq", 1);
    cy.get("cds-button").contains("Log in with IDIR");
  });

  it("displays the dev buttons if their feature flags are enabled", () => {
    init({
      BCEID_LOGIN: true,
      BCSC_LOGIN: true,
      ROLE_LOGIN: true,
    });

    cy.get("cds-button").its("length").should("eq", 4);
  });
});
