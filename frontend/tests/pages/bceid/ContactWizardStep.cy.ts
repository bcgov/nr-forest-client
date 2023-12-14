import ContactWizardStep from "@/pages/bceidform/ContactWizardStep.vue";
import type { Contact, FormDataDto } from "@/dto/ApplyClientNumberDto";
import { emptyContact } from "@/dto/ApplyClientNumberDto";

describe('<ContactWizardStep />', () => {
  beforeEach(() => {
    cy.fixture("contact.json").as("contactFixture");
    cy.fixture("roles.json").as("rolesFixture");
    cy.fixture("addresses.json").as("addressesFixture");
  });
  
  it('renders the ContactWizardStep component', () => {
    cy.mount(ContactWizardStep, {
      props: {
        data: {
          location: {
            addresses: [],
            contacts: [
              {
                ...emptyContact,
                firstName: "John",
                lastName: "Doe",
              } as Contact,
              {
                ...emptyContact,
                firstName: null,
                lastName: null,
              } as unknown as Contact,
            ],
          },
        } as unknown as FormDataDto,
        active: false,
      },
    });

    // Assert that the main component is rendered
    cy.get('.frame-01').should('exist');

    // Assert that the element with v-if condition is displayed when the condition is true
    cy.get('.body-compact-01').should('exist');

    // Check when 5 contacts are reached
    cy.get('@contactFixture').then((contact: Contact) => {
      const contactsArray = Array.from({ length: 5 }, (_, index) => ({ ...contact, firstName: `Contact${index + 1}` }));
      cy.mount(ContactWizardStep, {
        props: {
          data: {
            location: {
              addresses: [],
              contacts: contactsArray,
            },
          } as unknown as FormDataDto,
          active: false,
        },
      });

      // Assert that the element with v-if condition is displayed when the condition is met
      cy.get('#maxAdditionalContsReachedLblId').should('exist');
    });
  });
});
