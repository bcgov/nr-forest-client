import type {
  ClientDetails,
  ClientLocation,
  CodeNameType,
  IndexedRelatedClient,
  RelatedClientEntry,
  SaveEvent,
} from "@/dto/CommonTypesDto";
import ClientRelationshipForm from "@/pages/client-details/ClientRelationshipForm.vue";
import type { GoToTab } from "@/pages/client-details/shared";

// Carbon
import "@carbon/web-components/es/components/button/index";
import "@carbon/web-components/es/components/modal/index";
import type { VueWrapper } from "@vue/test-utils";
import * as vueRouter from "vue-router";

describe("<client-relationship-form />", () => {
  let clientDetails: ClientDetails;
  before(() => {
    cy.fixture("client-details/michael-scott.json").then((data) => {
      clientDetails = data;
    });
  });
  const getDefaultProps = () => ({
    locationIndex: "01",
    index: "0",
    data: {
      id: "1234ASDF",
      client: {
        client: { code: "1234" } as CodeNameType,
        location: null,
      },
      relatedClient: {
        client: null,
        location: null,
      },
      relationship: null,
      percentageOwnership: null,
      hasSigningAuthority: null,
      isMainParticipant: true,
    } as RelatedClientEntry,
    clientData: clientDetails,
    validations: [],
  });

  let currentProps: ReturnType<typeof getDefaultProps> = null;
  let locationIndex: string, index: string;

  const goToTabStub: GoToTab = () => {};
  const goToTabWrapper = {
    goToTab: goToTabStub,
  };

  const setSpyGoToTab = () => cy.spy(goToTabWrapper, "goToTab").as("goToTab");

  let spyGoToTab: ReturnType<typeof setSpyGoToTab>;

  const router = vueRouter.createRouter({
    history: vueRouter.createMemoryHistory(),
    routes: [],
  });

  const setSpyRouterPush = () => cy.spy(router, "push").as("routerPush");

  let spyRouterPush: ReturnType<typeof setSpyRouterPush>;

  const mount = (props = getDefaultProps()) => {
    currentProps = props;

    spyGoToTab = setSpyGoToTab();
    spyRouterPush = setSpyRouterPush();

    ({ locationIndex, index } = props);
    return cy
      .mount(ClientRelationshipForm, {
        props,
        global: {
          plugins: [router],
          provide: {
            goToTab: goToTabWrapper.goToTab,
          },
        },
      })
      .its("wrapper")
      .as("vueWrapper");
  };

  beforeEach(() => {
    cy.intercept("GET", "**/api/codes/relationship-types/*", {
      fixture: "relationshipTypes.json",
    }).as("getRelationshipTypes");

    cy.intercept("GET", "**/api/clients/relation/*", {
      fixture: "search/james.json",
    }).as("searchClient");

    cy.intercept("GET", "**/api/clients/details/*", {
      fixture: "client-details/james-bond.json",
    }).as("getClientDetails");
  });

  it("renders the ClientRelationshipForm component", () => {
    mount();

    cy.get(`cds-dropdown#rc-${locationIndex}-${index}-location`);
    cy.get(`cds-dropdown#rc-${locationIndex}-${index}-relationship`);
    cy.get(`cds-combo-box#rc-${locationIndex}-${index}-relatedClient`);
    cy.get(`cds-dropdown#rc-${locationIndex}-${index}-relatedClient-location`);
    cy.get(`cds-text-input#rc-${locationIndex}-${index}-percentageOwnership`);
    cy.get(`cds-dropdown#rc-${locationIndex}-${index}-hasSigningAuthority`);

    cy.get(`cds-button#rc-${locationIndex}-${index}-SaveBtn`);
    cy.get(`cds-button#rc-${locationIndex}-${index}-CancelBtn`);
  });

  it("disables the Save button by default", () => {
    mount();

    cy.get(`cds-button#rc-${locationIndex}-${index}-SaveBtn`)
      .shadow()
      .find("button")
      .should("be.disabled");
  });

  const checkLoadedLocations = (
    addresses: ClientLocation[],
    expectedTotal: number,
    expectedActive: number,
    chainableDropdownList: Cypress.Chainable<JQuery<HTMLElement>>,
  ) => {
    expect(addresses.length).to.eq(expectedTotal);
    expect(addresses.filter((item) => item.locnExpiredInd === "N")).to.have.length(expectedActive);

    const expectedDeactivated = expectedTotal - expectedActive;

    expect(addresses.filter((item) => item.locnExpiredInd === "Y")).to.have.length(
      expectedDeactivated,
    );

    // Display only the active locations
    chainableDropdownList.should("have.length", expectedActive);
  };

  it("loads only the active locations from the primary client", () => {
    mount();

    // options are loaded in the dropdown
    cy.get(`cds-dropdown#rc-${locationIndex}-${index}-location`).within(() => {
      cy.get("cds-dropdown-item").each(($el, index) => {
        const { clientLocnCode, clientLocnName } = clientDetails.addresses[index];
        expect($el).to.have.attr("data-id", clientLocnCode);
        expect($el.text()).to.contain(clientLocnCode);
        expect($el.text()).to.contain(clientLocnName || "");
      });

      // 4 locations, but only 3 are active
      checkLoadedLocations(clientDetails.addresses, 4, 3, cy.get("cds-dropdown-item"));
    });
  });

  it("loads the relationship types according to the client type", () => {
    mount();

    cy.wait("@getRelationshipTypes").then(({ request, response }) => {
      // the request contains the clientTypeCode
      expect(request.url).to.match(
        new RegExp(
          `/api/codes/relationship-types/${currentProps.clientData.client.clientTypeCode}`,
        ),
      );

      cy.wait(50);

      // options are loaded in the dropdown
      cy.get(`cds-dropdown#rc-${locationIndex}-${index}-relationship`).within(() => {
        cy.get("cds-dropdown-item").should("have.length.greaterThan", 0);
        cy.get("cds-dropdown-item").each(($el) => {
          const code = $el.attr("data-id");
          const entry = response.body.find((item: CodeNameType) => item.code === code);
          const expectedText = entry.name;
          expect($el).to.have.text(expectedText);
        });
      });
    });
  });

  const relationshipTypePA = {
    code: "PA",
    name: "General Partner",
  };

  const relationshipTypeFM = {
    code: "FM",
    name: "Family Member",
  };

  const relationshipTypeJV = {
    code: "JV",
    name: "Joint Venture Participant",
  };

  const selectRelationshipType = (relationshipType: CodeNameType) => {
    cy.selectFormEntry(
      `cds-dropdown#rc-${locationIndex}-${index}-relationship`,
      relationshipType.name,
    );
  };

  describe("when id is null and the user selects Joint Venture as the relationship type", () => {
    beforeEach(() => {
      const props = getDefaultProps();
      props.data.id = null;
      props.data.percentageOwnership = 50.0;
      props.data.hasSigningAuthority = true;
      mount(props);

      cy.wait("@getRelationshipTypes");

      selectRelationshipType(relationshipTypeJV);
    });

    it("hides the percentageOwnership and the hasSigningAuthority fields", () => {
      cy.get(`#rc-${locationIndex}-${index}-percentageOwnership`).should("not.exist");
      cy.get(`#rc-${locationIndex}-${index}-hasSigningAuthority`).should("not.exist");
    });

    it("resets both percentageOwnership and hasSigningAuthority to null", () => {
      // Fill the remaining required fields
      cy.selectFormEntry(`cds-dropdown#rc-${locationIndex}-${index}-location`, "00 - Headquarters");
      selectRelatedClient("00000007");
      cy.wait("@getClientDetails");
      cy.selectFormEntry(
        `cds-dropdown#rc-${locationIndex}-${index}-relatedClient-location`,
        "00 - MI6 Headquarters",
      );

      // Hit the Save button
      cy.get(`#rc-${locationIndex}-${index}-SaveBtn`).click();

      cy.get<VueWrapper>("@vueWrapper").should((vueWrapper) => {
        expect(vueWrapper.emitted("save")).to.be.an("array");
        const { updatedData } = vueWrapper.emitted<SaveEvent<IndexedRelatedClient>>("save")![0][0];

        expect(updatedData.percentageOwnership).to.eq(null);
        expect(updatedData.hasSigningAuthority).to.eq(null);
      });
    });
  });

  describe("when id is null but relationship type is not JV", () => {
    beforeEach(() => {
      const props = getDefaultProps();
      props.data.id = null;
      mount(props);

      cy.wait("@getRelationshipTypes");
    });

    it("doesn't hide the fields percentageOwnership and hasSigningAuthority", () => {
      cy.get(`#rc-${locationIndex}-${index}-percentageOwnership`).should("be.visible");
      cy.get(`#rc-${locationIndex}-${index}-hasSigningAuthority`).should("be.visible");
    });
  });

  describe("when relationship type is JV but id is not null", () => {
    beforeEach(() => {
      const props = getDefaultProps();
      props.data.relationship = relationshipTypeJV;
      mount(props);

      cy.wait("@getRelationshipTypes");
    });

    it("doesn't hide the fields percentageOwnership and hasSigningAuthority", () => {
      cy.get(`#rc-${locationIndex}-${index}-percentageOwnership`).should("be.visible");
      cy.get(`#rc-${locationIndex}-${index}-hasSigningAuthority`).should("be.visible");
    });
  });

  const searchInput = "james";

  const selectRelatedClient = (relatedClientNumber: string) => {
    cy.selectAutocompleteEntry(
      `#rc-${locationIndex}-${index}-relatedClient`,
      searchInput,
      relatedClientNumber,
      "@searchClient",
    );
  };

  it("searches for clients filtered by the current client and the selected relationship type", () => {
    mount();

    selectRelationshipType(relationshipTypePA);

    cy.fillFormEntry(`cds-combo-box#rc-${locationIndex}-${index}-relatedClient`, searchInput);

    cy.wait("@searchClient").then(({ request, response }) => {
      // the request contains the client code (clientNumber)
      expect(request.url).to.match(
        new RegExp(`/api/clients/relation/${currentProps.data.client.client.code}`),
      );

      // the request contains the relationshipType
      expect(request.query.type).to.eq(relationshipTypePA.code);

      // the search input goes in the value parameter
      expect(request.query.value).to.eq(searchInput);

      // options are loaded in the combo-box
      cy.get(`cds-combo-box#rc-${locationIndex}-${index}-relatedClient`).within(() => {
        cy.get("cds-combo-box-item").each(($el, index) => {
          const { clientNumber } = response.body[index];
          expect($el).to.have.attr("data-id", clientNumber);
        });
      });
    });
  });

  const itLoadsLocationsByRelatedClient = (
    relatedClientNumber: string,
    cb?: (
      body: ClientDetails,
      chainableDropdownList: Cypress.Chainable<JQuery<HTMLElement>>,
    ) => void,
  ) => {
    cy.wait("@getClientDetails").then(({ request, response }) => {
      // the request contains the selected clientNumber
      expect(request.url).to.match(new RegExp(`/api/clients/details/${relatedClientNumber}`));

      cy.wait(50);

      // options are loaded in the dropdown
      cy.get(`cds-dropdown#rc-${locationIndex}-${index}-relatedClient-location`).within(() => {
        cy.get("cds-dropdown-item").each(($el, index) => {
          const { clientLocnCode, clientLocnName } = response.body.addresses[index];
          expect($el).to.have.attr("data-id", clientLocnCode);
          expect($el.text()).to.contain(clientLocnName);
        });

        cb?.(response.body, cy.get("cds-dropdown-item"));
      });
    });
  };

  it("loads only the active locations from the selected related client", () => {
    mount();

    selectRelationshipType(relationshipTypePA);

    const relatedClientNumber = "00000007";
    selectRelatedClient(relatedClientNumber);

    itLoadsLocationsByRelatedClient(relatedClientNumber, (body, chainableDropdownList) => {
      // 3 locations, but only 2 are active
      checkLoadedLocations(body.addresses, 3, 2, chainableDropdownList);
    });
  });

  it('emits a "delete" event', () => {
    mount();

    cy.get(`#rc-${locationIndex}-${index}-DeleteBtn`).click();

    cy.get<VueWrapper>("@vueWrapper").should((vueWrapper) => {
      expect(vueWrapper.emitted("delete")).to.be.an("array");
      expect(vueWrapper.emitted("delete")).to.have.length(1);
    });
  });

  const fillInRequiredFields = () => {
    cy.selectFormEntry(`cds-dropdown#rc-${locationIndex}-${index}-location`, "00 - Headquarters");
    selectRelationshipType(relationshipTypePA);
    selectRelatedClient("00000007");
    cy.wait("@getClientDetails");
    cy.selectFormEntry(
      `cds-dropdown#rc-${locationIndex}-${index}-relatedClient-location`,
      "00 - MI6 Headquarters",
    );
  };

  describe("when all the required fields are filled in", () => {
    beforeEach(() => {
      mount();

      fillInRequiredFields();
    });

    it("enables the Save button", () => {
      cy.get(`cds-button#rc-${locationIndex}-${index}-SaveBtn`)
        .shadow()
        .find("button")
        .should("be.enabled");
    });

    it("disables the Save button again if any required field gets cleared", () => {
      cy.clearFormEntry(`cds-combo-box#rc-${locationIndex}-${index}-relatedClient`);

      cy.get(`cds-button#rc-${locationIndex}-${index}-SaveBtn`)
        .shadow()
        .find("button")
        .should("be.disabled");
    });

    it("validates against percentage above 100", () => {
      cy.fillFormEntry(`#rc-${locationIndex}-${index}-percentageOwnership`, "101");

      cy.get(`#rc-${locationIndex}-${index}-percentageOwnership`).should("have.attr", "invalid");
    });

    describe("and the Related client field gets cleared by the user", () => {
      beforeEach(() => {
        cy.clearFormEntry(`cds-combo-box#rc-${locationIndex}-${index}-relatedClient`);
      });
      it("clears the Related client's location", () => {
        cy.get(`cds-dropdown#rc-${locationIndex}-${index}-relatedClient-location`).should(
          "have.value",
          "",
        );

        // related client options got cleared
        cy.get(`cds-combo-box#rc-${locationIndex}-${index}-relatedClient`).within(() => {
          /*
          Not sure why it just doesn't exist instead of being a single empty option (see the other
          test where "related client options got cleared").
          */
          cy.get("cds-combo-box-item").should("not.exist");
        });

        // related client's location options got cleared
        cy.get(`cds-dropdown#rc-${locationIndex}-${index}-relatedClient-location`).within(() => {
          cy.get("cds-dropdown-item").should("have.length", 1);
          cy.get("cds-dropdown-item").first().should("have.text", "");
        });
      });
    });

    describe("and the Related client field gets changed by the user", () => {
      const relatedClientNumber = "00000008";
      beforeEach(() => {
        selectRelatedClient(relatedClientNumber);
      });
      it("clears the related client's location", () => {
        cy.get(`cds-dropdown#rc-${locationIndex}-${index}-relatedClient-location`).should(
          "have.value",
          "",
        );
      });

      it("loads the locations from the newly selected related client", () => {
        itLoadsLocationsByRelatedClient(relatedClientNumber);
      });
    });

    describe("and the Relationship type field gets changed by the user", () => {
      beforeEach(() => {
        selectRelationshipType(relationshipTypeFM);
      });

      it("clears the Related client and the Related client's location", () => {
        cy.get(`cds-combo-box#rc-${locationIndex}-${index}-relatedClient`).should("have.value", "");
        cy.get(`cds-dropdown#rc-${locationIndex}-${index}-relatedClient-location`).should(
          "have.value",
          "",
        );

        // related client options got cleared
        cy.get(`cds-combo-box#rc-${locationIndex}-${index}-relatedClient`).within(() => {
          cy.get("cds-combo-box-item").should("have.length", 1);
          cy.get("cds-combo-box-item").first().should("have.text", "");
        });

        // related client's location options got cleared
        cy.get(`cds-dropdown#rc-${locationIndex}-${index}-relatedClient-location`).within(() => {
          cy.get("cds-dropdown-item").should("have.length", 1);
          cy.get("cds-dropdown-item").first().should("have.text", "");
        });
      });
    });

    it("sends the percentageOwnership as a number", () => {
      cy.clearFormEntry(`#rc-${locationIndex}-${index}-percentageOwnership`);

      cy.fillFormEntry(`#rc-${locationIndex}-${index}-percentageOwnership`, "44");

      cy.get(`#rc-${locationIndex}-${index}-SaveBtn`).click();

      cy.get<VueWrapper>("@vueWrapper").should((vueWrapper) => {
        expect(vueWrapper.emitted("save")).to.be.an("array");
        const { patch, updatedData } =
          vueWrapper.emitted<SaveEvent<IndexedRelatedClient>>("save")![0][0];

        expect(patch[0].value).to.be.a("number");
        expect(patch[0].value).to.eq(44);

        expect(updatedData.percentageOwnership).to.be.a("number");
        expect(updatedData.percentageOwnership).to.eq(44);
      });
    });
  });

  describe("when initial hasSigningAuthority is not null", () => {
    beforeEach(() => {
      const props = getDefaultProps();
      props.data.hasSigningAuthority = true;
      mount(props);

      fillInRequiredFields();
    });

    it("sends the hasSigningAuthority as null", () => {
      cy.selectFormEntry(`#rc-${locationIndex}-${index}-hasSigningAuthority`, "Not applicable");

      cy.get(`#rc-${locationIndex}-${index}-SaveBtn`).click();

      cy.get<VueWrapper>("@vueWrapper").should((vueWrapper) => {
        expect(vueWrapper.emitted("save")).to.be.an("array");
        const { patch, updatedData } =
          vueWrapper.emitted<SaveEvent<IndexedRelatedClient>>("save")![0][0];

        expect(patch[0].value).to.eq(null);

        expect(updatedData.hasSigningAuthority).to.eq(null);
      });
    });
  });

  describe("when initial percentageOwnership is not null", () => {
    beforeEach(() => {
      const props = getDefaultProps();
      props.data.percentageOwnership = 55;
      mount(props);

      fillInRequiredFields();
    });

    it("sends the percentageOwnership as null", () => {
      cy.clearFormEntry(`#rc-${locationIndex}-${index}-percentageOwnership`);

      cy.get(`#rc-${locationIndex}-${index}-SaveBtn`).click();

      cy.get<VueWrapper>("@vueWrapper").should((vueWrapper) => {
        expect(vueWrapper.emitted("save")).to.be.an("array");
        const { patch, updatedData } =
          vueWrapper.emitted<SaveEvent<IndexedRelatedClient>>("save")![0][0];

        expect(patch[0].value).to.eq(null);

        expect(updatedData.percentageOwnership).to.eq(null);
      });
    });
  });

  describe("when the link to create a new location gets clicked", () => {
    beforeEach(() => {
      mount();
      cy.get("#createLocationLink").click();
    });

    it("displays a confirmation modal", () => {
      cy.get("#modal-new-location").should("be.visible");
    });

    describe("and the Cancel button is clicked", () => {
      beforeEach(() => {
        cy.get("#modal-new-location .cds--modal-close-btn").click();
      });

      it("closes the modal", () => {
        cy.get("#modal-new-location").should("not.be.visible");
      });
      it("doesn't call the injected function 'goToTab'", () => {
        // eslint-disable-next-line @typescript-eslint/no-unused-expressions
        expect(spyGoToTab).not.to.be.called;
      });
      it("doesn't emit any 'canceled' event", () => {
        cy.get<VueWrapper>("@vueWrapper").should((vueWrapper) => {
          // eslint-disable-next-line @typescript-eslint/no-unused-expressions
          expect(vueWrapper.emitted("canceled")).to.be.undefined;
        });
      });
    });

    describe("and the confirmation button is clicked", () => {
      beforeEach(() => {
        cy.get("#modal-new-location .cds--modal-submit-btn").click();
      });

      it("closes the modal", () => {
        cy.get("#modal-new-location").should("not.be.visible");
      });
      it("calls the injected function 'goToTab' with 'locations'", () => {
        expect(spyGoToTab).to.be.calledWith("locations");
      });
      it("emits a 'canceled' event", () => {
        cy.get<VueWrapper>("@vueWrapper").should((vueWrapper) => {
          expect(vueWrapper.emitted("canceled")).to.be.an("array").of.length(1);
        });
      });
    });
  });

  describe("when the link to create a new client gets clicked", () => {
    beforeEach(() => {
      mount();
      cy.get("#createClientLink").click();
    });

    it("displays a confirmation modal", () => {
      cy.get("#modal-new-client").should("be.visible");
    });

    describe("and the Cancel button is clicked", () => {
      beforeEach(() => {
        cy.get("#modal-new-client .cds--modal-close-btn").click();
      });

      it("closes the modal", () => {
        cy.get("#modal-new-client").should("not.be.visible");
      });
      it("doesn't redirect to the Create client page", () => {
        // eslint-disable-next-line @typescript-eslint/no-unused-expressions
        expect(spyRouterPush).not.to.be.called;
      });
      it("doesn't emit any 'canceled' event", () => {
        cy.get<VueWrapper>("@vueWrapper").should((vueWrapper) => {
          // eslint-disable-next-line @typescript-eslint/no-unused-expressions
          expect(vueWrapper.emitted("canceled")).to.be.undefined;
        });
      });
    });

    describe("and the confirmation button is clicked", () => {
      beforeEach(() => {
        cy.get("#modal-new-client .cds--modal-submit-btn").click();
      });

      it("closes the modal", () => {
        cy.get("#modal-new-client").should("not.be.visible");
      });
      it("redirects to the Create client page", () => {
        expect(spyRouterPush).to.be.calledWith("/new-client-staff");
      });
      it("emits a 'canceled' event", () => {
        cy.get<VueWrapper>("@vueWrapper").should((vueWrapper) => {
          expect(vueWrapper.emitted("canceled")).to.be.an("array").of.length(1);
        });
      });
    });
  });
});
