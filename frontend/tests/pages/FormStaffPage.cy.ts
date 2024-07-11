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


  cy.get("#identificationType")
    .find(`cds-combo-box-item[data-id="${data.identificationTypeCode}"]`)
    .debug();


  cy.get("#identificationType")
    .find(`cds-combo-box-item[data-id="${data.identificationTypeCode}"]`)
    .should("be.visible")
    .debug()
    .click();

  cy.get("#clientIdentification").find("input").clear();
  cy.get("#clientIdentification").find("input").should('be.focused').blur();
  cy.get("#clientIdentification").find("input").type(data.clientIdentification);
  cy.get("#clientIdentification").find("input").should('be.focused').blur();
};

describe("Step 2 - Locations", () => {

  beforeEach(() => {
    cy.intercept("GET", "/api/codes/countries/CA/provinces?page=0&size=250", {
      fixture: "provinces.json",
    }).as("getProvinces");

    cy.intercept("GET", "/api/codes/countries/US/provinces?page=0&size=250", {
      fixture: "states.json",
    }).as("getStates");

    cy.intercept("GET", "/api/codes/identification-types", {
      fixture: "identificationTypes.json",
    }).as("getIdentificationTypes");
  });

  it("should render the LocationsWizardStep with maxLocations set to 25", () => {

    cy.viewport(1056, 800);

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
