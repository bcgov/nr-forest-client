import FormStaffPage from "@/pages/FormStaffPage.vue";

const individualBaseData = {
  firstName: "John",
  middleName: "Michael",
  lastName: "Silver",
  birthdateYear: "2001",
  birthdateMonth: "05",
  birthdateDay: "30",
  identificationTypeCode: "PASS",
  identificationTypeValue: "Canadian passport",
  identificationProvinceValue: undefined,
  clientIdentification: "AB345678",
};

const fillIndividual = (data = individualBaseData) => {
  cy.get("#firstName").find("input").type(data.firstName);
  cy.get("#middleName").find("input").type(data.middleName);
  cy.get("#lastName").find("input").type(data.lastName);
  cy.get("#birthdateYear").find("input").type(data.birthdateYear);
  cy.get("#birthdateMonth").find("input").type(data.birthdateMonth);
  cy.get("#birthdateDay").find("input").type(data.birthdateDay);

  cy.get("#identificationType").find("[part='trigger-button']").click();
  cy.wait(2000);
  cy.get("#identificationType")
    .find(`cds-combo-box-item[data-id="${data.identificationTypeCode}"]`)
    .should('exist')
    .should('be.visible')
    .then(($el) => {
      if ($el.length > 0) {
        cy.wrap($el).click();
      } else {
        throw new Error('Element not found');
      }
    });

  cy.get("#clientIdentification").find("input").clear();
  cy.get("#clientIdentification").find("input").should('be.focused').blur();
  cy.get("#clientIdentification").find("input").type(data.clientIdentification);
  cy.get("#clientIdentification").find("input").should('be.focused').blur();
};

describe("Step 2 - Locations", () => {
  it("should render the LocationsWizardStep with maxLocations set to 25", () => {
    cy.mount(FormStaffPage, {}).its("wrapper").as("wrapper");

    cy.get("#clientType").find("[part='trigger-button']").click();

    cy.get("#clientType").find('cds-combo-box-item[data-id="I"]').click();

    fillIndividual();

    cy.get("[data-test='wizard-next-button']").click();

    cy.get("@wrapper")
      .then((wrapper) => {
        const locations = wrapper.findComponent({ name: "LocationsWizardStep" });
        return locations.props("maxLocations");
      })
      .should("eq", 25);
  });
});
