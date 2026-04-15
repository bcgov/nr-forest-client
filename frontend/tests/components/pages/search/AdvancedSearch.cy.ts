import AdvancedSearch from "@/pages/search/AdvancedSearch.vue";

// Carbon
import "@carbon/web-components/es/components/button/index";
import "@carbon/web-components/es/components/modal/index";
import type { VueWrapper } from "@vue/test-utils";

const listeners = {
  "onUpdate:active": () => {},
  "onUpdate:filters": () => {},
};

describe("<advanced-search />", () => {
  beforeEach(() => {
    cy.spy(listeners, "onUpdate:active").as("updateActive");
    cy.spy(listeners, "onUpdate:filters").as("updateFilters");

    cy.intercept("GET", "/api/clients/client-users?userId=*", {
      fixture: "clientUsers-autocomplete.json",
    }).as("getClientUsers");
    cy.intercept("GET", "/api/codes/client-statuses", {
      fixture: "clientStatuses.json",
    }).as("getClientStatuses");
    cy.intercept("GET", "/api/codes/client-types/legacy", {
      fixture: "legacyClientTypes.json",
    }).as("getClientTypes");
    cy.intercept("GET", "/api/codes/identification-types/legacy", {
      fixture: "legacyIdentificationTypes.json",
    }).as("getLegacyIdentificationTypes");
  });
  let filters = {};
  const getDefaultProps = () => {
    filters = {};
    return {
      active: false,
      filters,
      "onUpdate:active": listeners["onUpdate:active"],
      "onUpdate:filters": listeners["onUpdate:filters"],
    };
  };
  let currentProps: ReturnType<typeof getDefaultProps>;

  const mount = (props = getDefaultProps()) => {
    currentProps = props;
    return cy
      .mount(AdvancedSearch, {
        props,
      })
      .its("wrapper")
      .as("vueWrapper");
  };

  const mountActive = (props = getDefaultProps()) => {
    props.active = true;
    mount(props);
  };

  it("renders the component", () => {
    mount();
    cy.get("cds-modal#advanced-modal").should("exist");
  });

  it("gets displayed when v-model:active is true", () => {
    mountActive();
    cy.get("cds-modal#advanced-modal").should("be.visible");
  });

  it("gets hidden when v-model:active is false", () => {
    mount();
    cy.get("cds-modal#advanced-modal").should("not.be.visible");
  });

  it("sets active to false when the modal gets closed", () => {
    mountActive();
    cy.get("cds-modal#advanced-modal").should("have.attr", "open");

    cy.get("cds-modal-close-button").click();

    cy.get("cds-modal#advanced-modal").should("not.have.attr", "open");

    // sets active to false
    cy.get("@updateActive").should("be.calledWith", false);
  });

  it("updates v-model:filters as filter input values are changed", () => {
    mountActive();

    cy.fillFormEntry("#clientName", "sample-clientName");
    cy.fillFormEntry("#firstName", "sample-firstName");
    cy.fillFormEntry("#middleName", "sample-middleName");
    cy.selectAutocompleteEntry(
      "#userId",
      "jry",
      "IDIR\\\\JRYAN",
      "@getClientUsers",
    );
    cy.selectFormEntry("#clientIdType", "British Columbia Drivers Licence");
    cy.fillFormEntry("#clientIdentification", "sample-clientIdentification");
    cy.selectFormEntry("#clientType", "Corporation");
    cy.selectFormEntry("#clientStatus", "Suspended");
    cy.fillFormEntry("#contactName", "sample-contactName");
    cy.fillFormEntry("#emailAddress", "sample-emailAddress");

    cy.fillFormEntry("#updatedFromDateYear", "2025");
    cy.fillFormEntry("#updatedFromDateMonth", "04");
    cy.fillFormEntry("#updatedFromDateDay", "15");

    cy.fillFormEntry("#updatedToDateYear", "2026");
    cy.fillFormEntry("#updatedToDateMonth", "04");
    cy.fillFormEntry("#updatedToDateDay", "15");

    cy.wrap(filters).should("deep.equal", {
      clientName: "sample-clientName",
      firstName: "sample-firstName",
      middleName: "sample-middleName",
      userId: "IDIR\\JRYAN",
      clientIdType: ["BCDL"],
      clientIdentification: "sample-clientIdentification",
      clientType: ["C"],
      clientStatus: ["SPN"],
      contactName: "sample-contactName",
      emailAddress: "sample-emailAddress",
      updatedFromDate: "2025-04-15",
      updatedToDate: "2026-04-15",
    });
  });

  it("disables the Search button when something is invalid", () => {
    mountActive();

    cy.fillFormEntry("#clientName", "sample !@#$");

    cy.get("#search-advanced-btn")
        .shadow()
        .find("button")
        .should("be.disabled");
  });

  it("enables the Search button when validation errors are fixed", () => {
    mountActive();

    cy.fillFormEntry("#clientName", "sample !@#$");

    cy.get("#search-advanced-btn")
        .shadow()
        .find("button")
        .should("be.disabled");

    cy.fillFormEntry("#clientName", "sample fixed");

    cy.get("#search-advanced-btn")
        .shadow()
        .find("button")
        .should("be.enabled");
  });

  it("emits a search event", () => {
    mountActive();

    cy.fillFormEntry("#clientName", "sample value");

    cy.get("#search-advanced-btn").click();

    cy.get<VueWrapper>("@vueWrapper").should((vueWrapper) => {
      // the search event was emitted once
      expect(vueWrapper.emitted("search")).to.be.an("array").that.has.length(1);
    });
  });
});