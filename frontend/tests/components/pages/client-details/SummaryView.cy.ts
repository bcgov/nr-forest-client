import type { ClientDetails } from "@/dto/CommonTypesDto";
import SummaryView from "@/pages/client-details/SummaryView.vue";

describe("<summary-view />", () => {
  const getDefaultProps = () => ({
    data: {
      registryCompanyTypeCode: "SP",
      corpRegnNmbr: "88888888",
      clientNumber: "4444",
      clientName: "Scott",
      legalFirstName: "Michael",
      legalMiddleName: "Gary",
      doingBusinessAs: [{ doingBusinessAsName: "Dunder Mifflin Paper Company" }],
      birthdate: "1962-08-17",
      clientAcronym: "DMPC",
      clientTypeCode: "RSP",
      clientTypeDesc: "Registered sole proprietorship",
      goodStandingInd: "Y",
      clientStatusCode: "A",
      clientStatusDesc: "Active",
      clientComment:
        "Email from Michael Scott to request any letters for sec deposits be mailed to 3000, 28th St, Scranton",
      wcbFirmNumber: "7777777",
      clientIdTypeDesc: "British Columbia Driver's Licence",
      clientIdentification: "64242646",
    } as ClientDetails,
  });

  let currentProps: ReturnType<typeof getDefaultProps> = null;
  const mount = (props = getDefaultProps()) => {
    currentProps = props;
    return cy
      .mount(SummaryView, {
        props,
      })
      .its("wrapper")
      .as("vueWrapper");
  };

  const testField = (selector: string, value: string) => {
    cy.get(selector).should("be.visible");
    cy.get(selector).contains(value);
    expect(value.length).to.be.greaterThan(0);
  };

  const testFieldHidden = (selector: string) => {
    cy.get(selector).should("not.exist");
  };

  it("renders the SummaryView component", () => {
    mount();

    testField("#clientNumber", currentProps.data.clientNumber);
    testField("#acronym", currentProps.data.clientAcronym);
    testField("#doingBusinessAs", currentProps.data.doingBusinessAs[0].doingBusinessAsName);
    testField("#clientType", currentProps.data.clientTypeDesc);

    // registryCompanyTypeCode + corpRegnNmbr
    testField(
      "#registrationNumber",
      `${currentProps.data.registryCompanyTypeCode}${currentProps.data.corpRegnNmbr}`,
    );

    testField("#goodStanding", "Good standing");

    // identification Label
    testField("#identification", currentProps.data.clientIdTypeDesc);
    // identification Value
    testField("#identification", currentProps.data.clientIdentification);

    testField("#dateOfBirth", currentProps.data.birthdate);
    testField("#status", currentProps.data.clientStatusDesc);
    testField("#notes", currentProps.data.clientComment);
  });

  it("hides optional fields when they are empty", () => {
    const data: ClientDetails = {
      ...getDefaultProps().data,
      registryCompanyTypeCode: "",
      corpRegnNmbr: "",
      doingBusinessAs: [],
      birthdate: "",
      clientAcronym: "",
      goodStandingInd: null,
      clientComment: "",
      clientIdTypeDesc: "",
      clientIdentification: "",
    };
    mount({ data });

    testFieldHidden("#acronym");
    testFieldHidden("#registrationNumber");
    testFieldHidden("#goodStanding");
    testFieldHidden("#identification");
    testFieldHidden("#doingBusinessAs");
    testFieldHidden("#dataOfBirth");
    testFieldHidden("#notes");
  });

  it("displays as many names the client has as Doing business as", () => {
    const { data } = getDefaultProps();
    data.doingBusinessAs[1] = { doingBusinessAsName: "Name #2" };
    data.doingBusinessAs[2] = { doingBusinessAsName: "Name #3" };

    mount({ data });

    testField("#doingBusinessAs", data.doingBusinessAs[0].doingBusinessAsName);
    testField("#doingBusinessAs", data.doingBusinessAs[1].doingBusinessAsName);
    testField("#doingBusinessAs", data.doingBusinessAs[2].doingBusinessAsName);
  });

  it("sets the birthdate label to 'Date of birth' when date is complete", () => {
    mount();

    cy.get("#dateOfBirth").contains("Date of birth");
  });

  it("sets the birthdate label to 'Year of birth' when date has only four digits", () => {
    const { data } = getDefaultProps();
    data.birthdate = "1985";

    mount({ data });

    cy.get("#dateOfBirth").contains("Year of birth");
  });
});
