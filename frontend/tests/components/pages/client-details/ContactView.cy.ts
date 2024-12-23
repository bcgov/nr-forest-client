import type { ClientContact } from "@/dto/CommonTypesDto";
import ContactView from "@/pages/client-details/ContactView.vue";

describe("<contact-view />", () => {
  const getDefaultProps = () => ({
    data: {
      clientLocnCode: ["00"],
      contactCode: "00",
      contactName: "Cheryl Bibby",
      contactTypeCode: "BL",
      contactTypeDesc: "Billing",
      businessPhone:
        "({{randomValue length=3 type='NUMERIC'}}) {{randomValue length=3 type='NUMERIC'}}-{{randomValue length=4 type='NUMERIC'}}",
      secondaryPhone:
        "({{randomValue length=3 type='NUMERIC'}}) {{randomValue length=3 type='NUMERIC'}}-{{randomValue length=4 type='NUMERIC'}}",
      faxNumber:
        "({{randomValue length=3 type='NUMERIC'}}) {{randomValue length=3 type='NUMERIC'}}-{{randomValue length=4 type='NUMERIC'}}",
      emailAddress: "cheryl@ktb.com",
    } as ClientContact,
    associatedLocationsString: "00 - Mailing address",
  });

  let currentProps: ReturnType<typeof getDefaultProps> = null;
  const mount = (props = getDefaultProps()) => {
    currentProps = props;
    return cy
      .mount(ContactView, {
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

  it("renders the ClientContact component", () => {
    mount();

    const emailPrefix = "mailto:";

    cy.get("#contact-00-general-section").within(() => {
      testField("#contact-00-contactType", currentProps.data.contactTypeDesc);
      testField("#contact-00-associatedLocations", currentProps.associatedLocationsString);
      testField("#contact-00-emailAddress", currentProps.data.emailAddress, emailPrefix);
    });

    const phonePrefix = "tel:";

    cy.get("#contact-00-phone-section").within(() => {
      testField("#contact-00-primaryPhoneNumber", currentProps.data.businessPhone, phonePrefix);
      testField("#contact-00-secondaryPhoneNumber", currentProps.data.secondaryPhone, phonePrefix);
      testField("#contact-00-fax", currentProps.data.faxNumber, phonePrefix);
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
    cy.get("#contact-00-phone-section").should("not.exist");
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

    testFieldHidden("#contact-00-emailAddress");
  });

  describe("while there is at least one phone to be displayed", () => {
    const scenarios = [
      ["businessPhone", "#contact-00-primaryPhoneNumber"],
      ["secondaryPhone", "#contact-00-secondaryPhoneNumber"],
      ["faxNumber", "#contact-00-fax"],
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
            [propName]: "(250) 555-9876",
          };
          mount({
            ...getDefaultProps(),
            data,
          });
        });
        it(`displays the one phone with value: ${propName}`, () => {
          cy.get("#contact-00-phone-section").within(() => {
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
