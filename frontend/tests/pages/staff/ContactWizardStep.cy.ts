import ContactsWizardStep from "@/pages/staffform/ContactsWizardStep.vue";
import type { Contact, Address, FormDataDto } from "@/dto/ApplyClientNumberDto";
import type { ModalNotification } from "@/dto/CommonTypesDto";
import { useEventBus } from "@vueuse/core";

describe('<ContactsWizardStep />', () => {

  let bus: ReturnType<typeof useEventBus<ModalNotification>> = useEventBus<ModalNotification>("modal-notification");

  const individualFormData: FormDataDto = {
    businessInformation:{
      district:"",
      businessType:"U",
      legalType:"SP",
      clientType:"I",
      registrationNumber:"",
      businessName:"Jhonathan James Wick",
      goodStandingInd:"Y",
      birthdate:"1985-07-17",
      firstName:"Jhonathan",
      middleName:"James",
      lastName:"Wick",
      identificationType:"CDDL",
      identificationProvince:"NB",
      identificationCountry:"CA",
      clientIdentification:"99999999"
    },
    location:{
      addresses:[
        {
          locationName:"Main Address",
          complementaryAddressOne:"",
          complementaryAddressTwo:null,
          streetAddress:"3925 Dieppe Ave",
          country:{"value":"CA","text":"Canada"},
          province:{"value":"NS","text":"Nova Scotia"},
          city:"Lumsden",
          postalCode:"M2W5E5",
          businessPhoneNumber:"",
          secondaryPhoneNumber:"",
          faxNumber:"",
          emailAddress:"",
          notes:"",
          index: 0
        } as Address
      ],
      contacts:[
        {
          locationNames:[{value:"0",text:"Mailing address"}],
          contactType:{value:"",text:""},
          firstName:"Jhonathan",
          lastName:"James Wick",
          phoneNumber:"",
          email:"",
          index: 0
        } as Contact
      ]
    }
  };

  const baseData = [
    {
      mail:'contact1@mail.ca',
      phone1:'1234567890',
      phone2:'1234567890',
      fax:'1234567890',
      role:'Person',
      address:individualFormData.location.addresses[0].locationName,
    }
  ]

  const fillFormEntry = (field: string, value: string) => {
    cy.get(field)
    .should("exist")
    .shadow()
    .find('input')
    .should("have.value", "")
    .type(value);

    cy.get(field)    
    .shadow()
    .find('input').blur();
  }

  const selectFormEntry = (field: string, value: string, box: boolean) => {
    cy.get(field).find("[part='trigger-button']").click();

    if(!box){
      cy.get(field)
        .find(`cds-combo-box-item[data-value="${value}"]`)
        .click();
    }else{
      cy.get(field)
        .find(`cds-multi-select-item[data-value="${value}"]`)
        .click();
      cy.get(field).click();
    }
  }

  const addContact = (addressId: number, name: string) => {
    cy.get('.body-02').should('have.text', "Contacts can be added to the applicant's account");
    cy.contains("Add another contact").should("be.visible").click();      

    // Focus accordion title
    // TODO: uncomment next line when the following issue is fixed: https://github.com/cypress-io/cypress/issues/26383
    // cy.focused().should("contain.text", "Additional contact");

    cy.get(`#firstName_${addressId}`)
      .should("be.visible")
      .shadow()
      .find("input")
      .should("have.value", "")
      .type(name);
  };

  beforeEach(() => {

    cy.viewport(1056, 768);
    
    cy.intercept('GET', '/api/codes/contact-types?page=0&size=250', {
      fixture: 'roles.json',
    })
    .as('getContactTypes');
    
    bus.on((payload) =>  payload.handler());
  });

  afterEach(() => bus.reset());

  it('renders the ContactsWizardStep component for individual', () => {
    // render the component
    cy.mount(ContactsWizardStep,{
      props: {
        data: individualFormData,
        active: true,
      },
    });
    
    // Assert that the main component is rendered
    cy.get(".frame-01").should("exist");

    cy.wait("@getContactTypes");

    // Assert that the first field is displayed
    cy.get(".text-fullName_0")
    .should("exist")
    .should("have.text", "Jhonathan James Wick");
  });

  it('fill the first contact information for individual',() =>{
    // render the component
    cy.mount(ContactsWizardStep,{
      props: {
        data: individualFormData,
        active: true,
      },
    });

    // Assert that the main component is rendered
    cy.get(".frame-01").should("exist");

    cy.wait("@getContactTypes");

    // Fill the first contact information
    fillFormEntry('#emailAddress_0', baseData[0].mail);
    fillFormEntry('#businessPhoneNumber_0', baseData[0].phone1);
    fillFormEntry('#secondaryPhoneNumber_0', baseData[0].phone2);
    fillFormEntry('#faxNumber_0', baseData[0].fax);

    selectFormEntry('#role_0', baseData[0].role,false);
    selectFormEntry('#addressname_0', baseData[0].address,true);

  });

  it('add extra contacts until max', () => {

    // Set the maximum number of contacts
    const maxContacts = 5;

    // render the component
    cy.mount(ContactsWizardStep,{
      props: {
        data: individualFormData,
        active: true,
        maxContacts,
      },
    });
    
    // Assert that the main component is rendered
    cy.get(".frame-01").should("exist");

    // Wait for the contact types to be loaded
    cy.wait("@getContactTypes");

    // The add contact button should be visible
    cy.contains("Add another contact").should("be.visible");

    // Add contacts until the maximum is reached
    for(let index = 1; index < maxContacts; index++){
      addContact(index, `Contact${index}`);
    }

    // The add contact button should be hidden
    cy.contains("Add another contact").should("not.exist");
    // The message should be displayed
    cy.get('.body-02').should('have.text', `You can only add a maximum of ${maxContacts} contacts.`);

  });

  it('remove contacts', () => {
    
    //Set a maximum of 5 contacts
    const maxContacts = 5;

    // render the component
    cy.mount(ContactsWizardStep,{
      props: {
        data: individualFormData,
        active: true,
        maxContacts,
      },
    });
    
    // Assert that the main component is rendered
    cy.get(".frame-01").should("exist");

    // Wait for the contact types to be loaded
    cy.wait("@getContactTypes");

    // The delete button group should be visible
    cy.get('.grouping-06').should("be.visible");

    // Remove each contact and check to see if it was removed
    for(let index = 1; index < maxContacts; index++){
      cy.get('.grouping-06').find(`cds-button#deleteContact_${index}`).should("be.visible").click();
      cy.get(`#firstName_${index}`).should("not.exist");
    }
    
  });

});

