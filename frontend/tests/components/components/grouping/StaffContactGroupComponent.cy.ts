import StaffContactGroupComponent from "@/components/grouping/StaffContactGroupComponent.vue";

// load app validations
import "@/helpers/validators/BCeIDFormValidations";

describe("<StaffLocationGroupComponent />", () => {
  beforeEach(() => {
    cy.fixture("contact.json").as("contactFixture");

    cy.fixture("roles.json").as("rolesFixture");
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
});
