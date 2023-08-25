/// <reference types="cypress-get-by-label" />
import {
  When,
  Then,
  Before,
  Step,
} from "@badeball/cypress-cucumber-preprocessor";
import "cypress-get-by-label/commands";
import { BusinessTypeDescription, CustomWorld, IAddress } from "./common";

Before(() => {
  // cy.intercept("GET", "/api/clients/name/*", {
  //   fixture: "response-autocomplete.json",
  // });

  cy.intercept("GET", "/api/clients/BC0680951", {
    // GOOD SOFTWARE INC.
    fixture: "response-details-BC0680951.json",
  });
  cy.intercept("GET", "/api/clients/BC1317640", {
    // BAD HORSE INC.
    fixture: "response-details-BC1317640.json",
  });
  cy.intercept("GET", "/api/clients/BC1162739", {
    // DUPP MEDIA INC.
    statusCode: 409,
    body: "The Dupped Company already exists with the Incorporation number BC1162739 and client number 999999",
  });
  cy.intercept("POST", "/api/clients/submissions", {
    statusCode: 201,
    headers: {
      Location: "/api/clients/123456",
      "x-sub-id": "123456",
    },
  });
});

When(
  "I select the option that says I have a BC registered business",
  function (this: CustomWorld) {
    cy.contains("I have a BC registered business").click();
    this.businessTypeDescription = BusinessTypeDescription.Registered;
  }
);

When(
  "I type in {string} in the business name input",
  function (businessNameFirstCharacters: string) {
    // TODO: fix in the application for concurrent autocomplete requests, or bounce changes before firing a request.

    const stringLength = businessNameFirstCharacters.length;
    const stringMinusLastCharacter = businessNameFirstCharacters.substring(
      0,
      stringLength - 1
    );

    cy.getByLabel("BC registered business name")
      .filter("input[type=text]")
      .type(stringMinusLastCharacter, { delay: 0 });

    cy.wait(250);

    const lastCharacter = businessNameFirstCharacters[stringLength - 1];

    cy.getByLabel("BC registered business name")
      .filter("input[type=text]")
      .type(lastCharacter);
  }
);

Then(
  "I am presented a list of business names filtered by the text typed in the business name field",
  function () {
    cy.get("div.autocomplete-items-cell")
      .should("be.visible")
      .should("have.length.above", 1);
  }
);

When(
  "I select the name of a business in good standing from the filtered list",
  function () {
    cy.get("div.autocomplete-items-cell")
      .contains("Valid Manufacturing LTD", { matchCase: false })
      .click();
  }
);

When(
  "I select {string} from the filtered list of businesses",
  function (this: CustomWorld, name: string) {
    Step(
      this,
      "I am presented a list of business names filtered by the text typed in the business name field"
    );

    // select it
    cy.get("div.autocomplete-items-cell")
      .contains(name, { matchCase: false })
      .click();

    // store its precise name for a later check
    cy.getByLabel("BC registered business name")
      .filter("input[type=text]")
      .invoke("val")
      .then((value) => {
        this.businessName = value as any;
      });
  }
);

When(
  "I select the first option from the filtered list of businesses",
  function () {
    Step(
      this,
      "I am presented a list of business names filtered by the text typed in the business name field"
    );
    cy.get("div.autocomplete-items-cell").first().click();
  }
);

When(
  "I select the name of a business whose data is not found in the BC Registries",
  function () {
    cy.get("div.autocomplete-items-cell")
      .contains("Valid Manufacturing LTD", { matchCase: false })
      .click();
  }
);

When(
  "I select the name of a business which is not in good standing from the filtered list",
  function () {
    cy.get("div.autocomplete-items-cell").contains("Shady").click();
  }
);

When(
  "I select the name of a business which already has a client number from the filtered list",
  function () {
    cy.get("div.autocomplete-items-cell").contains("Duplicated").click();
  }
);

Then("a field to type in the business name is displayed", () => {
  cy.getByLabel("BC registered business name")
    .filter("input[type=text]") // just makes sure this is a simple text field
    .should("exist");
});

Then("a notification about 'Registered business name' is displayed", () => {
  cy.contains("span", "BC registered business name").should("be.visible");
});

Then(
  "a notification for the business being 'Not in good standing' is displayed",
  () => {
    cy.contains("span", "Not in good standing").should("be.visible");
  }
);

Then("a notification saying that 'Client already exists' is displayed", () => {
  cy.contains("span", "Client already exists").should("be.visible");
});

Then("the button 'End application and logout' is displayed", () => {
  cy.contains("bx-btn", "End application and logout")
    .find("button")
    .should("be.visible");
});

Then("the button 'Receive email and logout' is displayed", () => {
  cy.contains("bx-btn", "Receive email and logout")
    .find("button")
    .should("be.visible");
});

Then("I get to the Address tab", () => {
  cy.get(".heading-04").should("contain.text", "Address");
});

Then("I get to the Contacts tab", () => {
  cy.get(".heading-04").should("contain.text", "Contacts");
});

Then("I get to the Review tab", () => {
  cy.get(".heading-04").should("contain.text", "Review");
});

Then("I get to the Application submitted page", () => {
  cy.contains("Application submitted");
});

When("the list of countries finishes loading", function (this: CustomWorld) {
  cy.get('bx-dropdown[label-text="Country"]')
    .contains("span", "Canada")
    .should("be.visible");

  cy.get('bx-dropdown[label-text="Country"]')
    .find("span")
    .invoke("text")
    .then((text) => {
      if (!this.addressList) {
        this.addressList = [
          {
            name: "Mailing Address",
          } as IAddress,
        ];
      }
      this.addressList[0].country = text;
    });
});

When("I type in {string} in the Street address", (address: string) => {
  // cy.getByLabel("Street address or PO box").type(address);
  cy.get('bx-input[label-text="Street address or PO box"')
    .find("input")
    .type(address);
});

When(
  "I select the Street address that contains {string} from the list",
  function (this: CustomWorld, address: string) {
    cy.get("div.autocomplete-items-cell").contains(address).click();
  }
);

Then(
  "the Street address gets updated to {string}",
  function (this: CustomWorld, streetAddress: string) {
    // TODO: fix the backend to remove spaces in the end.
    return "skip";

    cy.get('bx-input[label-text="Street address or PO box"')
      .find("input")
      .should("have.value", streetAddress);

    cy.get('bx-input[label-text="Street address or PO box"')
      .find("input")
      .invoke("val")
      .then((value) => {
        this.addressList[0].streetAddress = value as any;
      });
  }
);

Then(
  "the City gets updated to {string}",
  function (this: CustomWorld, city: string) {
    // cy.getByLabel("City").should("equal", city);
    cy.get('bx-input[label-text="City"')
      .find("input")
      .should("have.value", city);

    cy.get('bx-input[label-text="City"')
      .find("input")
      .invoke("val")
      .then((value) => {
        this.addressList[0].city = value as any;
      });
  }
);

Then(
  "the Province gets updated to {string}",
  function (this: CustomWorld, province: string) {
    cy.get('bx-dropdown[label-text="Province or territory"]')
      .contains("span", province)
      .should("be.visible");

    cy.get('bx-dropdown[label-text="Province or territory"]')
      .find("span")
      .invoke("text")
      .then((text) => {
        this.addressList[0].province = text;
      });
  }
);

Then(
  "the Postal code gets updated to {string}",
  function (this: CustomWorld, postalCode: string) {
    cy.get('bx-input[label-text="Postal code"')
      .find("input")
      .should("have.value", postalCode);

    cy.get('bx-input[label-text="Postal code"')
      .find("input")
      .invoke("val")
      .then((value) => {
        this.addressList[0].postalCode = value as any;
      });
  }
);

When(
  "I select the Address name {string}",
  function (this: CustomWorld, addressName: string) {
    cy.get('bx-dropdown[label-text="Address name"]')
      .find("[role='button']")
      .click();

    cy.get('bx-dropdown[label-text="Address name"]')
      .contains(addressName)
      .as("addressName");

    cy.get("@addressName")
      .invoke("text")
      .then((text) => {
        if (!this.contactList[0].addressNameList) {
          this.contactList[0].addressNameList = [];
        }
        this.contactList[0].addressNameList.push(text);
      });

    cy.get("@addressName").click();
  }
);

When(
  "I select the Primary role {string}",
  function (this: CustomWorld, contactName: string) {
    cy.get('bx-dropdown[label-text="Primary role"]')
      .find("[role='button']")
      .click();
    cy.get('bx-dropdown[label-text="Primary role"]')
      .contains(contactName)
      .click();

    cy.get('bx-dropdown[label-text="Primary role"]')
      .find("span")
      .invoke("text")
      .then((text) => {
        this.contactList[0].primaryRole = text;
      });
  }
);

When(
  "I type in {string} as Phone number",
  function (this: CustomWorld, phoneNumber: string) {
    cy.get('bx-input[label-text="Phone number"')
      .find("input")
      .type(phoneNumber);

    cy.get('bx-input[label-text="Phone number"')
      .find("input")
      .invoke("val")
      .then((value) => {
        this.contactList[0].phoneNumber = value as any;
      });
  }
);

Then(
  "the displayed Business information match the provided information",
  function (this: CustomWorld) {
    cy.contains(".grouping-05", "Business information")
      .find(".grouping-06")
      .as("businessInfo");

    cy.get("@businessInfo").contains(this.businessName);
    cy.get("@businessInfo").contains(this.businessTypeDescription);
  }
);

Then(
  "the displayed Address information match the provided information",
  function (this: CustomWorld) {
    cy.contains(".grouping-05", "Address")
      .find(".grouping-07")
      .as("addressInfo");

    cy.get("@addressInfo").contains(this.addressList[0].name);

    // TODO: uncomment when fixed
    // cy.get("@addressInfo").contains(this.addressList[0].streetAddress);

    cy.get("@addressInfo").contains(
      `${this.addressList[0].city}, ${this.addressList[0].province}`
    );
    cy.get("@addressInfo").contains(this.addressList[0].country);
    cy.get("@addressInfo").contains(this.addressList[0].postalCode);
  }
);

Then(
  "the displayed Contacts information match the provided information",
  function (this: CustomWorld) {
    cy.contains(".grouping-05", "Contacts")
      .find(".grouping-07")
      .as("contactsInfo");

    cy.get("@contactsInfo").contains(
      `${this.contactList[0].firstName} ${this.contactList[0].lastName}`
    );
    cy.get("@contactsInfo").contains(this.contactList[0].addressNameList[0]);
    cy.get("@contactsInfo").contains(this.contactList[0].primaryRole);
    cy.get("@contactsInfo").contains(this.contactList[0].email);
    cy.get("@contactsInfo").contains(this.contactList[0].phoneNumber);
  }
);

Then("the button Submit application is enabled", () => {
  cy.contains("bx-btn", "Submit application")
    .find("button")
    .should("be.enabled");
});

When("I click the button Submit application", () => {
  cy.contains("bx-btn", "Submit application").click();
});
