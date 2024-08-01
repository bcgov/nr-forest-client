import BcRegisteredClientInformationWizardStep from '@/pages/staffform/BcRegisteredClientInformationWizardStep.vue';
import { newFormDataDto, type FormDataDto } from '@/dto/ApplyClientNumberDto';

import "@/helpers/validators/StaffFormValidations";

describe('<BcRegisteredClientInformationWizardStep />', () => {

  beforeEach(() => {
    cy.viewport(1056, 800);
    cy.intercept('GET', '/api/clients/name/exi', {
      fixture: 'clients/bcreg_ac_list1.json',
    });
    cy.intercept('GET', '/api/clients/C1234567', {
      fixture: 'clients/bcreg_C1234567.json',
    });
  });

  it('should render the component', () => {
    const formContent: FormDataDto = newFormDataDto();
    formContent.businessInformation.businessName = '';
    formContent.businessInformation.businessType = 'BCR';
    cy.log('formContent', formContent);

    cy.mount(BcRegisteredClientInformationWizardStep, {
      props: {
        data: formContent,
        active: true,
        autofocus: false,
      },
    });

    // Initially, only the client name and the info notification should exist
    cy.get('#businessName').should('exist');
    cy.get('cds-inline-notification').should('exist');

    //Just a check to make sure the fields are not visible
    cy.get('.read-only-box').should('not.exist');

    // Then, when a client is selected, the rest of the form should appear
    cy.selectAutocompleteEntry('#businessName', 'exi','C1234567');
    
    cy.get('.read-only-box')
    .should('exist')
    .find('cds-inline-notification').should('exist');

    cy.get('.read-only-box > :nth-child(2) > .title-group-01 > .label-01')
    .should('exist')
    .and('have.text', 'Type');

    cy.get('.read-only-box > :nth-child(2) > .body-compact-01')
    .should('exist')    
    .and('have.text', 'Corporation');

    cy.get('.read-only-box > :nth-child(4) > .title-group-01 > .label-01')
    .should('exist')
    .and('have.text', 'Registration number');

    cy.get('.read-only-box > :nth-child(4) > .body-compact-01')
    .should('exist')
    .and('have.text', 'C1234567');


    cy.get('.read-only-box > :nth-child(6) > .title-group-01 > .label-01')    
    .should('exist')
    .and('have.text', 'BC Registries standing');

    cy.get('.read-only-box > :nth-child(6) > div.internal-grouping-01 > .body-compact-01')
    .should('exist')
    .and('have.text', 'Good standing');

    cy.get('#workSafeBCNumber').should('exist');
    cy.get('#doingBusinessAs').should('exist');
    cy.get('#acronym').should('exist');


  });

  describe('Scenario combinations', () => {

    const scenarios = [
      {
        scenarioName: 'OK State - Corporation',
        companySearch: 'cmp',
        companyCode: 'C1231231',
        showData: true,
        showBirthdate: false,
        showUnknowNotification: false,
        showNotGoodStandingNotification: false,
        showBcRegDownNotification: false,
        showDuplicatedNotification: false,
        type: 'Corporation',
        standing: 'Good standing',
        dba: '',
      },
      {
        scenarioName: 'OK State - Sole Proprietorship, show birthdate',
        companySearch: 'spp',
        companyCode: 'FM123123',
        showData: true,
        showBirthdate: true,
        showUnknowNotification: false,
        showNotGoodStandingNotification: false,
        showBcRegDownNotification: false,
        showDuplicatedNotification: false,
        type: 'Sole proprietorship',
        standing: 'Good standing',
        dba: 'Soleprop',
      },    
      {
        scenarioName: 'OK State - Unsuported types for external',
        companySearch: 'llp',
        companyCode: 'LL123123',
        showData: false,
        showBirthdate: false,
        showUnknowNotification: false,
        showNotGoodStandingNotification: false,
        showBcRegDownNotification: false,
        type: 'Limited liability partnership',
        standing: 'Unknow',
        dba: '',
      },
      {
        scenarioName: 'OK State - SP not owned by person',
        companySearch: 'spw',
        companyCode: 'FM7715744',
        showData: true,
        showBirthdate: true,
        showUnknowNotification: false,
        showNotGoodStandingNotification: false,
        showBcRegDownNotification: false,
        showDuplicatedNotification: false,
        type: 'Sole proprietorship',
        standing: 'Good standing',
        dba: 'Soleprop',
      },
      {
        scenarioName: 'Failed state - Unknown standing',
        companySearch: 'uks',
        companyCode: 'C4566541',
        showData: true,
        showBirthdate: false,
        showUnknowNotification: true,
        showNotGoodStandingNotification: false,
        showBcRegDownNotification: false,
        showDuplicatedNotification: false,
        type: 'Corporation',
        standing: 'Unknow',
        dba: '',
      },
      {
        scenarioName: 'Failed state - BC Registry down',
        companySearch: 'bcd',
        companyCode: 'C7745745',
        showData: false,
        showBirthdate: false,
        showUnknowNotification: false,
        showNotGoodStandingNotification: false,
        showBcRegDownNotification: true,
        showDuplicatedNotification: false,
        type: 'Corporation',
        standing: 'Good Standing',
        dba: '',
      },
      {
        scenarioName: 'Failed state - Duplicated entry',
        companySearch: 'dup',
        companyCode: 'C7775745',
        showData: true,
        showBirthdate: false,
        showUnknowNotification: true,
        showNotGoodStandingNotification: false,
        showBcRegDownNotification: false,
        showDuplicatedNotification: true,
        type: 'Corporation',
        standing: 'Unknow',
        dba: '',
      },
      {
        scenarioName: 'Failed state - Not in good standing',
        companySearch: 'ngs',
        companyCode: 'C4443332',
        showData: true,
        showBirthdate: false,
        showUnknowNotification: false,
        showNotGoodStandingNotification: true,
        showBcRegDownNotification: false,
        type: 'Corporation',
        standing: 'Not in good standing',
        dba: '',
      },
    ]

    
    beforeEach(function() {

      //The title contains some parameters that we need to extract
      const params: string[] = 
      this
      .currentTest
      .title
      .split(':')[1].trim()
      .replace(' should return ', ' ')
      .split(' ');
      
      const detailsCode = (code: string) =>{
        return code === 'bcd' ? 408 : code === 'dup' ? 409 : 200;
      }

      //We need to intercept the client search for the scenario, as param 1
      cy.intercept('GET', `**/api/clients/name/${params[0]}`, {
        statusCode: 200,
        fixture: 'clients/bcreg_ac_list2.json',
      }).as('clientSearch');
      
      //If the search is successful, we need to intercept the subsequent search that happens when the clie8nt is selected
      if(detailsCode(params[0]) === 200){
        cy.fixture(`clients/bcreg_${params[1]}.json`, 'utf-8').then((data) => {
          cy.intercept('GET', `**/api/clients/name/${encodeURIComponent(data.name)}`, {
            fixture: 'clients/bcreg_ac_list2.json',
          }).as('clientSearchEncoded');
        }).as('responseData');
      }

      if(detailsCode(params[0]) === 409){        
        cy.intercept('GET', `**/api/clients/name/${encodeURIComponent('Corporation 5')}`, {
          fixture: 'clients/bcreg_ac_list2.json',
        }).as('clientSearchEncodedToo');        
      }
  
      //We load the fixture beforehand due to the different content types and extensions based on the response
      cy.fixture(`clients/bcreg_${params[1]}.${detailsCode(params[0]) === 200 ? 'json' : 'txt'}`, 'utf-8').then((data) => {
        cy.log('data', data);
        cy.intercept('GET', `**/api/clients/${params[1]}`, {
          statusCode: detailsCode(params[0]),
          body: data,
          headers: {
            'content-type': detailsCode(params[0]) !== 200 ? 'text/plain' :'application/json',
          },
        }).as('clientDetails');
      });
    
    });

    scenarios.forEach((scenario) => {

      it(`${scenario.scenarioName} : ${scenario.companySearch} should return ${scenario.companyCode}`, () => {

        const formContent: FormDataDto = newFormDataDto();
        formContent.businessInformation.businessName = '';
        formContent.businessInformation.businessType = 'BCR';
        
        cy.mount(BcRegisteredClientInformationWizardStep, {
          props: {
            data: formContent,
            active: true,
            autofocus: false,
          },
        });

        // Initially, only the client name and the info notification should exist
        cy.get('#businessName').should('exist');
        cy.get('cds-inline-notification').should('exist');

        //Just a check to make sure the fields are not visible
        cy.get('.read-only-box').should('not.exist');

        cy.get('cds-inline-notification#bcRegistrySearchNotification').should('exist');

        // Then, when a client is selected, the rest of the form should appear
        cy.selectAutocompleteEntry('#businessName', scenario.companySearch,scenario.companyCode,'@clientSearch');

        if(scenario.showDuplicatedNotification){
          cy.get("#businessName")          
          .should('have.attr', 'aria-invalid', 'true')
          .should('have.attr', 'invalid-text', 'Client already exists');

          cy.get("#businessName")
          .shadow()
          .find('svg').should('exist');
        }

        cy.get('cds-inline-notification#bcRegistrySearchNotification').should('not.exist');

        if(scenario.showBcRegDownNotification){
          cy.wait('@clientDetails');
          cy.wait(5000);
          cy.get('cds-inline-notification#bcRegistryDownNotification').should('exist');
        }

        if(scenario.showData){

          const success = Object.entries(scenario)
          .filter(([key, value]) => key.startsWith('show') && key.endsWith('Notification'))
          .map(([key, value]) => value)
          .every(value => value === false);

          cy.get('.read-only-box > cds-inline-notification#readOnlyNotification').should(success || scenario.showDuplicatedNotification ? 'exist' : 'not.exist');
          
          cy.get(`.read-only-box > #legalType > .title-group-01 > .label-01`)
          .should('exist')
          .and('have.text', 'Type');

          cy.get(`.read-only-box > #legalType > .body-compact-01`)
          .should('exist')    
          .and('have.text', scenario.type);

          cy.get(`.read-only-box > #registrationNumber > .title-group-01 > .label-01`)
          .should('exist')
          .and('have.text', 'Registration number');

          cy.get(`.read-only-box > #registrationNumber > .body-compact-01`)
          .should('exist')
          .and('have.text', scenario.companyCode);

          if(scenario.showUnknowNotification){
            cy.get('.read-only-box > cds-inline-notification#unknownStandingNotification')
            .should('exist');
            //TODO: check the text and style maybe?!
          }

          if(scenario.showNotGoodStandingNotification){
            cy.get('.read-only-box > cds-inline-notification#notGoodStandingNotification')
            .should('exist');
            //TODO: check the text and style maybe?!
          }

          cy.get(`.read-only-box > #goodStanding > .title-group-01 > .label-01`)    
          .should('exist')
          .and('have.text', 'BC Registries standing');

          cy.get(`.read-only-box > #goodStanding > div.internal-grouping-01 > .body-compact-01`)
          .should('exist')
          .and('have.text', scenario.standing);

        }

        if(scenario.showBirthdate){
          cy.get("#birthdate").should("be.visible");
        }

        cy.get('#workSafeBCNumber').should('exist').and("have.value", "");
        cy.get('#doingBusinessAs').should(scenario.dba ? 'not.exist' :'exist')
        cy.get('#acronym').should('exist').and("have.value", "");

      });

    });

  });

});