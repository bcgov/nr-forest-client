import type { ClientContact } from "@/dto/CommonTypesDto";
import ContactView from "@/pages/client-details/ContactView.vue";

describe("<contact-view />", () => {
  const getDefaultProps = () => ({
    data: {
      locationCode: ["01"],
      contactName: "Cheryl Bibby",
      contactTypeCode: "BL",
      contactTypeDesc: "Billing",
      businessPhone: "(250) 286-3767",
      secondaryPhone: "(250) 555-3700",
      faxNumber: "(250) 286-3768",
      emailAddress: "cheryl@ktb.com",
    } as ClientContact,
    index: 0,
    associatedLocationsString: "01 - Town office",
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

    cy.get("#contact-0-general-section").within(() => {
      testField("#contact-0-contactType", currentProps.data.contactTypeDesc);
      testField("#contact-0-associatedLocations", currentProps.associatedLocationsString);
      testField("#contact-0-emailAddress", currentProps.data.emailAddress, emailPrefix);
    });

    const phonePrefix = "tel:";

    cy.get("#contact-0-phone-section").within(() => {
      testField("#contact-0-primaryPhoneNumber", currentProps.data.businessPhone, phonePrefix);
      testField("#contact-0-secondaryPhoneNumber", currentProps.data.secondaryPhone, phonePrefix);
      testField("#contact-0-fax", currentProps.data.faxNumber, phonePrefix);
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
            [propName]: "(250) 555-9876",
          };
          mount({
            ...getDefaultProps(),
            data,
          });
        });
        it(`displays the one phone with value: ${propName}`, () => {
          cy.get("#contact-0-phone-section").within(() => {
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
