import type { ClientLocation } from "@/dto/CommonTypesDto";
import LocationView from "@/pages/client-details/LocationView.vue";

describe("<location-view />", () => {
  const getDefaultProps = () => ({
    data: {
      clientLocnName: "Mailing address",
      clientLocnCode: "00",
      addressOne: "886 Richmond Ave",
      addressTwo: "C/O Tony Pineda",
      addressThree: "Sample additional info",
      country: "Canada",
      provinceCode: "SK",
      provinceDesc: "Saskatchewan",
      city: "Hampton",
      postalCode: "T4G5J1",
      emailAddress: "contact@mail.com",
      businessPhone: "(250) 286-3767",
      cellPhone: "(250) 555-3700",
      faxNumber: "(250) 286-3768",
      homePhone: "(250) 555-3101",
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

  const testField = (selector: string, value: string) => {
    cy.get(selector).should("be.visible");
    cy.get(selector).contains(value);
    expect(value.length).to.be.greaterThan(0);
  };

  const testFieldHidden = (selector: string) => {
    cy.get(selector).should("not.exist");
  };

  it("renders the ClientLocation component", () => {
    mount();

    cy.get("#location-00-address-section").within(() => {
      testField("#location-00-streetAddress", currentProps.data.addressOne);

      // City, Province
      testField(
        "#location-00-city-province",
        `${currentProps.data.city}, ${currentProps.data.provinceDesc}`,
      );

      testField("#location-00-country", currentProps.data.country);
      testField("#location-00-postalCode", currentProps.data.postalCode);
    });

    cy.get("#location-00-delivery-section").within(() => {
      // same field twice because we can't assert the <br> that separates the values as text content.
      testField("#location-00-deliveryInformation", currentProps.data.addressTwo);
      testField("#location-00-deliveryInformation", currentProps.data.addressThree);
    });

    cy.get("#location-00-email-section").within(() => {
      testField("#location-00-emailAddress", currentProps.data.emailAddress);
    });

    cy.get("#location-00-phone-section").within(() => {
      testField("#location-00-primaryPhoneNumber", currentProps.data.businessPhone);
      testField("#location-00-secondaryPhoneNumber", currentProps.data.cellPhone);
      testField("#location-00-fax", currentProps.data.faxNumber);
      testField("#location-00-other", currentProps.data.homePhone);
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
      faxNumber: "",
      homePhone: "",
      cliLocnComment: "",
    };
    mount({ data });

    cy.get("#location-00-delivery-section").should("not.exist");
    cy.get("#location-00-email-section").should("not.exist");
    cy.get("#location-00-phone-section").should("not.exist");
    cy.get("#location-00-notes-section").should("not.exist");
  });

  describe("while there is at least one phone to be displayed", () => {
    const scenarios = [
      ["businessPhone", "#location-00-primaryPhoneNumber"],
      ["cellPhone", "#location-00-secondaryPhoneNumber"],
      ["faxNumber", "#location-00-fax"],
      ["homePhone", "#location-00-other"],
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
            faxNumber: "",
            homePhone: "",
            [propName]: "(250) 555-9876",
          };
          mount({ data });
        });
        it(`displays the one phone with value: ${propName}`, () => {
          cy.get("#location-00-phone-section").within(() => {
            testField(selector, currentProps.data[propName]);
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
