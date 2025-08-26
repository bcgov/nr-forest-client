import type { ClientDetails, CodeNameType, RelatedClientEntry } from "@/dto/CommonTypesDto";
import ClientRelationshipForm from "@/pages/client-details/ClientRelationshipForm.vue";

// Carbon
import "@carbon/web-components/es/components/button/index";

describe("<client-relationship-form />", () => {
  let clientDetails: ClientDetails;
  before(() => {
    cy.fixture("client-details/se.json").then((data) => {
      clientDetails = data;
    });
  });
  const getDefaultProps = () => ({
    locationIndex: "null",
    index: "null",
    data: {
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
  const mount = (props = getDefaultProps()) => {
    currentProps = props;
    ({ locationIndex, index } = props);
    return cy.mount(ClientRelationshipForm, { props }).its("wrapper").as("vueWrapper");
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
    cy.get(`cds-toggle#rc-${locationIndex}-${index}-hasSigningAuthority`);

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
        cy.get("cds-dropdown-item").each(($el, index) => {
          const { code } = response.body[index];
          expect($el).to.have.attr("data-id", code);
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

  const selectRelationshipType = (relationshipType: CodeNameType) => {
    cy.selectFormEntry(
      `cds-dropdown#rc-${locationIndex}-${index}-relationship`,
      relationshipType.name,
    );
  };

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

  const itLoadsLocationsByRelatedClient = (relatedClientNumber: string) => {
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
      });
    });
  };

  it("loads the locations from the selected related client", () => {
    mount();

    selectRelationshipType(relationshipTypePA);

    const relatedClientNumber = "00000007";
    selectRelatedClient(relatedClientNumber);

    itLoadsLocationsByRelatedClient(relatedClientNumber);
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
  });
});
