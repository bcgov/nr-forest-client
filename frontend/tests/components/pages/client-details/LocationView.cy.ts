import type { Address } from "@/dto/ApplyClientNumberDto";
import type { ClientLocation, ModalNotification } from "@/dto/CommonTypesDto";
import LocationView from "@/pages/client-details/LocationView.vue";
import { formatPhoneNumber, locationToCreateFormat } from "@/services/ForestClientService";

import { VueWrapper } from "@vue/test-utils";
import type { ComponentProps } from "vue-component-type-helpers";

import "@carbon/web-components/es/components/modal/index";
import { useEventBus } from "@vueuse/core";

describe("<location-view />", () => {
  type RawProps = ComponentProps<typeof LocationView>;

  type IfEquals<X, Y, A = X, B = never> =
    (<T>() => T extends X ? 1 : 2) extends <T>() => T extends Y ? 1 : 2 ? A : B;

  type WritableKeys<T> = {
    [P in keyof T]-?: IfEquals<{ [Q in P]: T[P] }, { -readonly [Q in P]: T[P] }, P>;
  }[keyof T];

  type ReadonlyKeys<T> = {
    [P in keyof T]-?: IfEquals<{ [Q in P]: T[P] }, { -readonly [Q in P]: T[P] }, never, P>;
  }[keyof T];

  type Mutable<T> = {
    -readonly [K in keyof T]: T[K];
  };

  type Props = Mutable<Pick<RawProps, ReadonlyKeys<RawProps>>>;

  const validation = () => {};
  const validationWrapper = () => validation;

  const getDefaultProps = (): Props => ({
    data: {
      clientLocnName: "Mailing address",
      clientLocnCode: "00",
      addressOne: "C/O Tony Pineda",
      addressTwo: "Sample additional info",
      addressThree: "886 Richmond Ave",
      countryCode: "CA",
      countryDesc: "Canada",
      provinceCode: "SK",
      provinceDesc: "Saskatchewan",
      city: "Hampton",
      postalCode: "T4G5J1",
      emailAddress: "contact@mail.com",
      businessPhone: "2502863767",
      cellPhone: "2505553700",
      homePhone: "2505553101",
      faxNumber: "2502863768",
      cliLocnComment: "Sample location 00 comment",
      locnExpiredInd: "N",
    } as ClientLocation,
    userRoles: ["CLIENT_EDITOR"],
    validations: [validationWrapper],
    isReloading: false,
  });

  let currentProps: ReturnType<typeof getDefaultProps> = null;
  const mount = (props = getDefaultProps()) => {
    currentProps = props;
    return cy
      .mount(LocationView, {
        props,
      })
      .its("wrapper")
      .as("vueWrapper");
  };

  beforeEach(() => {
    cy.intercept("GET", "/api/codes/countries?page=0&size=250", {
      fixture: "countries.json",
    }).as("getCountries");

    cy.intercept("GET", "/api/codes/countries/CA/provinces?page=0&size=250", {
      fixture: "provinces.json",
    }).as("getProvinces");
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

  it("renders the ClientLocation component", () => {
    mount();

    cy.get("#location-00-address-section").within(() => {
      testField("#location-00-addressOne", currentProps.data.addressOne);
      testField("#location-00-addressTwo", currentProps.data.addressTwo);
      testField("#location-00-addressThree", currentProps.data.addressThree);

      // City, Province
      testField(
        "#location-00-city-province",
        `${currentProps.data.city}, ${currentProps.data.provinceDesc}`,
      );

      testField("#location-00-country", currentProps.data.countryDesc);
      testField("#location-00-postalCode", currentProps.data.postalCode);
    });

    const emailPrefix = "mailto:";

    cy.get("#location-00-email-section").within(() => {
      testField("#location-00-emailAddress", currentProps.data.emailAddress, emailPrefix);
    });

    const phonePrefix = "tel:";

    cy.get("#location-00-phone-section").within(() => {
      testField(
        "#location-00-primaryPhoneNumber",
        formatPhoneNumber(currentProps.data.businessPhone),
        phonePrefix,
      );
      testField(
        "#location-00-secondaryPhoneNumber",
        formatPhoneNumber(currentProps.data.cellPhone),
        phonePrefix,
      );
      testField(
        "#location-00-tertiaryPhoneNumber",
        formatPhoneNumber(currentProps.data.homePhone),
        phonePrefix,
      );
      testField("#location-00-fax", formatPhoneNumber(currentProps.data.faxNumber), phonePrefix);
    });

    cy.get("#location-00-notes-section").within(() => {
      testField("#location-00-notes", currentProps.data.cliLocnComment);
    });
  });

  it("hides sections when they are empty", () => {
    const data: ClientLocation = {
      ...getDefaultProps().data,
      addressTwo: "",
      addressThree: "",
      emailAddress: "",
      businessPhone: "",
      cellPhone: "",
      homePhone: "",
      faxNumber: "",
      cliLocnComment: "",
    };
    mount({ data });

    cy.get("#location-00-email-section").should("not.exist");
    cy.get("#location-00-phone-section").should("not.exist");
    cy.get("#location-00-notes-section").should("not.exist");
  });

  it("hides address fields when they are empty", () => {
    const data: ClientLocation = {
      ...getDefaultProps().data,
      addressTwo: "",
      addressThree: "",
    };
    mount({ data });

    testFieldHidden("#location-00-addressTwo");
    testFieldHidden("#location-00-addressThree");
  });

  describe("while there is at least one phone to be displayed", () => {
    const scenarios = [
      ["businessPhone", "#location-00-primaryPhoneNumber"],
      ["cellPhone", "#location-00-secondaryPhoneNumber"],
      ["homePhone", "#location-00-tertiaryPhoneNumber"],
      ["faxNumber", "#location-00-fax"],
    ];

    scenarios.forEach((scenario) => {
      const [propName, selector] = scenario;
      const otherPhonesList = scenarios.filter((cur) => cur[0] !== propName);
      describe(propName, () => {
        beforeEach(() => {
          const data: ClientLocation = {
            ...getDefaultProps().data,
            businessPhone: "",
            cellPhone: "",
            homePhone: "",
            faxNumber: "",
            [propName]: "2505559876",
          };
          mount({ data });
        });
        it(`displays the one phone with value: ${propName}`, () => {
          cy.get("#location-00-phone-section").within(() => {
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
        cy.get(`#location-${customProps.data.clientLocnCode}-EditBtn`).click();
      }
    });

    it("enables the edition of some fields by displaying the staff-details-location-group-component", () => {
      cy.get<VueWrapper>("@vueWrapper").should((vueWrapper) => {
        const staffCreateComponent = vueWrapper.getComponent({
          name: "staff-details-location-group-component",
        });

        expect(staffCreateComponent.props("id")).to.eq(0);

        const staffCreateData: Address = staffCreateComponent.props("modelValue");

        expect(staffCreateData).to.deep.eq(locationToCreateFormat(currentProps.data));
      });
    });

    describe("additional delivery information", () => {
      let bus: ReturnType<typeof useEventBus<ModalNotification>>;

      before(() => {
        customProps.data.addressThree = null;
      });

      beforeEach(() => {
        bus = useEventBus<ModalNotification>("modal-notification");
        bus.on((payload) => {
          payload.handler(); // automatically proceed with the deletion
        });
      });

      afterEach(() => {
        bus.reset();
      });

      after(() => {
        defaultInit();
      });

      it("should add the Additional delivery information and then remove it", () => {
        // should not display the Additional delivery information input initially
        cy.get("#complementaryAddressTwo_0").should("not.exist");

        // click to add it
        cy.contains("Add more delivery information").should("be.visible").click();

        // should display the Additional delivery information input
        cy.get("#complementaryAddressTwo_0").should("be.visible");

        // should hide the button
        cy.contains("Add more delivery information").should("not.exist");

        // click to remove it
        cy.get("#deleteAdditionalDeliveryInformation_0").click();

        // should hide the Additional delivery information input again
        cy.get("#complementaryAddressTwo_0").should("not.exist");

        // the button to add it is visible again
        cy.contains("Add more delivery information").should("be.visible");
      });
    });

    it("disables the Save button by default", () => {
      cy.get("#location-00-SaveBtn").shadow().find("button").should("be.disabled");
    });

    it("enables the Save button once something gets changed", () => {
      cy.clearFormEntry("#emailAddress_0");
      cy.get("#location-00-SaveBtn").shadow().find("button").should("be.enabled");
    });

    it("disables the Save button again if values are restored to their original values", () => {
      cy.clearFormEntry("#emailAddress_0");
      cy.get("#location-00-SaveBtn").shadow().find("button").should("be.enabled");
      cy.fillFormEntry("#emailAddress_0", currentProps.data.emailAddress);
      cy.get("#location-00-SaveBtn").shadow().find("button").should("be.disabled");
    });

    it("restores original values if the Cancel button gets clicked", () => {
      // Change some values
      cy.clearFormEntry("#name_0");
      cy.fillFormEntry("#name_0", "Changed name");
      cy.selectFormEntry("#province_0", "Quebec");
      cy.clearFormEntry("#emailAddress_0");

      // Cancel
      cy.get("#location-00-CancelBtn").click();

      // Click to edit again
      cy.get("#location-00-EditBtn").click();

      // Check changed values were restored on the form
      testTextInput("#name_0", currentProps.data.clientLocnName);
      testComboBox("#province_0", currentProps.data.provinceDesc);
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

          cy.get("#location-00-SaveBtn").click();

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

            // Contains the location data as edited/created by the user
            expect(updatedData).to.deep.eq({
              ...customProps.data,
              emailAddress: null,
            });
          });
        });
      });
    });

    it("forwards validations to the staff-details-location-group-component", () => {
      cy.get<VueWrapper>("@vueWrapper").should((vueWrapper) => {
        const staffCreateComponent = vueWrapper.getComponent({
          name: "staff-details-location-group-component",
        });

        expect(staffCreateComponent.props("validations")[0]).to.eq(validationWrapper);
      });
    });

    it("doesn't display a Deactivate button when location code is '00'", () => {
      cy.get("#location-00-DeactivateBtn").should("not.exist");
    });

    describe("and location code is not '00'", () => {
      before(() => {
        customProps.data.clientLocnCode = "01";
      });
      after(() => {
        defaultInit();
      });
      describe("and the Deactivate button is clicked", () => {
        beforeEach(() => {
          cy.get("#location-01-DeactivateBtn").click();
        });

        it("displays a confirmation dialog when Deactivate is clicked", () => {
          cy.get("#modal-deactivate").should("be.visible");
        });

        it("emits a save event when the intention to Deactivate is confirmed", () => {
          cy.get("#modal-deactivate .cds--modal-submit-btn").click();

          cy.get("@vueWrapper").should((vueWrapper) => {
            const saveData = vueWrapper.emitted("save")[0][0];
            const { patch } = saveData;

            expect(patch).to.be.an("array");
            expect(patch).to.have.lengthOf(1);

            expect(patch[0].op).to.eq("replace");
            expect(patch[0].path).to.eq("/locnExpiredInd");
            expect(patch[0].value).to.eq("Y");
          });
        });
      });
    });
  });

  describe("when location is expired", () => {
    beforeEach(() => {
      const props = getDefaultProps();
      props.data.clientLocnCode = "01";
      props.data.locnExpiredInd = "Y";
      mount(props);
    });

    describe("and the Reactivate button is clicked", () => {
      beforeEach(() => {
        cy.get("#location-01-ReactivateBtn").click();
      });

      it("displays a confirmation dialog when Reactivate is clicked", () => {
        cy.get("#modal-reactivate").should("be.visible");
      });

      it("emits a save event when the intention to Reactivate is confirmed", () => {
        cy.get("#modal-reactivate .cds--modal-submit-btn").click();

        cy.get("@vueWrapper").should((vueWrapper) => {
          const saveData = vueWrapper.emitted("save")[0][0];
          const { patch } = saveData;

          expect(patch).to.be.an("array");
          expect(patch).to.have.lengthOf(1);

          expect(patch[0].op).to.eq("replace");
          expect(patch[0].path).to.eq("/locnExpiredInd");
          expect(patch[0].value).to.eq("N");
        });
      });
    });
  });
});
