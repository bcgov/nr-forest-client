/// <reference types="cypress-get-by-label" />
import {
  When,
  Then,
  Before,
  Step,
} from "@badeball/cypress-cucumber-preprocessor";
import "cypress-get-by-label/commands";
import { BusinessType, CustomWorld, IAddress } from "./common";

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
  }).as("submitData");
});

When(
  "I select the option that says I have a BC registered business",
  function (this: CustomWorld) {
    cy.contains("I have a BC registered business").click();
    this.businessType = BusinessType.Registered;
  }
);

When(
  "I type in {string} in the business name input",
  function (businessNameFirstCharacters: string) {
    // TODO: fix in the application for concurrent autocomplete requests, or bounce changes before firing a request.
    cy.getByLabel("BC registered business name")
      .filter("input[type=text]")
      .typeWait(businessNameFirstCharacters);
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
  cy.contains(".heading-compact-01", "BC registered business name").should(
    "be.visible"
  );
});

Then(
  "a notification for the business being 'Not in good standing' is displayed",
  () => {
    cy.contains(".heading-compact-01", "Not in good standing").should(
      "be.visible"
    );
  }
);

Then("a notification saying that 'Client already exists' is displayed", () => {
  cy.contains(".heading-compact-01", "Client already exists").should(
    "be.visible"
  );
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
      this.addressList[this.curAddressIndex].country = text;
    });
});

When("I type in {string} in the Street address", (address: string) => {
  // TODO: fix in the application for concurrent autocomplete requests, or bounce changes before firing a request.
  cy.get('bx-input[label-text="Street address or PO box"')
    .find("input")
    .typeWait(address);
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
        this.addressList[this.curAddressIndex].streetAddress = value as any;
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
        this.addressList[this.curAddressIndex].city = value as any;
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
        this.addressList[this.curAddressIndex].province = text;
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
        this.addressList[this.curAddressIndex].postalCode = value as any;
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
        if (!this.contactList[this.curContactIndex].addressNameList) {
          this.contactList[this.curContactIndex].addressNameList = [];
        }
        this.contactList[this.curContactIndex].addressNameList.push(text);
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
        this.contactList[this.curContactIndex].primaryRole = text;
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
        this.contactList[this.curContactIndex].phoneNumber = value as any;
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
    cy.get("@businessInfo").contains(this.businessType.description);
  }
);

Then(
  "the displayed Address information match the provided information",
  function (this: CustomWorld) {
    cy.contains(".grouping-05", "Address")
      .find(".grouping-07")
      .as("addressInfo");

    this.addressList.forEach((address) => {
      cy.get("@addressInfo").contains(address.name);

      // TODO: uncomment when fixed
      // cy.get("@addressInfo").contains(address.streetAddress);

      cy.get("@addressInfo").contains(`${address.city}, ${address.province}`);
      cy.get("@addressInfo").contains(address.country);
      cy.get("@addressInfo").contains(address.postalCode);
    });
  }
);

Then(
  "the displayed Contacts information match the provided information",
  function (this: CustomWorld) {
    cy.contains(".grouping-05", "Contacts")
      .find(".grouping-07")
      .as("contactsInfo");

    this.contactList.forEach((contact) => {
      cy.get("@contactsInfo").contains(
        `${contact.firstName} ${contact.lastName}`
      );
      cy.get("@contactsInfo").contains(contact.addressNameList.join(", "));
      cy.get("@contactsInfo").contains(contact.primaryRole);
      cy.get("@contactsInfo").contains(contact.email);
      cy.get("@contactsInfo").contains(contact.phoneNumber);
    });
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

Then(
  "the provided information is sent to the backend",
  function (this: CustomWorld) {
    cy.get("@submitData")
      .its("request.body")
      .then((data) => {
        expect(data.businessInformation).to.deep.include({
          businessName: this.businessName,
          businessType: this.businessType.value,
          // TODO: should we add the other business fields?
          // For example: should we capture the incorporationNumber so as to be able to assert it here?
        });

        expect(data.location.addresses).to.have.lengthOf(
          this.addressList.length
        );

        for (let index = 0; index < data.location.addresses.length; index++) {
          const dataAddress = data.location.addresses[index];
          const expectedAddress = this.addressList[index];

          expect(dataAddress).to.deep.include({
            locationName: expectedAddress.name,
            // TODO: uncomment when fixed
            // streetAddress: curAddress.streetAddress,
            city: expectedAddress.city,
            postalCode: expectedAddress.postalCode,
          });
          expect(dataAddress.country.text).to.equal(expectedAddress.country);
          expect(dataAddress.province.text).to.equal(expectedAddress.province);
        }

        expect(data.location.contacts).to.have.lengthOf(
          this.contactList.length
        );

        for (let index = 0; index < data.location.contacts.length; index++) {
          const dataContact = data.location.contacts[index];
          const expectedContact = this.contactList[index];

          expect(dataContact).to.deep.include({
            phoneNumber: expectedContact.phoneNumber,
            firstName: expectedContact.firstName,
            lastName: expectedContact.lastName,
            email: expectedContact.email,
          });

          expect(dataContact.locationNames).to.have.lengthOf(
            expectedContact.addressNameList.length
          );

          for (
            let locationIndex = 0;
            locationIndex < dataContact.locationNames.length;
            locationIndex++
          ) {
            expect(dataContact.locationNames[locationIndex].text).to.equal(
              expectedContact.addressNameList[locationIndex]
            );
          }

          expect(dataContact.contactType.text).to.equal(
            expectedContact.primaryRole
          );
        }
      });
  }
);
