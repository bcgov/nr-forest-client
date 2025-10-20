import type { ClientDetails } from "@/dto/CommonTypesDto";

// For some reason this import is needed during tests
import "@carbon/web-components/es/components/button/index";

import SummaryView from "@/pages/client-details/SummaryView.vue";

const formatData = (data: ClientDetails) => {
  data.client.birthdate = data.client.birthdate.substring(0, 10);
  return data;
};

describe("<summary-view />", () => {
  const getDefaultProps = () => ({
    data: {
      client: {
        registryCompanyTypeCode: "BRT",
        corpRegnNmbr: "88888888",
        clientNumber: "4444",
        clientName: "Scott",
        legalFirstName: "Michael",
        legalMiddleName: "Gary",
        birthdate: "1962-08-17T00:00",
        clientAcronym: "DMPC",
        clientTypeCode: "I",
        clientTypeDesc: "Registered sole proprietorship",
        goodStandingInd: "Y",
        clientStatusCode: "ACT",
        clientStatusDesc: "Active",
        clientComment:
          "Email from Michael Scott to request any letters for sec deposits be mailed to 3000, 28th St, Scranton",
        wcbFirmNumber: "123456",
        clientIdTypeCode: "BCDL",
        clientIdTypeDesc: "British Columbia Drivers Licence",
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

  const testComboBox = (rawSelector: string, value?: string) =>
    testInputTag("cds-combo-box", rawSelector, value);

  const testTextarea = (rawSelector: string, value?: string) =>
    testInputTag("cds-textarea", rawSelector, value);

  const getClientStatusesBaseUrl = "/api/codes/client-statuses";
  beforeEach(() => {
    cy.intercept("GET", `${getClientStatusesBaseUrl}/*`, {
      fixture: "clientStatuses.json",
    }).as("getClientStatuses");
    cy.intercept("GET", "/api/codes/client-types/legacy", {
      fixture: "legacyClientTypes.json",
    }).as("getRegistryTypes");
    cy.intercept("GET", "/api/codes/registry-types/*", {
      fixture: "registryTypes.json",
    }).as("getRegistryTypes");
    cy.intercept("GET", "/api/codes/identification-types/legacy", {
      fixture: "legacyIdentificationTypes.json",
    }).as("getLegacyIdentificationTypes");
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

    // identification Label
    testReadonly("#identification", currentProps.data.client.clientIdTypeDesc);
    // identification Value
    testReadonly("#identification", currentProps.data.client.clientIdentification);

    testReadonly("#dateOfBirth", currentProps.data.client.birthdate.substring(0, 10));
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
    testHidden("#dateOfBirth");
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

  const testDateOfBirthEmpty = (userRoles: string[]) => {
    describe("and the Date of birth is empty", () => {
      const props = getDefaultProps();
      props.data.client.birthdate = null;
      props.userRoles = userRoles;

      beforeEach(() => {
        mount(props);
        cy.get("#summaryEditBtn").click();
        cy.wait("@getClientStatuses");
      });

      it("enables the edition of Date of birth", () => {
        testTextInput("#input-birthdateYear", "");
        testTextInput("#input-birthdateMonth", "");
        testTextInput("#input-birthdateDay", "");
      });

      it("stops displaying the Date of birth read-only field", () => {
        testHidden("#dateOfBirth");
      });

      it("doesn't require a Date of birth as a mandatory field", () => {
        cy.get("#summarySaveBtn").shadow().find("button").should("be.disabled");
        cy.clearFormEntry("#input-workSafeBCNumber");

        /*
        Date of birth is still empty, but the Save button gets enabled after changing something
        else.
        */
        cy.get("#summarySaveBtn").shadow().find("button").should("be.enabled");
      });
    });
  };

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
        testHidden("#input-registryType");
        testHidden("#input-registryNumber");
        testHidden("#input-birthdateYear");
        testHidden("#input-clientIdType");
        testHidden("#input-clientIdentification");
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

        testReadonly("#identification", currentProps.data.client.clientIdentification);
        testReadonly("#dateOfBirth", currentProps.data.client.birthdate.substring(0, 10));

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

          const { patch, updatedData } = saveData;

          expect(patch).to.be.an("array");
          expect(patch).to.have.lengthOf(1);

          expect(patch[0].op).to.eq("replace");

          const expectedData = formatData(structuredClone(props.data));

          expectedData.client.wcbFirmNumber = null;

          // Contains the client data as edited by the user
          expect(updatedData).to.deep.eq(expectedData);
        });
      });
    });

    testDateOfBirthEmpty(props.userRoles);

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

    testDateOfBirthEmpty(props.userRoles);

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

  describe("when role contains CLIENT_ADMIN", () => {
    const props = getDefaultProps();
    props.userRoles = ["CLIENT_ADMIN"];

    const clientTypes1 = [
      {
        code: "F",
        desc: "Ministry of Forests and Range",
      },
      {
        code: "G",
        desc: "Government",
      },
      {
        code: "R",
        desc: "First Nation Group",
      },
    ];

    // An arbitrary type among the group that contains types: B, F and G
    const firstNationClientType = clientTypes1[0];

    const ministryClientType = clientTypes1[1];

    const bcRegisteredTypes = [
      { code: "A", desc: "Association" },
      { code: "C", desc: "Corporation" },
      { code: "L", desc: "Limited Partnership" },
      { code: "P", desc: "General Partnership" },
      { code: "S", desc: "Society" },
      { code: "U", desc: "Unregistered Company" },
    ];

    const companyLikeTypes = [
      ...bcRegisteredTypes,
      { code: "B", desc: "First Nation Band" },
      { code: "T", desc: "First Nation Tribal Council" },
    ];

    const associationClientType = companyLikeTypes[0];

    const corporationClientType = companyLikeTypes[1];

    const individualClientType = { code: "I", desc: "Individual" };

    const itEnablesTheEditionOfTheBasicFields = () =>
      it('enables the edition of the "basic" fields', () => {
        testTextInput("#input-acronym", props.data.client.clientAcronym);
        testComboBox("#input-clientType", props.data.client.clientTypeDesc);
        testTextInput("#input-workSafeBCNumber", props.data.client.wcbFirmNumber);
        testDropdown("#input-clientStatus", props.data.client.clientStatusDesc);
        testTextarea("[data-id='input-input-notes']", props.data.client.clientComment);
      });

    const itEnablesTheEditionOfTheClientName = () =>
      it("also enables the edition of the Client name", () => {
        testTextInput("#input-clientName", props.data.client.clientName);
      });

    const itEnablesTheEditionOfTheDoingBusinessAs = () =>
      it("also enables the edition of the Doing business as", () => {
        testTextInput("#input-doingBusinessAs", props.data.doingBusinessAs);
      });

    // Higher order function to prepare for a specific client type change
    const changeClientType = (desc: string) => () => {
      cy.selectFormEntry("#input-clientType", desc);
    };

    const itReloadsTheClientStatusList = (changeType: () => void) =>
      it("reloads the client status list", () => {
        changeType();
        cy.wait("@getClientStatuses");
      });

    const itReloadsTheRegistryTypesList = (changeType: () => void) =>
      it("reloads the registry types list", () => {
        changeType();
        cy.wait("@getRegistryTypes");
      });

    const itAddsTheRegistrationNumberInputs = (changeType: () => void) =>
      it("adds the Registration number inputs (Type and Number) to the form", () => {
        testHidden("#input-registryType");
        testHidden("#input-registryNumber");

        changeType();

        testComboBox("#input-registryType");
        testTextInput("#input-registryNumber");
      });

    const itHidesTheRegistrationNumberInputs = (changeType: () => void) =>
      it("hides the Registration number inputs (Type and Number)", () => {
        testComboBox("#input-registryType");
        testTextInput("#input-registryNumber");

        changeType();

        testHidden("#input-registryType");
        testHidden("#input-registryNumber");
      });

    const itDeletesTheRegistrationNumberValues = () =>
      it("deletes the Registration Type and the Registration Number values", () => {
        cy.get("#summarySaveBtn").click();

        cy.get("@vueWrapper").should((vueWrapper) => {
          const saveData = vueWrapper.emitted("save")[0][0];

          const { patch, updatedData } = saveData;

          expect(patch).to.be.an("array");

          expect(patch).to.deep.include({
            op: "replace",
            path: "/client/registryCompanyTypeCode",
            value: null,
          });

          expect(patch).to.deep.include({
            op: "replace",
            path: "/client/corpRegnNmbr",
            value: null,
          });

          const expectedData = formatData(structuredClone(props.data));

          expectedData.client.registryCompanyTypeCode = null;
          expectedData.client.corpRegnNmbr = null;

          // skips the check on the clientTypeCode
          delete expectedData.client.clientTypeCode;
          delete updatedData.client.clientTypeCode;

          // Contains the client data as edited by the user
          expect(updatedData).to.deep.eq(expectedData);
        });
      });

    const itAddsIndividualFields = (changeType: () => void) =>
      it("adds the fields that are only concerned to Individuals", () => {
        testHidden("#input-legalFirstName");
        testHidden("#input-legalMiddleName");
        cy.get("#input-clientName").contains("Client name");
        testHidden("#input-birthdateYear");
        testHidden("#input-clientIdType");
        testHidden("#input-clientIdentification");

        changeType();

        testTextInput("#input-legalFirstName");
        testTextInput("#input-legalMiddleName");
        cy.get("#input-clientName").contains("Last name");
        testTextInput("#input-birthdateYear");
        testComboBox("#input-clientIdType");
        testTextInput("#input-clientIdentification");
      });

    const itHidesIndividualFields = (changeType: () => void) =>
      it("hides the fields that are only concerned to Individuals", () => {
        testTextInput("#input-legalFirstName");
        testTextInput("#input-legalMiddleName");
        cy.get("#input-clientName").contains("Last name");
        testTextInput("#input-birthdateYear");
        testComboBox("#input-clientIdType");
        testTextInput("#input-clientIdentification");

        changeType();

        testHidden("#input-legalFirstName");
        testHidden("#input-legalMiddleName");
        cy.get("#input-clientName").contains("Client name");
        testHidden("#input-birthdateYear");
        testHidden("#input-clientIdType");
        testHidden("#input-clientIdentification");
      });

    const itDeletesIndividualFieldsValues = () =>
      it("deletes the values on the fields that are only concerned to Individuals", () => {
        cy.get("#summarySaveBtn").click();

        cy.get("@vueWrapper").should((vueWrapper) => {
          const saveData = vueWrapper.emitted("save")[0][0];

          const { patch, updatedData } = saveData;

          expect(patch).to.be.an("array");

          expect(patch).to.deep.include({
            op: "replace",
            path: "/client/legalFirstName",
            value: null,
          });

          expect(patch).to.deep.include({
            op: "replace",
            path: "/client/legalMiddleName",
            value: null,
          });

          expect(patch).to.deep.include({
            op: "replace",
            path: "/client/birthdate",
            value: null,
          });

          expect(patch).to.deep.include({
            op: "replace",
            path: "/client/clientIdTypeCode",
            value: null,
          });

          expect(patch).to.deep.include({
            op: "replace",
            path: "/client/clientIdentification",
            value: null,
          });

          const expectedData = formatData(structuredClone(props.data));

          expectedData.client.legalFirstName = null;
          expectedData.client.legalMiddleName = null;
          expectedData.client.birthdate = null;
          expectedData.client.clientIdTypeCode = null;
          expectedData.client.clientIdentification = null;

          // skips the check on the clientTypeCode
          delete expectedData.client.clientTypeCode;
          delete updatedData.client.clientTypeCode;

          // Contains the client data as edited by the user
          expect(updatedData).to.deep.eq(expectedData);
        });
      });

    describe(`when client type in (${clientTypes1.map((clientType) => `"${clientType.code} - ${clientType.desc}"`).join(", ")})`, () => {
      clientTypes1.forEach((clientType) => {
        describe(`more especifically ${clientType.code} - ${clientType.desc}`, () => {
          describe("and the edit button in clicked", () => {
            beforeEach(() => {
              props.data.client.clientTypeCode = clientType.code;
              props.data.client.clientTypeDesc = clientType.desc;
              mount(props);
              cy.get("#summaryEditBtn").click();
              cy.wait("@getClientStatuses");
            });

            itEnablesTheEditionOfTheBasicFields();

            itEnablesTheEditionOfTheClientName();

            itEnablesTheEditionOfTheDoingBusinessAs();

            it("disables the edition of everything else", () => {
              testHidden("#input-registryType");
              testHidden("#input-registryNumber");
              testHidden("#input-birthdateYear");
              testHidden("#input-clientIdType");
              testHidden("#input-clientIdentification");
            });

            it("keeps displaying the other fields in view mode", () => {
              testReadonly("#clientNumber", currentProps.data.client.clientNumber);
              testReadonly("#clientType", currentProps.data.client.clientTypeDesc);

              // registryCompanyTypeCode + corpRegnNmbr
              testReadonly(
                "#registrationNumber",
                `${currentProps.data.client.registryCompanyTypeCode}${currentProps.data.client.corpRegnNmbr}`,
              );

              testReadonly("#identification", currentProps.data.client.clientIdentification);
              testReadonly("#dateOfBirth", currentProps.data.client.birthdate.substring(0, 10));

              // Make sure the fields enabled for edition are not also displayed in read-only mode.
              testHidden("#acronym");
              testHidden("#doingBusinessAs");
              testHidden("#workSafeBCNumber");
              testHidden("#clientStatus");
              testHidden("#notes");
            });

            it("hides the BC Registries standing field", () => {
              testHidden("#goodStanding");
            });
          });
        });
      });

      describe("and the client type is changed to some other type in the same group", () => {
        beforeEach(() => {
          props.data.client.clientTypeCode = firstNationClientType.code;
          props.data.client.clientTypeDesc = firstNationClientType.desc;
          mount(props);
          cy.get("#summaryEditBtn").click();
          cy.wait("@getClientStatuses");
        });

        const changeType = changeClientType(ministryClientType.desc);

        itReloadsTheClientStatusList(changeType);
      });

      describe("and the client type is changed to a company-like type", () => {
        beforeEach(() => {
          props.data.client.clientTypeCode = firstNationClientType.code;
          props.data.client.clientTypeDesc = firstNationClientType.desc;
          mount(props);
          cy.get("#summaryEditBtn").click();
          cy.wait("@getClientStatuses");
        });

        const changeType = changeClientType(associationClientType.desc);

        itReloadsTheClientStatusList(changeType);

        itAddsTheRegistrationNumberInputs(changeType);
      });

      describe("and the client type is changed to the Individual type", () => {
        beforeEach(() => {
          props.data.client.clientTypeCode = firstNationClientType.code;
          props.data.client.clientTypeDesc = firstNationClientType.desc;
          mount(props);
          cy.get("#summaryEditBtn").click();
          cy.wait("@getClientStatuses");
        });

        const changeType = changeClientType(individualClientType.desc);

        itReloadsTheClientStatusList(changeType);

        itAddsIndividualFields(changeType);
      });
    });

    describe("Company-like types", () => {
      companyLikeTypes.forEach((clientType) => {
        describe(`when client type is ${clientType.code} - ${clientType.desc}`, () => {
          describe("when the edit button in clicked", () => {
            beforeEach(() => {
              props.data.client.clientTypeCode = clientType.code;
              props.data.client.clientTypeDesc = clientType.desc;
              mount(props);
              cy.get("#summaryEditBtn").click();
              cy.wait("@getClientStatuses");
            });

            itEnablesTheEditionOfTheBasicFields();

            itEnablesTheEditionOfTheClientName();

            itEnablesTheEditionOfTheDoingBusinessAs();

            it("also enables the edition of the Registration number (Type and Number)", () => {
              const registryTypeDesc = "Bogus Registry Type";
              const formattedTypeName = `${currentProps.data.client.registryCompanyTypeCode} - ${registryTypeDesc}`;
              testComboBox("#input-registryType", formattedTypeName);
              testTextInput("#input-registryNumber", props.data.client.corpRegnNmbr);
            });

            it("disables the edition of everything else", () => {
              testHidden("#input-birthdate");
              testHidden("#input-clientIdType");
              testHidden("#input-clientIdentification");
            });

            it("keeps displaying the other fields in view mode", () => {
              testReadonly("#clientNumber", currentProps.data.client.clientNumber);
              testReadonly("#clientType", currentProps.data.client.clientTypeDesc);
              testReadonly("#identification", currentProps.data.client.clientIdentification);
              testReadonly("#dateOfBirth", currentProps.data.client.birthdate.substring(0, 10));

              // Make sure the fields enabled for edition are not also displayed in read-only mode.
              testHidden("#acronym");
              testHidden("#doingBusinessAs");
              testHidden("#registrationNumber");
              testHidden("#workSafeBCNumber");
              testHidden("#clientStatus");
              testHidden("#notes");
            });

            if (bcRegisteredTypes.find((type) => type.code === clientType.code)) {
              it("shows the BC Registries standing field", () => {
                testReadonly("#goodStanding", "Good standing");
              });
            } else {
              it("hides the BC Registries standing field", () => {
                testHidden("#goodStanding");
              });
            }
          });
        });
      });

      describe("and the client type is changed to some other company-like type", () => {
        beforeEach(() => {
          props.data.client.clientTypeCode = associationClientType.code;
          props.data.client.clientTypeDesc = associationClientType.desc;
          mount(props);
          cy.get("#summaryEditBtn").click();
          cy.wait("@getClientStatuses");
        });

        const changeType = changeClientType(corporationClientType.desc);

        itReloadsTheClientStatusList(changeType);

        itReloadsTheRegistryTypesList(changeType);
      });

      describe(`and the client type is changed to any type among (${clientTypes1.map((clientType) => `"${clientType.code} - ${clientType.desc}"`).join(", ")})`, () => {
        beforeEach(() => {
          props.data.client.clientTypeCode = associationClientType.code;
          props.data.client.clientTypeDesc = associationClientType.desc;
          mount(props);
          cy.get("#summaryEditBtn").click();
          cy.wait("@getClientStatuses");
        });

        const changeType = changeClientType(firstNationClientType.desc);

        itReloadsTheClientStatusList(changeType);

        itHidesTheRegistrationNumberInputs(changeType);

        describe("and the Save button is clicked", () => {
          beforeEach(() => {
            changeType();
          });

          itDeletesTheRegistrationNumberValues();
        });
      });

      describe("and the client type is changed to the Individual type", () => {
        beforeEach(() => {
          props.data.client.clientTypeCode = associationClientType.code;
          props.data.client.clientTypeDesc = associationClientType.desc;
          mount(props);
          cy.get("#summaryEditBtn").click();
          cy.wait("@getClientStatuses");
        });

        const changeType = changeClientType(individualClientType.desc);

        itReloadsTheClientStatusList(changeType);

        itHidesTheRegistrationNumberInputs(changeType);

        itAddsIndividualFields(changeType);

        describe("and the Save button is clicked", () => {
          beforeEach(() => {
            changeType();
          });

          itDeletesTheRegistrationNumberValues();
        });
      });
    });

    describe(`when client type is ${individualClientType.code} - ${individualClientType.desc}`, () => {
      describe("when the edit button in clicked", () => {
        beforeEach(() => {
          props.data.client.clientTypeCode = individualClientType.code;
          props.data.client.clientTypeDesc = individualClientType.desc;
          mount(props);
          cy.get("#summaryEditBtn").click();
          cy.wait("@getClientStatuses");
        });

        itEnablesTheEditionOfTheBasicFields();

        it("also enables the edition of the name parts", () => {
          testTextInput("#input-legalFirstName", props.data.client.legalFirstName);
          testTextInput("#input-legalMiddleName", props.data.client.legalMiddleName);
          testTextInput("#input-clientName", props.data.client.clientName);
        });

        itEnablesTheEditionOfTheDoingBusinessAs();

        it("also enables the edition of the Date of birth", () => {
          const dateObject = new Date(props.data.client.birthdate);
          testTextInput("#input-birthdateYear", String(dateObject.getFullYear()));
          testTextInput(
            "#input-birthdateMonth",
            String(dateObject.getMonth() + 1).padStart(2, "0"),
          );
          testTextInput("#input-birthdateDay", String(dateObject.getDate()).padStart(2, "0"));
        });

        it("also enables the edition of the ID fields", () => {
          testComboBox("#input-clientIdType", props.data.client.clientIdTypeDesc);
          testTextInput("#input-clientIdentification", props.data.client.clientIdentification);
        });

        it("disables the edition of everything else", () => {
          testHidden("#input-registryType");
          testHidden("#input-registryNumber");
        });

        it("keeps displaying the other fields in view mode", () => {
          testReadonly("#clientNumber", currentProps.data.client.clientNumber);
          testReadonly("#clientType", currentProps.data.client.clientTypeDesc);

          // Make sure the fields enabled for edition are not also displayed in read-only mode.
          testHidden("#acronym");
          testHidden("#doingBusinessAs");
          testHidden("#workSafeBCNumber");
          testHidden("#identification");
          testHidden("#dateOfBirth");
          testHidden("#clientStatus");
          testHidden("#notes");
        });

        it("hides the BC Registries standing field", () => {
          testHidden("#goodStanding");
        });
      });

      describe(`and the client type is changed to any type among (${clientTypes1.map((clientType) => `"${clientType.code} - ${clientType.desc}"`).join(", ")})`, () => {
        beforeEach(() => {
          props.data.client.clientTypeCode = individualClientType.code;
          props.data.client.clientTypeDesc = individualClientType.desc;
          mount(props);
          cy.get("#summaryEditBtn").click();
          cy.wait("@getClientStatuses");
        });

        const changeType = changeClientType(firstNationClientType.desc);

        itReloadsTheClientStatusList(changeType);

        itHidesIndividualFields(changeType);

        describe("and the Save button is clicked", () => {
          beforeEach(() => {
            changeType();
          });

          itDeletesIndividualFieldsValues();
        });
      });

      describe("and the client type is changed to a company-like type", () => {
        beforeEach(() => {
          props.data.client.clientTypeCode = individualClientType.code;
          props.data.client.clientTypeDesc = individualClientType.desc;
          mount(props);
          cy.get("#summaryEditBtn").click();
          cy.wait("@getClientStatuses");
        });

        const changeType = changeClientType(associationClientType.desc);

        itReloadsTheClientStatusList(changeType);

        itHidesIndividualFields(changeType);

        itAddsTheRegistrationNumberInputs(changeType);

        describe("and the Save button is clicked", () => {
          beforeEach(() => {
            changeType();
          });

          itDeletesIndividualFieldsValues();
        });
      });
    });

    ["ACT", "SPN", "REC", "DAC", "DEC"].forEach((clientStatus) => {
      const props = getDefaultProps();
      props.userRoles = ["CLIENT_ADMIN"];
      props.data.client.clientStatusCode = clientStatus;
      props.data.client.clientStatusDesc = clientStatus;
      describe(`regardless of the current Client status: ${clientStatus}`, () => {
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
