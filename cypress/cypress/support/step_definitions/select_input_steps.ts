import { Then, Step, BeforeStep } from "@badeball/cypress-cucumber-preprocessor";

let idir = true;

BeforeStep({ tags: "@loginAsBCeID or @loginAsBCSC" }, function () {
  idir = false;
});
BeforeStep({ tags: "@loginAsEditor or @loginAsViewer or @loginAsAdmin" }, function () {
  idir = true;
});

/* Select Input Steps */

Then('I select {string} from the {string} form input', (value: string, input: string) => {
  cy.contains('label', input).then(($label) => {
    const parentShadow = $label[0].getRootNode();
    cy.wrap(parentShadow).find("[part='trigger-button']").click();
    cy.wrap(parentShadow).parent().find(`cds-combo-box-item[data-value="${value}"]`).click();
  });
});

Then(
  'I select {string} from the {string} form input for the {string}',
  (value: string, fieldLabel: string, sectionTitle: string) => {

    if (sectionTitle === 'Primary location' || sectionTitle === 'Primary contact') {      
      cy.get('div.frame-01:first').within(() => {
        Step(this, `I select "${value}" from the "${fieldLabel}" form input`);
      });
    } else {
      cy.get(`[data-text="${sectionTitle}"]`).within(() => {
        Step(this, `I select "${value}" from the "${fieldLabel}" form input`);
      });
    }

});

Then('I select {string} from the {string} multiselect', (value: string, input: string) => {
  cy.contains('label', input).then(($label) => {
    const parentShadow = $label[0].getRootNode();
    cy.wrap(parentShadow).find("[part='trigger-button']").click();
    cy.wrap(parentShadow).parent().find(`cds-multi-select-item[data-value="${value}"]`).as("item");
    cy.get("@item").then(($item) => {
      // Prevents clicking the item if it's already selected
      if (!$item.prop("selected")) {
        cy.get("@item").click();
      }
    });
  });
});

Then(
  'I select {string} from the {string} multiselect for the {string}',
  (value: string, fieldLabel: string, sectionTitle: string) => {

    if (sectionTitle === 'Primary location' || sectionTitle === 'Primary contact') {      
      cy.get('div.frame-01:first').within(() => {
        Step(this, `I select "${value}" from the "${fieldLabel}" multiselect`);
      });
    } else {  
      cy.get(`[data-text="${sectionTitle}"]`).within(() => {
        Step(this, `I select "${value}" from the "${fieldLabel}" multiselect`);
      });      
    }

});
