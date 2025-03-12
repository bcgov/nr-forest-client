import StaffContactGroupComponent from "@/components/grouping/StaffContactGroupComponent.vue";

// load app validations
import "@/helpers/validators/BCeIDFormValidations";

describe("<StaffLocationGroupComponent />", () => {
  beforeEach(() => {
    cy.fixture("contact.json").as("contactFixture");

    cy.fixture("roles.json").as("rolesFixture");
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
