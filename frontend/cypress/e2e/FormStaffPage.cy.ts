import type { StaticResponse } from "../../node_modules/cypress/types/net-stubbing";

/* eslint-disable no-undef */
describe("Staff Form", () => {
  
  beforeEach(() => {

    cy.visit("/");
    cy.wait(500);
    cy.get("#landing-title").should("contain", "Forests Client Management System");
    cy.get("#landing-subtitle").should(
      "contain",
      "Create and manage client accounts"
    );

    cy.viewport(1920, 1080);
  });

  it("CLIENT_EDITOR should be able to see the button", () => {

    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_EDITOR"],
    });

    // Check if the Create client button is visible
    cy.get('#menu-list-staff-form')
      .should('be.visible')
      .find('span')
      .should('contain', 'Create client');
    
  });

  it("CLIENT_ADMIN should be able to see the button", () => {

    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_ADMIN"],
    });

    // Check if the Create client button is visible
    cy.get('#menu-list-staff-form')
      .should('be.visible')
      .find('span')
      .should('contain', 'Create client');
    
  });

  it("CLIENT_VIEWER should not be able to see the button", () => {

    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_VIEWER"],
    });

    // Check if the Create client button is not visible and cannot be found
    cy.get('#menu-list-staff-form').should('not.exist');
    
  });

  it("CLIENT_EDITOR should be able to click the button", () => {

    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_EDITOR"],
    });

    // Check if the Create client button is visible
    cy.get('#menu-list-staff-form')
      .should('be.visible')
      .click();

      cy.get('h1')
      .should('be.visible')      
      .should('contain', ' Create client ');
    
  });

  it("CLIENT_ADMIN should be able to click the button", () => {

    cy.login("uattest@gov.bc.ca", "Uat Test", "idir", {
      given_name: "James",
      family_name: "Baxter",
      "cognito:groups": ["CLIENT_ADMIN"],
    });

    // Check if the Create client button is visible
    cy.get('#menu-list-staff-form')
      .should('be.visible')
      .click();

      cy.get('h1')
      .should('be.visible')      
      .should('contain', ' Create client ');
    
  });

});
