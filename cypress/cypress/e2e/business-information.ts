/// <reference types="cypress-get-by-label" />
import { Then, When } from "@badeball/cypress-cucumber-preprocessor";
import "cypress-get-by-label/commands";
import "./common";

When(
  "I select the option that says I have an unregistered sole proprietorship",
  () => {
    cy.contains("I have an unregistered sole proprietorship").click();
  }
);

Then(
  "my name is displayed under the label 'Unregistered proprietorship'",
  () => {
    cy.contains("Unregistered proprietorship").should("be.visible");

    cy.get("input[type=text]")
      .should("have.value", "Test Uat") // This name comes from the information in the token
      .should("be.visible");
  }
);
