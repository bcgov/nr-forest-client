import type { ClientDetails } from "@/dto/CommonTypesDto";

// For some reason this import is needed during tests
import "@carbon/web-components/es/components/button/index";

import SummaryView from "@/pages/client-details/SummaryView.vue";

describe("<summary-view />", () => {
  const getDefaultProps = () => ({
    data: {
      client: {
        registryCompanyTypeCode: "SP",
        corpRegnNmbr: "88888888",
        clientNumber: "4444",
        clientName: "Scott",
        legalFirstName: "Michael",
        legalMiddleName: "Gary",
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
      },
      doingBusinessAs: "Dunder Mifflin Paper Company",
    } as ClientDetails,
    userRoles: ["CLIENT_VIEWER"],
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

  const testReadonly = (rawSelector: string, value?: string) => {
    const selector = `div${rawSelector}`;
    cy.get(selector).should("be.visible");
    if (value !== undefined) {
      cy.get(selector).contains(value);
      expect(value.length).to.be.greaterThan(0);
    }
  };

  const testHidden = (selector: string) => {
    cy.get(selector).should("not.exist");
  };

  const testInputTag = (inputTag: string, rawSelector: string, value?: string) => {
    const selector = `${inputTag}${rawSelector}`;
    cy.get(selector).should("be.visible");
    if (value !== undefined) {
      cy.get(selector).should("have.value", value);
    }
  };

  const testTextInput = (rawSelector: string, value?: string) =>
    testInputTag("cds-text-input", rawSelector, value);

  const testDropdown = (rawSelector: string, value?: string) =>
    testInputTag("cds-dropdown", rawSelector, value);

  const testTextarea = (rawSelector: string, value?: string) =>
    testInputTag("cds-textarea", rawSelector, value);

  const getClientStatusesBaseUrl = "/api/codes/client-statuses";
  beforeEach(() => {
    cy.intercept("GET", `${getClientStatusesBaseUrl}/*`, {
      fixture: "clientStatuses.json",
    }).as("getClientStatuses");
  });

  it("renders the SummaryView component", () => {
    mount();

    testReadonly("#clientNumber", currentProps.data.client.clientNumber);
    testReadonly("#acronym", currentProps.data.client.clientAcronym);
    testReadonly("#doingBusinessAs", currentProps.data.doingBusinessAs);
    testReadonly("#clientType", currentProps.data.client.clientTypeDesc);

    // registryCompanyTypeCode + corpRegnNmbr
    testReadonly(
      "#registrationNumber",
      `${currentProps.data.client.registryCompanyTypeCode}${currentProps.data.client.corpRegnNmbr}`,
    );

    testReadonly("#workSafeBCNumber", currentProps.data.client.wcbFirmNumber);

    testReadonly("#goodStanding", "Good standing");

    // identification Label
    testReadonly("#identification", currentProps.data.client.clientIdTypeDesc);
    // identification Value
    testReadonly("#identification", currentProps.data.client.clientIdentification);

    testReadonly("#dateOfBirth", currentProps.data.client.birthdate);
    testReadonly("#clientStatus", currentProps.data.client.clientStatusDesc);
    testReadonly("#notes", currentProps.data.client.clientComment);
  });

  it("hides optional fields when they are empty", () => {
    const props = getDefaultProps();
    props.data = {
      ...props.data,
      client: {
        ...props.data.client,
        registryCompanyTypeCode: "",
        corpRegnNmbr: "",
        birthdate: "",
        clientAcronym: "",
        goodStandingInd: null,
        clientComment: "",
        clientIdTypeDesc: "",
        clientIdentification: "",
      },
      doingBusinessAs: null,
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

  it("sets the birthdate label to 'Date of birth' when date is complete", () => {
    mount();

    cy.get("#dateOfBirth").contains("Date of birth");
  });

  it("sets the birthdate label to 'Year of birth' when date's month and day are masked", () => {
    const props = getDefaultProps();
    const { data } = props;
    data.client.birthdate = "1985-**-**";

    mount(props);

    cy.get("#dateOfBirth").contains("Year of birth");
  });

  it("sets the birthdate label to 'Year of birth' when date has only four digits", () => {
    const props = getDefaultProps();
    const { data } = props;
    data.client.birthdate = "1985";

    mount(props);

    cy.get("#dateOfBirth").contains("Year of birth");
  });

  ["CLIENT_EDITOR", "CLIENT_SUSPEND", "CLIENT_ADMIN"].forEach((userRole) => {
    it(`displays the Edit button if userRole contains: ${userRole}`, () => {
      const props = getDefaultProps();
      props.userRoles = [userRole];

      mount(props);

      cy.get("#summaryEditBtn").should("be.visible");
    });
  });

  ["CLIENT_VIEWER", "UNKNOWN", null].forEach((userRole) => {
    it(`hides the Edit button if userRole contains only: ${userRole}`, () => {
      const props = getDefaultProps();
      props.userRoles = [userRole];

      mount(props);

      cy.get("#summaryEditBtn").should("not.exist");
    });
  });

  describe("when role contains CLIENT_EDITOR", () => {
    const props = getDefaultProps();
    props.userRoles = ["CLIENT_EDITOR"];
    describe("when the edit button in clicked", () => {
      let getClientStatusesRequest;
      beforeEach(() => {
        mount(props);
        cy.get("#summaryEditBtn").click();
        cy.wait("@getClientStatuses").then(({ request }) => {
          getClientStatusesRequest = request;
        });
      });

      it("enables the edition of some fields only", () => {
        testTextInput("#input-acronym", props.data.client.clientAcronym);
        testTextInput("#input-workSafeBCNumber", props.data.client.wcbFirmNumber);
        testDropdown("#input-clientStatus", props.data.client.clientStatusDesc);
        testTextarea("[data-id='input-input-notes']", props.data.client.clientComment);

        testHidden("#input-clientName");
        testHidden("#input-doingBusinessAs");
        testHidden("#input-clientType");
        testHidden("#input-registrationNumber");
        testHidden("#input-identification");
        testHidden("#input-dateOfBirth");
      });

      it("requests the client statuses according to the client type", () => {
        expect(getClientStatusesRequest.url).to.match(
          new RegExp(`${getClientStatusesBaseUrl}/${props.data.client.clientTypeCode}`),
        );
      });

      it("keeps displaying the other fields in view mode", () => {
        testReadonly("#clientNumber", currentProps.data.client.clientNumber);
        testReadonly("#doingBusinessAs", currentProps.data.doingBusinessAs);
        testReadonly("#clientType", currentProps.data.client.clientTypeDesc);

        // registryCompanyTypeCode + corpRegnNmbr
        testReadonly(
          "#registrationNumber",
          `${currentProps.data.client.registryCompanyTypeCode}${currentProps.data.client.corpRegnNmbr}`,
        );

        testReadonly("#goodStanding", "Good standing");
        testReadonly("#identification", currentProps.data.client.clientIdentification);
        testReadonly("#dateOfBirth", currentProps.data.client.birthdate);

        // Make sure the fields enabled for edition are not also displayed in read-only mode.
        testHidden("#acronym");
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
        testTextInput("#input-workSafeBCNumber", props.data.client.wcbFirmNumber);
        testDropdown("#input-clientStatus", props.data.client.clientStatusDesc);
        testTextarea("[data-id='input-input-notes']", props.data.client.clientComment);
      });

      it("emits a save event when the Save button gets clicked", () => {
        // Change some information
        cy.clearFormEntry("#input-workSafeBCNumber");

        cy.get("#summarySaveBtn").click();

        cy.get("@vueWrapper").should((vueWrapper) => {
          const saveData = vueWrapper.emitted("save")[0][0];

          expect(saveData).to.be.an("array");
          expect(saveData).to.have.lengthOf(1);

          expect(saveData[0].op).to.eq("replace");
        });
      });
    });

    ["SPN", "REC", "DAC", "DEC"].forEach((clientStatus) => {
      const props = getDefaultProps();
      props.userRoles = ["CLIENT_EDITOR"];
      props.data.client.clientStatusCode = clientStatus;
      props.data.client.clientStatusDesc = clientStatus;
      describe(`when current client status is: ${clientStatus}`, () => {
        beforeEach(() => {
          mount(props);
          cy.get("#summaryEditBtn").click();
        });

        it("locks the Client status field", () => {
          // Check we are in edit mode
          cy.get("#summarySaveBtn").should("be.visible");

          testHidden("#input-clientStatus");
          testReadonly("#clientStatus");
        });
      });
    });

    ["ACT"].forEach((clientStatus) => {
      const props = getDefaultProps();
      props.userRoles = ["CLIENT_EDITOR"];
      props.data.client.clientStatusCode = clientStatus;
      props.data.client.clientStatusDesc = clientStatus;
      describe(`when current client status is: ${clientStatus}`, () => {
        beforeEach(() => {
          mount(props);
          cy.get("#summaryEditBtn").click();
        });

        it("allows to update the Client status field", () => {
          testDropdown("#input-clientStatus");
        });
      });
    });
  });

  describe("when role contains CLIENT_SUSPEND", () => {
    const props = getDefaultProps();
    props.userRoles = ["CLIENT_SUSPEND"];

    ["REC", "DAC", "DEC"].forEach((clientStatus) => {
      const props = getDefaultProps();
      props.userRoles = ["CLIENT_SUSPEND"];
      props.data.client.clientStatusCode = clientStatus;
      props.data.client.clientStatusDesc = clientStatus;
      describe(`when current client status is: ${clientStatus}`, () => {
        beforeEach(() => {
          mount(props);
          cy.get("#summaryEditBtn").click();
        });

        it("locks the Client status field", () => {
          // Check we are in edit mode
          cy.get("#summarySaveBtn").should("be.visible");

          testHidden("#input-clientStatus");
          testReadonly("#clientStatus");
        });
      });
    });

    ["ACT", "SPN"].forEach((clientStatus) => {
      const props = getDefaultProps();
      props.userRoles = ["CLIENT_SUSPEND"];
      props.data.client.clientStatusCode = clientStatus;
      props.data.client.clientStatusDesc = clientStatus;
      describe(`when current client status is: ${clientStatus}`, () => {
        beforeEach(() => {
          mount(props);
          cy.get("#summaryEditBtn").click();
        });

        it("allows to update the Client status field", () => {
          testDropdown("#input-clientStatus");
        });
      });
    });
  });
});
