import BusinessInformationWizardStep from "@/pages/bceidform/BusinessInformationWizardStep.vue";
import type { FormDataDto } from "@/dto/ApplyClientNumberDto";

describe('<BusinessInformationWizardStep />', () => {
  beforeEach(() => {
    cy.intercept('/api/clients/name/*').as('autoCompleteRequest');
  });

  it('renders the BusinessInformationWizardStep component and interacts with elements', () => {
    cy.mount(BusinessInformationWizardStep, {
      props: {
        data: {
          businessInformation: {
            businessType: "",
            legalType: "",
            clientType: "",
            incorporationNumber: "",
            businessName: "",
            goodStandingInd: "",
            birthdate: "",
            address: ""
          },
        } as unknown as FormDataDto,
        active: false,
      },
    });

    cy.get('#businessTyperbR').click();
    
    cy.get('#bcRegistryEmailId').click();

    cy.get('.link-button:visible').should('have.attr', 'aria-label', 'Contact BC Registry via Email');
    cy.get('.link-button:visible span').should('exist');
    cy.get('.link-button:visible span').invoke('text').should('not.be.empty');
  });
});
