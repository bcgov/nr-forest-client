import type { ClientDetails } from "@/dto/CommonTypesDto";
import SummaryView from "@/pages/client-details/SummaryView.vue";

describe("<summary-view />", () => {
  const getDefaultProps = () => ({
    data: {
      registryCompanyTypeCode: "SP",
      corpRegnNmbr: "88888888",
      clientNumber: "4444",
      clientName: "Dunder Mifflin Paper Company",
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
  };

  const testFieldHidden = (selector: string) => {
    cy.get(selector).should("not.exist");
  };

  it("renders the SummaryView component", () => {
    mount();

    testField("#clientNumber", currentProps.data.clientNumber);
    testField("#acronym", currentProps.data.clientAcronym);
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

    testField("#dataOfBirth", currentProps.data.birthdate);
    testField("#status", currentProps.data.clientStatusDesc);
    testField("#notes", currentProps.data.clientComment);
  });

  it("hides fields optional fields when they are empty", () => {
    const data: ClientDetails = {
      ...getDefaultProps().data,
      registryCompanyTypeCode: "",
      corpRegnNmbr: "",
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
    testFieldHidden("#dataOfBirth");
    testFieldHidden("#notes");
  });
});
