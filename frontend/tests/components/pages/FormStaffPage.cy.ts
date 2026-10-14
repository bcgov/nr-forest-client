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
  cy.fillFormEntry("#firstName",data.firstName);
  cy.fillFormEntry("#middleName",data.middleName);
  cy.fillFormEntry("#lastName",data.lastName);
  cy.fillFormEntry("#birthdateYear",data.birthdateYear);
  cy.fillFormEntry("#birthdateMonth",data.birthdateMonth);
  cy.fillFormEntry("#birthdateDay",data.birthdateDay);
  cy.selectFormEntry("#identificationType",data.identificationTypeValue,false);
  cy.fillFormEntry("#clientIdentification",data.clientIdentification);
};

describe("Step 2 - Locations", () => {

  beforeEach(() => {
    cy.intercept("GET", "/api/codes/countries?page=0&size=250", {
      fixture: "countries.json",
    }).as("getCountries");

    cy.intercept("GET", "/api/codes/countries/CA/provinces?page=0&size=250", {
      fixture: "provinces.json",
    }).as("getProvinces");

    cy.intercept("GET", "/api/codes/countries/US/provinces?page=0&size=250", {
      fixture: "states.json",
    }).as("getStates");

    cy.intercept("GET", "/api/codes/identification-types", {
      fixture: "identificationTypes.json",
    }).as("getIdentificationTypes");

    cy.intercept("POST", "/api/clients/matches", {
      statusCode: 204,
    }).as("doMatches");

    cy.viewport(1056, 800);
  });

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
