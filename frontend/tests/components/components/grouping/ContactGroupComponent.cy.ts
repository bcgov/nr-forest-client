import ContactGroupComponent from "@/components/grouping/ContactGroupComponent.vue";
import type { Contact } from "@/dto/ApplyClientNumberDto";

// load app validations
import "@/helpers/validators/BCeIDFormValidations";

describe("<ContactGroupComponent />", () => {
  const dummyValidation = (): ((
    key: string,
    field: string
  ) => (value: string) => string) => {
    return (key: string, fieldId: string) => (value: string) => {
      if (value.includes("fault")) return "Error";
      return "";
    };
  };

  beforeEach(() => {
    cy.fixture("contact.json").as("contactFixture");
    cy.fixture("roles.json").as("rolesFixture");
    cy.fixture("addresses.json").as("addressesFixture");
  });

  it("should render the component", () => {
    cy.get("@contactFixture").then((contact: Contact) => {
      cy.get("@rolesFixture").then((roles) => {
        cy.get("@addressesFixture").then((addresses) => {
          cy.mount(ContactGroupComponent, {
            props: {
              id: 0,
              modelValue: contact,
              enabled: true,
              roleList: roles,
              addressList: addresses,
              validations: [],
            },
          });
        });
      });
    });

    cy.get("@contactFixture").then((contact: Contact) => {

      cy.contains("Full name").should("be.visible");
      cy.get("#fullName_0")
        .contains(`${contact.firstName} ${contact.lastName}`)
        .should("be.visible");

      cy.contains("Email address").should("be.visible");
      cy.get("#email_0").contains(contact.email).should("be.visible");

      cy.get("#phoneNumber_0").should("be.visible").and("have.value", "");

      cy.focused().should('have.id', 'phoneNumber_0');
    });
  });

  it("should render the component with validation", () => {
    cy.get("@contactFixture").then((contact: Contact) => {
      cy.get("@rolesFixture").then((roles) => {
        cy.get("@addressesFixture").then((addresses) => {
          cy.mount(ContactGroupComponent, {
            props: {
              id: 1,
              modelValue: {
                ...contact,
                firstName: contact.firstName + " fault",
              },
              enabled: true,
              roleList: roles,
              addressList: addresses,
              validations: [dummyValidation()],
            },
          });
        });
      });
    });

    cy.get("@contactFixture").then((contact: Contact) => {
      cy.get("#firstName_1")
        .should("be.visible")
        .and("have.value", contact.firstName + " fault");

      cy.get("#firstName_1")
        .shadow()
        .find(".cds--form-requirement")
        .should("be.visible")
        .find("slot")
        .and("include.text", "Error");

      cy.get("#lastName_1")
        .should("be.visible")
        .and("have.value", contact.lastName);

      cy.get("#lastName_1")
        .shadow()
        .find(".cds--form-requirement")
        .should("be.visible")
        .find("slot")
        .and("include.text", "Error");

      cy.get("#email_1").should("be.visible").and("have.value", contact.email);

      cy.get("#phoneNumber_1").should("be.visible").and("have.value", "");

      cy.focused().should('have.id', 'firstName_1');
    });
  });

  it("should render the component and select first address and show on component", () => {
    cy.get("@contactFixture").then((contact: Contact) => {
      cy.get("@rolesFixture").then((roles) => {
        cy.get("@addressesFixture").then((addresses) => {
          cy.mount(ContactGroupComponent, {
            props: {
              id: 0,
              modelValue: {
                ...contact,
                addresses: [addresses[0], addresses[1]],
              },
              enabled: true,
              roleList: roles,
              addressList: addresses,
              validations: [],
            },
          });
        });
      });
    });

    cy.focused().should('have.id', 'phoneNumber_0');

    cy.get("#addressname_0").should("be.visible").and("have.value", "");

    cy.get("#addressname_0")
      .click()
      .find('cds-multi-select-item[data-value="Mailing address"]')
      .should("exist")
      .and("be.visible")
      .click();

    cy.get("#addressname_0")
      .should("be.visible")
      .and("have.value", "Mailing address");
  });

  it("should render the component and select both addresses and show it as value", () => {
    cy.get("@contactFixture").then((contact: Contact) => {
      cy.get("@rolesFixture").then((roles) => {
        cy.get("@addressesFixture").then((addresses) => {
          cy.mount(ContactGroupComponent, {
            props: {
              id: 0,
              modelValue: {
                ...contact,
                addresses: [addresses[0], addresses[1]],
              },
              enabled: true,
              roleList: roles,
              addressList: addresses,
              validations: [],
            },
          });
        });
      });
    });

    cy.focused().should('have.id', 'phoneNumber_0');

    cy.get("#addressname_0").should("be.visible").and("have.value", "");

    cy.get("#addressname_0")
      .click()
      .find('cds-multi-select-item[data-value="Mailing address"]')
      .should("exist")
      .and("be.visible")
      .click();

    cy.get("#addressname_0")
      .click()
      .find('cds-multi-select-item[data-value="Jutland office"]')
      .should("exist")
      .and("be.visible")
      .click();

    cy.get("#addressname_0")
      .should("be.visible")
      .and("have.value", "Mailing address,Jutland office");
  });

  it("should render the component and select first address and show it then remove it", () => {
    cy.get("@contactFixture").then((contact: Contact) => {
      cy.get("@rolesFixture").then((roles) => {
        cy.get("@addressesFixture").then((addresses) => {
          cy.mount(ContactGroupComponent, {
            props: {
              id: 0,
              modelValue: {
                ...contact,
                addresses: [addresses[0], addresses[1]],
              },
              enabled: true,
              roleList: roles,
              addressList: addresses,
              validations: [],
            },
          });
        });
      });
    });

    cy.focused().should('have.id', 'phoneNumber_0');

    cy.get("#addressname_0").should("be.visible").and("have.value", "");

    cy.get("#addressname_0")
      .click()
      .find('cds-multi-select-item[data-value="Mailing address"]')
      .should("exist")
      .and("be.visible")
      .click();

    cy.get("#addressname_0")
      .should("be.visible")
      .and("have.value", "Mailing address");

    cy.get("#addressname_0")
      .click()
      .find('cds-multi-select-item[data-value="Mailing address"]')
      .should("exist")
      .and("be.visible")
      .click();

    cy.get("#addressname_0").should("be.visible").and("have.value", "");
  });

  it("should logout and redirect to BCeID", () => {
    const $session = {
      logOut() {},
    };
    cy.get("@contactFixture").then((contact: Contact) => {
      cy.get("@rolesFixture").then((roles) => {
        cy.get("@addressesFixture").then((addresses) => {
          cy.mount(ContactGroupComponent, {
            props: {
              id: 0,
              modelValue: {
                ...contact,
                addresses: [addresses[0], addresses[1]],
              },
              enabled: true,
              roleList: roles,
              addressList: addresses,
              validations: [],
            },
            global: {
              config: {
                globalProperties: {
                  $session,
                },
              },
            },
          });
        });
      });
    });

    cy.focused().should('have.id', 'phoneNumber_0');

    cy.get("#change-personal-info-link").click();

    cy.get("#logout-and-redirect-modal").should("be.visible");

    cy.spy(window, "open").as("windowOpen");
    cy.spy($session, "logOut").as("sessionLogOut");

    cy.get('#logout-and-redirect-modal > cds-modal-footer > .cds--modal-submit-btn').click();

    cy.get("@windowOpen").should("be.calledWith", "https://www.bceid.ca/", "_blank", "noopener");
    cy.get("@sessionLogOut").should("be.called");
  });

  describe('when it has last emitted "valid" with false due to a single, not empty, invalid field', () => {
    const genericTest = (
      fieldSelector: string,
      firstContent: string,
      additionalContent: string,
      expectedFinalValue = `${firstContent}${additionalContent}`
    ) => {
      const calls: boolean[] = [];
      const onValid = (valid: boolean) => {
        calls.push(valid);
      };

      // Trying to fix issue on the CI pipeline.
      // cy.wait(200);

      cy.get("@contactFixture").then((contact: Contact) => {
        cy.get("@rolesFixture").then((roles) => {
          cy.get("@addressesFixture").then((addresses) => {
            cy.mount(ContactGroupComponent, {
              props: {
                id: 1,
                modelValue: {
                  ...contact,
                  locationNames: addresses.map((a) => ({
                    value: a.code,
                    text: a.name,
                  })),
                  email: "a@b.com",
                  phoneNumber: "(123) 123-1234",
                  contactType: { value: roles[0].code, text: roles[0].name },
                },
                enabled: true,
                roleList: roles,
                addressList: addresses,
                validations: [],
                onValid,
              },
            });
          });
        });
      });

      // Debugging issue on CI.
      cy.get("#firstName_1");

      cy.focused().should("have.id", "firstName_1");
      cy.get(fieldSelector).shadow().find("input").clear(); // emits false
      cy.get(fieldSelector).blur(); // (doesn't emit)
      cy.get(fieldSelector).shadow().find("input").type(firstContent); // emits true before blurring
      cy.get(fieldSelector).blur(); // emits false
      cy.get(fieldSelector)
        .should("be.visible")
        .and("have.value", firstContent);
      cy.get(fieldSelector).shadow().find("input").type(additionalContent); // emits true (last2)
      cy.get(fieldSelector).blur(); // emits false (last1)
      cy.get(fieldSelector)
        .should("be.visible")
        .and("have.value", expectedFinalValue);

      // For some reason on Electron we need to wait a millisecond now.
      // Otherwise this test either fails or can't be trusted on Electron.
      cy.wait(1);

      return calls;
    };
    const checkValidFalseAgain = (calls: boolean[]) => {
      cy.wrap(calls).then((value) => {
        const last1 = value.pop();
        const last2 = value.pop();
        const last3 = value.pop();
        expect(last3).to.equal(false);
        expect(last2).to.equal(true);
        expect(last1).to.equal(false);
      });
    };
    const checkValidTrue = (calls: boolean[]) => {
      cy.wrap(calls).then((value) => {
        const last1 = value.pop();
        const last2 = value.pop();
        expect(last2).to.equal(false);
        expect(last1).to.equal(true);
      });
    };
    describe("First name", () => {
      it('should emit "valid" with false again', () => {
        const calls = genericTest("#firstName_1", "A", "{backspace}", "");

        checkValidFalseAgain(calls);
      });
      it('should emit "valid" with true', () => {
        const calls = genericTest("#firstName_1", "A", "{backspace}B", "B");

        checkValidTrue(calls);
      });
    });
    describe("Last name", () => {
      it('should emit "valid" with false again', () => {
        const calls = genericTest("#lastName_1", "A", "{backspace}", "");

        checkValidFalseAgain(calls);
      });
      it('should emit "valid" with true', () => {
        const calls = genericTest("#lastName_1", "A", "{backspace}B", "B");

        checkValidTrue(calls);
      });
    });
    describe("Email address", () => {
      it('should emit "valid" with false again', () => {
        const calls = genericTest("#email_1", "a@", "b");

        checkValidFalseAgain(calls);
      });
      it('should emit "valid" with true', () => {
        const calls = genericTest("#email_1", "a@", "b.com");

        checkValidTrue(calls);
      });
    });
    describe("Phone number", () => {
      it('should emit "valid" with false again', () => {
        const calls = genericTest("#phoneNumber_1", "(123) 123", "-1");

        checkValidFalseAgain(calls);
      });
      it('should emit "valid" with true', () => {
        const calls = genericTest("#phoneNumber_1", "(123) 123", "-1234");

        checkValidTrue(calls);
      });
    });
  });
});
