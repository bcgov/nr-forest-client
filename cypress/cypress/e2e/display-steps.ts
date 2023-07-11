import { Given, Then } from "@badeball/cypress-cucumber-preprocessor";

Given("I am on the form page", () => {
  cy.visit("/");
});

Given("the width of screen is {int} pixels", (width: number) => {
  cy.viewport(width, 660);
});

Then("steps are displayed horizontally", () => {
  cy.get(".wizard-wrap-item")
    .then((elements) =>
      elements.map((_index, el) => el.getBoundingClientRect())
    )
    .as("stepContainers");

  cy.get<DOMRect[]>("@stepContainers").should("have.lengthOf", 4);

  cy.get<DOMRect[]>("@stepContainers").then((stepContainers) => {
    expect(stepContainers[0].top)
      .to.eq(stepContainers[1].top)
      .and.eq(stepContainers[2].top)
      .and.eq(stepContainers[3].top);

    expect(stepContainers[0].left).to.be.lessThan(stepContainers[1].left);
    expect(stepContainers[1].left).to.be.lessThan(stepContainers[2].left);
    expect(stepContainers[2].left).to.be.lessThan(stepContainers[3].left);
  });
});

Then("steps are displayed vertically", () => {
  cy.get(".wizard-wrap-item")
    .then((elements) =>
      elements.map(function () {
        return this.getBoundingClientRect();
      })
    )
    .as("stepContainers");

  cy.get<DOMRect[]>("@stepContainers").should("have.lengthOf", 4);

  cy.get<DOMRect[]>("@stepContainers").then((stepContainers) => {
    expect(stepContainers[0].left)
      .to.eq(stepContainers[1].left)
      .and.eq(stepContainers[2].left)
      .and.eq(stepContainers[3].left);

    expect(stepContainers[0].top).to.be.lessThan(stepContainers[1].top);
    expect(stepContainers[1].top).to.be.lessThan(stepContainers[2].top);
    expect(stepContainers[2].top).to.be.lessThan(stepContainers[3].top);
  });
});
