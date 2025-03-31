import StaffContactGroupComponent from "@/components/grouping/StaffContactGroupComponent.vue";

// load app validations
import "@/helpers/validators/BCeIDFormValidations";
import type { VueWrapper } from "@vue/test-utils";

describe("<StaffLocationGroupComponent />", () => {
  beforeEach(() => {
    cy.fixture("contact.json").as("contactFixture");

    cy.fixture("roles.json").as("rolesFixture");

    cy.fixture("addresses.json").as("addressesFixture");
  });

  const firstNameSelector0 = "#firstName_0";
  const lastNameSelector0 = "#lastName_0";
  const fullNameSelector0 = "#fullName_0";

  it("should render the component with fields First and Last names by default", () => {
    cy.get("@contactFixture").then((contact: any) => {
      cy.get("@rolesFixture").then((roles) => {
        cy.mount(StaffContactGroupComponent, {
          props: {
            id: 0,
            modelValue: contact,
            roleList: roles,
            validations: [],
          },
        });
      });
    });

    cy.get(firstNameSelector0).should("be.visible");
    cy.get(lastNameSelector0).should("be.visible");

    cy.get(fullNameSelector0).should("not.exist");
  });

  it("should render the component with field Full name instead of First and Last names", () => {
    cy.get("@contactFixture").then((contact: any) => {
      cy.get("@rolesFixture").then((roles) => {
        cy.mount(StaffContactGroupComponent, {
          props: {
            id: 0,
            modelValue: contact,
            roleList: roles,
            validations: [],
            singleInputForName: true,
          },
        });
      });
    });

    cy.get(fullNameSelector0).should("be.visible");

    cy.get(firstNameSelector0).should("not.exist");
    cy.get(lastNameSelector0).should("not.exist");
  });

  const deleteSelector1 = "#deleteContact_1";

  it("should render the component with a Delete button by default when id > 0", () => {
    cy.get("@contactFixture").then((contact: any) => {
      cy.get("@rolesFixture").then((roles) => {
        cy.mount(StaffContactGroupComponent, {
          props: {
            id: 1,
            modelValue: contact,
            roleList: roles,
            validations: [],
          },
        });
      });
    });

    cy.get(deleteSelector1).should("be.visible");
  });

  it("should render the component without a Delete button", () => {
    cy.get("@contactFixture").then((contact: any) => {
      cy.get("@rolesFixture").then((roles) => {
        cy.mount(StaffContactGroupComponent, {
          props: {
            id: 1,
            modelValue: contact,
            roleList: roles,
            validations: [],
            hideDeleteButton: true,
          },
        });
      });
    });

    cy.get(deleteSelector1).should("not.exist");
  });

  it("displays location titles as their names by default", () => {
    cy.get("@contactFixture").then((contact: any) => {
      cy.get("@rolesFixture").then((roles) => {
        cy.get("@addressesFixture").then((addresses) => {
          cy.mount(StaffContactGroupComponent, {
            props: {
              id: 1,
              modelValue: contact,
              roleList: roles,
              addressList: addresses,
              validations: [],
            },
          });
        });
      });
    });

    cy.get("#addressname_1").find("[part='trigger-button']").click();

    cy.get("#addressname_1 cds-multi-select-item")
      .eq(0)
      .invoke("text")
      .should("eq", "Mailing address");

    cy.get("#addressname_1 cds-multi-select-item")
      .eq(1)
      .invoke("text")
      .should("eq", "Jutland office");
  });

  it("displays location titles as 'code - name'", () => {
    cy.get("@contactFixture").then((contact: any) => {
      cy.get("@rolesFixture").then((roles) => {
        cy.get("@addressesFixture").then((addresses) => {
          cy.mount(StaffContactGroupComponent, {
            props: {
              id: 1,
              modelValue: contact,
              roleList: roles,
              addressList: addresses,
              validations: [],
              showLocationCode: true,
            },
          });
        });
      });
    });

    cy.get("#addressname_1").find("[part='trigger-button']").click();

    cy.get("#addressname_1 cds-multi-select-item")
      .eq(0)
      .invoke("text")
      .should("eq", "00 - Mailing address");

    cy.get("#addressname_1 cds-multi-select-item")
      .eq(1)
      .invoke("text")
      .should("eq", "01 - Jutland office");
  });

  it("display the selected locations", () => {
    cy.get("@contactFixture").then((contact: any) => {
      cy.get("@rolesFixture").then((roles) => {
        cy.get("@addressesFixture").then((addresses) => {
          cy.mount(StaffContactGroupComponent, {
            props: {
              id: 1,
              modelValue: {
                ...contact,
                locationNames: [
                  {
                    value: "01",
                    text: "Jutland office",
                  },
                ],
              },
              roleList: roles,
              addressList: addresses,
              validations: [],
            },
          });
        });
      });
    });

    cy.get("#addressname_1").and("have.value", "Jutland office");
  });

  it("display the selected locations when showLocationCode is true", () => {
    cy.get("@contactFixture").then((contact: any) => {
      cy.get("@rolesFixture").then((roles) => {
        cy.get("@addressesFixture").then((addresses) => {
          cy.mount(StaffContactGroupComponent, {
            props: {
              id: 1,
              modelValue: {
                ...contact,
                locationNames: [
                  {
                    value: "01",
                    text: "Jutland office",
                  },
                ],
              },
              roleList: roles,
              addressList: addresses,
              validations: [],
              showLocationCode: true,
            },
          });
        });
      });
    });

    cy.get("#addressname_1").and("have.value", "01 - Jutland office");
  });

  it("can differentiate unnamed locations by their code", () => {
    cy.get("@contactFixture").then((contact: any) => {
      cy.get("@rolesFixture").then((roles) => {
        cy.mount(StaffContactGroupComponent, {
          props: {
            id: 1,
            modelValue: {
              ...contact,
              locationNames: [
                {
                  value: "01",
                  text: "",
                },
              ],
            },
            roleList: roles,
            addressList: [
              { code: "00", text: "" },
              { code: "01", text: "" },
              { code: "02", text: "" },
            ],
            validations: [],
            showLocationCode: true,
          },
        })
          .its("wrapper")
          .as("vueWrapper");
      });
    });

    cy.get("#addressname_1").and("have.value", "01");

    cy.get("#addressname_1").find("[part='trigger-button']").click();

    cy.get("#addressname_1")
      .find('cds-multi-select-item[data-value="02"]')
      .should("exist")
      .and("be.visible")
      .click();

    cy.get("#addressname_1").and("have.value", "01,02");

    cy.get<VueWrapper<InstanceType<typeof StaffContactGroupComponent>>>("@vueWrapper").should(
      (vueWrapper) => {
        expect(vueWrapper.props("modelValue").locationNames).to.deep.eq([
          { value: "01", text: undefined },
          { value: "02", text: undefined },
        ]);
      },
    );
  });
});
