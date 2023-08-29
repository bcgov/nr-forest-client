/// <reference types="cypress-get-by-label" />
import { Then, When } from "@badeball/cypress-cucumber-preprocessor";
import "cypress-get-by-label/commands";
import "./common";
import { CustomWorld } from "./common";

When(
  "I select the option that says I have an unregistered sole proprietorship",
  () => {
    cy.contains("I have an unregistered sole proprietorship").click();
  }
);

Then(
  "my name is displayed under the label 'Unregistered proprietorship'",
  function (this: CustomWorld) {
    cy.get(".grouping-04")
      .contains("Unregistered proprietorship")
      .should("be.visible");

    cy.get(".grouping-04")
      .contains(
        `${this.contactList[0].firstName} ${this.contactList[0].lastName}`
      )
      .should("be.visible");
  }
);
