import type { ClientLocation } from "@/dto/CommonTypesDto";
import LocationView from "@/pages/client-details/LocationView.vue";
import { formatPhoneNumber } from "@/services/ForestClientService";

describe("<location-view />", () => {
  const getDefaultProps = () => ({
    data: {
      clientLocnName: "Mailing address",
      clientLocnCode: "00",
      addressOne: "886 Richmond Ave",
      addressTwo: "C/O Tony Pineda",
      addressThree: "Sample additional info",
      countryCode: "CA",
      country: "Canada",
      province: "SK",
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
      testField("#location-00-addressTwo", currentProps.data.addressTwo);
      testField("#location-00-addressThree", currentProps.data.addressThree);
      testField("#location-00-streetAddress", currentProps.data.addressOne);

      // City, Province
      testField(
        "#location-00-city-province",
        `${currentProps.data.city}, ${currentProps.data.province}`,
      );

      testField("#location-00-country", currentProps.data.country);
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
});
