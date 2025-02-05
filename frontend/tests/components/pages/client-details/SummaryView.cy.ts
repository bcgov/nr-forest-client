import type { ClientDetails } from "@/dto/CommonTypesDto";

// For some reason this import is needed during tests
import "@carbon/web-components/es/components/button/index";

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
      clientStatusCode: "ACT",
      clientStatusDesc: "Active",
      clientComment:
        "Email from Michael Scott to request any letters for sec deposits be mailed to 3000, 28th St, Scranton",
      wcbFirmNumber: "123456",
      clientIdTypeDesc: "British Columbia Driver's Licence",
      clientIdentification: "64242646",
    } as ClientDetails,
    userRole: "CLIENT_VIEWER",
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

  const testReadonly = (rawSelector: string, value: string) => {
    const selector = `div${rawSelector}`;
    cy.get(selector).should("be.visible");
    cy.get(selector).contains(value);
    expect(value.length).to.be.greaterThan(0);
  };

  const testHidden = (selector: string) => {
    cy.get(selector).should("not.exist");
  };

  const testInputTag = (inputTag: string, rawSelector: string, value: string) => {
    const selector = `${inputTag}${rawSelector}`;
    cy.get(selector).should("be.visible");
    cy.get(selector).should("have.value", value);
  }

  const testTextInput = (rawSelector: string, value: string) =>
    testInputTag("cds-text-input", rawSelector, value);

  const testDropdown = (rawSelector: string, value: string) =>
    testInputTag("cds-dropdown", rawSelector, value);

  const testTextarea = (rawSelector: string, value: string) =>
    testInputTag("cds-textarea", rawSelector, value);

  it("renders the SummaryView component", () => {
    mount();

    testReadonly("#clientNumber", currentProps.data.clientNumber);
    testReadonly("#acronym", currentProps.data.clientAcronym);
    testReadonly("#doingBusinessAs", currentProps.data.doingBusinessAs[0].doingBusinessAsName);
    testReadonly("#clientType", currentProps.data.clientTypeDesc);

    // registryCompanyTypeCode + corpRegnNmbr
    testReadonly(
      "#registrationNumber",
      `${currentProps.data.registryCompanyTypeCode}${currentProps.data.corpRegnNmbr}`,
    );

    testReadonly("#workSafeBCNumber", currentProps.data.wcbFirmNumber);

    testReadonly("#goodStanding", "Good standing");

    // identification Label
    testReadonly("#identification", currentProps.data.clientIdTypeDesc);
    // identification Value
    testReadonly("#identification", currentProps.data.clientIdentification);

    testReadonly("#dateOfBirth", currentProps.data.birthdate);
    testReadonly("#status", currentProps.data.clientStatusDesc);
    testReadonly("#notes", currentProps.data.clientComment);
  });

  it("hides optional fields when they are empty", () => {
    const props = getDefaultProps();
    props.data = {
      ...props.data,
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
    mount(props);

    testHidden("#acronym");
    testHidden("#registrationNumber");
    testHidden("#goodStanding");
    testHidden("#identification");
    testHidden("#doingBusinessAs");
    testHidden("#dataOfBirth");
    testHidden("#notes");
  });

  it("displays as many names the client has as Doing business as", () => {
    const props = getDefaultProps();
    const { data } = props;
    data.doingBusinessAs[1] = { doingBusinessAsName: "Name #2" };
    data.doingBusinessAs[2] = { doingBusinessAsName: "Name #3" };

    mount(props);

    testReadonly("#doingBusinessAs", data.doingBusinessAs[0].doingBusinessAsName);
    testReadonly("#doingBusinessAs", data.doingBusinessAs[1].doingBusinessAsName);
    testReadonly("#doingBusinessAs", data.doingBusinessAs[2].doingBusinessAsName);
  });

  it("sets the birthdate label to 'Date of birth' when date is complete", () => {
    mount();

    cy.get("#dateOfBirth").contains("Date of birth");
  });

  it("sets the birthdate label to 'Year of birth' when date's month and day are masked", () => {
    const props = getDefaultProps();
    const { data } = props;
    data.birthdate = "1985-**-**";

    mount(props);

    cy.get("#dateOfBirth").contains("Year of birth");
  });

  it("sets the birthdate label to 'Year of birth' when date has only four digits", () => {
    const props = getDefaultProps();
    const { data } = props;
    data.birthdate = "1985";

    mount(props);

    cy.get("#dateOfBirth").contains("Year of birth");
  });

  ["CLIENT_EDITOR", "CLIENT_SUSPEND", "CLIENT_ADMIN"].forEach((userRole) => {
    it(`displays the Edit button if userRole is: ${userRole}`, () => {
      const props = getDefaultProps();
      props.userRole = userRole;

      mount(props);

      cy.get("#summaryEditBtn").should("be.visible");
    });
  });

  ["CLIENT_VIEWER", "UNKNOWN", null].forEach((userRole) => {
    it(`hides the Edit button if userRole is: ${userRole}`, () => {
      const props = getDefaultProps();
      props.userRole = userRole;

      mount(props);

      cy.get("#summaryEditBtn").should("not.exist");
    });
  });

  describe("when role is CLIENT_EDITOR or CLIENT_SUSPEND", () => {
    const props = getDefaultProps();
    props.userRole = "CLIENT_EDITOR";
    beforeEach(() => {
      mount(props);
    });
    describe("when the edit button in clicked", () => {
      beforeEach(() => {
        cy.get("#summaryEditBtn").click();
      });

      it("enables the edition of some fields only", () => {
        testTextInput("#input-workSafeBCNumber", props.data.wcbFirmNumber);
        testDropdown("#input-clientStatus", props.data.clientStatusDesc);
        testTextarea("[data-id='input-input-notes']", props.data.clientComment);

        testHidden("#input-clientName");
        testHidden("#input-acronym");
        testHidden("#input-doingBusinessAs");
        testHidden("#input-clientType");
        testHidden("#input-registrationNumber");
        testHidden("#input-identification");
        testHidden("#input-dateOfBirth");
      });

      it("keeps displaying the other fields in view mode", () => {
        testReadonly("#clientNumber", currentProps.data.clientNumber);
        testReadonly("#acronym", currentProps.data.clientAcronym);
        testReadonly("#doingBusinessAs", currentProps.data.doingBusinessAs[0].doingBusinessAsName);
        testReadonly("#clientType", currentProps.data.clientTypeDesc);

        // registryCompanyTypeCode + corpRegnNmbr
        testReadonly(
          "#registrationNumber",
          `${currentProps.data.registryCompanyTypeCode}${currentProps.data.corpRegnNmbr}`,
        );

        testReadonly("#goodStanding", "Good standing");
        testReadonly("#identification", currentProps.data.clientIdentification);
        testReadonly("#dateOfBirth", currentProps.data.birthdate);

        // Make sure the fields enabled for edition are not also displayed in read-only mode.
        testHidden("#workSafeBCNumber");
        testHidden("#clientStatus");
        testHidden("#notes");
      });

      it("disables the Save button by default", () => {
        cy.get("#summarySaveBtn").shadow().find("button").should("be.disabled");
      });

      it("enables the Save button once something gets changed", () => {
        cy.clearFormEntry("#input-workSafeBCNumber");
        cy.get("#summarySaveBtn").shadow().find("button").should("be.enabled");
      });

      it("disables the Save button again if values are restored to their original values", () => {
        cy.clearFormEntry("#input-workSafeBCNumber");
        cy.get("#summarySaveBtn").shadow().find("button").should("be.enabled");
        cy.fillFormEntry("#input-workSafeBCNumber", "123456");
        cy.get("#summarySaveBtn").shadow().find("button").should("be.disabled");
      });

      it("restores original values if the Cancel button gets clicked", () => {
        // Change all values
        cy.clearFormEntry("#input-workSafeBCNumber");
        cy.selectFormEntry("#input-clientStatus", "Deactivated");
        cy.clearFormEntry("[data-id='input-input-notes']", true);

        // Cancel
        cy.get("#summaryCancelBtn").click();

        // Click to edit again
        cy.get("#summaryEditBtn").click();

        // Check values on the form
        testTextInput("#input-workSafeBCNumber", props.data.wcbFirmNumber);
        testDropdown("#input-clientStatus", props.data.clientStatusDesc);
        testTextarea("[data-id='input-input-notes']", props.data.clientComment);
      });
    });
  });
});
