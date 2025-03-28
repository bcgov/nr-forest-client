import type { Contact } from "@/dto/ApplyClientNumberDto";
import type { ClientContact, ClientLocation } from "@/dto/CommonTypesDto";
import ContactView from "@/pages/client-details/ContactView.vue";
import { contactToCreateFormat, formatPhoneNumber } from "@/services/ForestClientService";
import type { VueWrapper } from "@vue/test-utils";

describe("<contact-view />", () => {
  const validation = () => {};
  const validationWrapper = () => validation;

  const getDefaultProps = () => ({
    data: {
      contactId: 0,
      locationCodes: ["01"],
      contactName: "Cheryl Bibby",
      contactTypeCode: "BL",
      contactTypeDesc: "Billing",
      businessPhone: "2502863767",
      secondaryPhone: "2505553700",
      faxNumber: "2502863768",
      emailAddress: "cheryl@ktb.com",
    } as ClientContact,
    index: 0,
    associatedLocationsString: "01 - Town office",
    allLocations: [
      {
        clientLocnCode: "00",
        clientLocnName: "Headquarters",
      },
      {
        clientLocnCode: "01",
        clientLocnName: "Town office",
      },
    ] as ClientLocation[],
    userRoles: ["CLIENT_EDITOR"],
    validations: [validationWrapper],
    isReloading: false,
    createMode: false,
  });

  let currentProps: ReturnType<typeof getDefaultProps> = null;
  const mount = (props = getDefaultProps()) => {
    currentProps = props;
    return cy
      .mount(ContactView, { props, })
      .its("wrapper")
      .as("vueWrapper");
  };

  beforeEach(() => {
    cy.intercept("GET", "**/api/codes/contact-types?page=0&size=250", {
      fixture: "roles.json",
    }).as("getContactTypes");
  });

  const testField = (selector: string, value: string, linkPrefix?: string) => {
    cy.get(selector).should("be.visible");
    cy.get(selector).contains(value);
    expect(value.length).to.be.greaterThan(0);

    if (linkPrefix) {
      cy.get(selector).within(() => {
        cy.get("a").should("have.attr", "href", `${linkPrefix}${value}`);
      });
    }
  };

  const testFieldHidden = (selector: string) => {
    cy.get(selector).should("not.exist");
  };

  it("renders the ClientContact component", () => {
    mount();

    const emailPrefix = "mailto:";

    cy.get("#contact-0-general-section").within(() => {
      testField("#contact-0-contactType", currentProps.data.contactTypeDesc);
      testField("#contact-0-associatedLocations", currentProps.associatedLocationsString);
      testField("#contact-0-emailAddress", currentProps.data.emailAddress, emailPrefix);
    });

    const phonePrefix = "tel:";
    cy.get("#contact-0-phone-section").within(() => {
      testField(
        "#contact-0-primaryPhoneNumber",
        formatPhoneNumber(currentProps.data.businessPhone),
        phonePrefix,
      );
      testField(
        "#contact-0-secondaryPhoneNumber",
        formatPhoneNumber(currentProps.data.secondaryPhone),
        phonePrefix,
      );
      testField("#contact-0-fax", formatPhoneNumber(currentProps.data.faxNumber), phonePrefix);
    });
  });

  it("hides sections when they are empty", () => {
    const data: ClientContact = {
      ...getDefaultProps().data,
      businessPhone: "",
      secondaryPhone: "",
      faxNumber: "",
    };
    mount({
      ...getDefaultProps(),
      data,
    });
    cy.get("#contact-0-phone-section").should("not.exist");
  });

  it("hides general fields when they are empty", () => {
    const data: ClientContact = {
      ...getDefaultProps().data,
      emailAddress: "",
    };
    mount({
      ...getDefaultProps(),
      data,
    });

    testFieldHidden("#contact-0-emailAddress");
  });

  describe("while there is at least one phone to be displayed", () => {
    const scenarios = [
      ["businessPhone", "#contact-0-primaryPhoneNumber"],
      ["secondaryPhone", "#contact-0-secondaryPhoneNumber"],
      ["faxNumber", "#contact-0-fax"],
    ];

    scenarios.forEach((scenario) => {
      const [propName, selector] = scenario;
      const otherPhonesList = scenarios.filter((cur) => cur[0] !== propName);
      describe(propName, () => {
        beforeEach(() => {
          const data: ClientContact = {
            ...getDefaultProps().data,
            businessPhone: "",
            secondaryPhone: "",
            faxNumber: "",
            [propName]: "2505559876",
          };
          mount({
            ...getDefaultProps(),
            data,
          });
        });
        it(`displays the one phone with value: ${propName}`, () => {
          cy.get("#contact-0-phone-section").within(() => {
            testField(selector, formatPhoneNumber(currentProps.data[propName]));
          });
        });
        it("hides the other empty phones", () => {
          otherPhonesList.forEach((cur) => {
            const [, curSelector] = cur;
            testFieldHidden(curSelector);
          });
        });
      });
    });
  });

  it("makes the content invisible when isReloading is true", () => {
    const props = getDefaultProps();
    props.isReloading = true;
    mount(props);

    cy.get("div.grouping-12").should("have.class", "invisible");
  });

  const testInputTag = (inputTag: string, rawSelector: string, value?: string) => {
    const selector = `${inputTag}${rawSelector}`;
    cy.get(selector).should("be.visible");
    if (value !== undefined) {
      cy.get(selector).should("have.value", value);
    }
  };

  const testTextInput = (rawSelector: string, value?: string) =>
    testInputTag("cds-text-input", rawSelector, value);

  const testComboBox = (rawSelector: string, value?: string) =>
    testInputTag("cds-combo-box", rawSelector, value);

  describe("when the edit button in clicked", () => {
    let customProps: ReturnType<typeof getDefaultProps>;
    const defaultInit = () => {
      customProps = getDefaultProps();
    };
    before(() => {
      defaultInit();
    });
    beforeEach(() => {
      mount(customProps);

      if (!customProps.createMode) {
        cy.get(`#contact-${customProps.data.contactId}-EditBtn`).click();
      }
    });

    it("enables the edition of some fields by displaying the staff-contact-group-component", () => {
      cy.get<VueWrapper>("@vueWrapper").should((vueWrapper) => {
        const staffCreateComponent = vueWrapper.getComponent({
          name: "staff-contact-group-component",
        });

        expect(staffCreateComponent.props("id")).to.eq(0);

        const staffCreateData: Contact = staffCreateComponent.props("modelValue");

        expect(staffCreateData).to.deep.eq(
          contactToCreateFormat(currentProps.data, currentProps.allLocations),
        );
      });
    });

    it("disables the Save button by default", () => {
      cy.get("#contact-0-SaveBtn").shadow().find("button").should("be.disabled");
    });

    it("enables the Save button once something gets changed", () => {
      cy.clearFormEntry("#emailAddress_0");
      cy.fillFormEntry("#emailAddress_0", "upd@ted.com");
      cy.get("#contact-0-SaveBtn").shadow().find("button").should("be.enabled");
    });

    it("disables the Save button again if values are restored to their original values", () => {
      cy.clearFormEntry("#emailAddress_0");
      cy.fillFormEntry("#emailAddress_0", "upd@ted.com");
      cy.get("#contact-0-SaveBtn").shadow().find("button").should("be.enabled");
      cy.fillFormEntry("#emailAddress_0", currentProps.data.emailAddress);
      cy.get("#contact-0-SaveBtn").shadow().find("button").should("be.disabled");
    });

    it("restores original values if the Cancel button gets clicked", () => {
      // Change some values
      cy.clearFormEntry("#fullName_0");
      cy.fillFormEntry("#fullName_0", "Changed name");
      cy.selectFormEntry("#role_0", "Robot");
      cy.clearFormEntry("#emailAddress_0");
      cy.fillFormEntry("#emailAddress_0", "upd@ted.com");

      // Cancel
      cy.get("#contact-0-CancelBtn").click();

      // Click to edit again
      cy.get("#contact-0-EditBtn").click();

      // Check changed values were restored on the form
      testTextInput("#fullName_0", currentProps.data.contactName);
      testComboBox("#role_0", currentProps.data.contactTypeDesc);
      testTextInput("#emailAddress_0", currentProps.data.emailAddress);
    });

    const booleanValues = [false, true];
    booleanValues.forEach((createMode) => {
      describe(`createMode: ${createMode}`, () => {
        before(() => {
          customProps.createMode = createMode;
        });
        after(() => {
          defaultInit();
        });
        it("emits a save event when the Save button gets clicked", () => {
          // Change some information
          cy.clearFormEntry("#emailAddress_0");
          cy.fillFormEntry("#emailAddress_0", "upd@ted.com");

          cy.get("#contact-0-SaveBtn").click();

          cy.get("@vueWrapper").should((vueWrapper) => {
            const saveData = vueWrapper.emitted("save")[0][0];

            const { patch, updatedData } = saveData;

            if (createMode) {
              expect(patch).to.eq(null);
            } else {
              expect(patch).to.be.an("array");
              expect(patch).to.have.lengthOf(1);
              expect(patch[0].op).to.eq("replace");
            }

            // Contains the contact data as edited/created by the user
            expect(updatedData).to.deep.eq({
              ...customProps.data,
              emailAddress: "upd@ted.com",
            });
          });
        });
      });
    });

    it("forwards validations to the staff-location-group-component", () => {
      cy.get<VueWrapper>("@vueWrapper").should((vueWrapper) => {
        const staffCreateComponent = vueWrapper.getComponent({
          name: "staff-contact-group-component",
        });

        expect(staffCreateComponent.props("validations")[0]).to.eq(validationWrapper);
      });
    });

    it("mounts the staff-location-group-component with showLocationCode: true ", () => {
      cy.get<VueWrapper>("@vueWrapper").should((vueWrapper) => {
        const staffCreateComponent = vueWrapper.getComponent({
          name: "staff-contact-group-component",
        });

        expect(staffCreateComponent.props("showLocationCode")).to.eq(true);
      });
    });

    describe("and the Delete button is clicked", () => {
      beforeEach(() => {
        cy.get("#contact-0-DeleteBtn").click();
      });

      it("displays a confirmation dialog when Delete is clicked", () => {
        cy.get("#modal-delete").should("be.visible");
      });

      it("emits a delete event when the intention to Delete is confirmed", () => {
        cy.get("#modal-delete .cds--modal-submit-btn").click();

        cy.get("@vueWrapper").should((vueWrapper) => {
          const deleteData = vueWrapper.emitted("delete")[0][0];

          expect(deleteData).to.deep.eq(customProps.data);
        });
      });
    });
  });
});