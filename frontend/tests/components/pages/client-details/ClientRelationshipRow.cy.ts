import type { ClientDetails } from "@/dto/CommonTypesDto";
import ClientRelationshipRow from "@/pages/client-details/ClientRelationshipRow.vue";
import {
  CLIENT_RELATIONSHIPS_EDIT_COLUMN_COUNT,
  type OperateRelatedClient,
} from "@/pages/client-details/shared";
import type { VueWrapper } from "@vue/test-utils";
import type { ComponentProps } from "vue-component-type-helpers";

// Carbon
import "@carbon/web-components/es/components/button/index";

describe("<client-relationships-row />", () => {
  type RawProps = ComponentProps<typeof ClientRelationshipRow>;

  type IfEquals<X, Y, A = X, B = never> =
    (<T>() => T extends X ? 1 : 2) extends <T>() => T extends Y ? 1 : 2 ? A : B;

  type ReadonlyKeys<T> = {
    [P in keyof T]-?: IfEquals<{ [Q in P]: T[P] }, { -readonly [Q in P]: T[P] }, never, P>;
  }[keyof T];

  type Mutable<T> = {
    -readonly [K in keyof T]: T[K];
  };

  type Props = Mutable<Pick<RawProps, ReadonlyKeys<RawProps>>>;

  let clientData: ClientDetails;
  before(() => {
    cy.fixture("client-details/michael-scott.json").then((data) => {
      clientData = data;
    });
  });

  const getDefaultProps = (): Props => ({
    row: {
      client: {
        client: {
          code: "00000001",
          name: "Thiz",
        },
        location: {
          code: "01",
          name: "Accountant address",
        },
      },
      relatedClient: {
        client: {
          code: "00000172",
          name: "NATURE NURTURERS",
        },
        location: {
          code: "00",
          name: "MAIN OFFICE",
        },
      },
      relationship: {
        code: "PA",
        name: "General Partner",
      },
      percentageOwnership: 33.0,
      hasSigningAuthority: true,
      isMainParticipant: true,
      originalLocation: {
        code: "00",
        name: "MAIN OFFICE",
      },
      index: 0,
    },
    clientData,
    locationIndex: "00",
    validations: [],
    userRoles: ["CLIENT_EDITOR"],
  });

  const operateRelatedClientStub: OperateRelatedClient = () => {
    alert("hey");
  };
  const operateRelatedClientWrapper = {
    operateRelatedClient: operateRelatedClientStub,
  };

  const setSpyOperateRelatedClient = () =>
    cy.spy(operateRelatedClientWrapper, "operateRelatedClient").as("operateRelatedClient");

  let spyOperateRelatedClient: ReturnType<typeof setSpyOperateRelatedClient>;

  let currentProps: ReturnType<typeof getDefaultProps> = null;
  const mount = (props = getDefaultProps()) => {
    currentProps = props;
    spyOperateRelatedClient = setSpyOperateRelatedClient();
    return cy
      .mount(ClientRelationshipRow, {
        props,
        global: {
          provide: {
            operateRelatedClient: operateRelatedClientWrapper.operateRelatedClient,
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

    cy.intercept("GET", "**/api/clients/details/*", {
      fixture: "client-details/james-bond.json",
    }).as("getClientDetails");
  });

  it('renders a table row without the css class "edit"', () => {
    mount();

    cy.get("cds-table-row").should("be.visible").should("not.have.class", "edit");
  });

  describe("when the Edit button is clicked", () => {
    const testData = {
      initialCellCount: null,
    };
    beforeEach(() => {
      mount();

      cy.get("cds-table-cell").then((cellList) => {
        testData.initialCellCount = cellList.length;
      });

      cy.get(
        `#location-${currentProps.locationIndex}-row-${currentProps.row.index}-EditBtn`,
      ).click();
    });

    it("renders a form in place of the table cells, within the table row", () => {
      cy.get("cds-table-cell").then((cellList) => {
        // Data cells were removed
        expect(cellList.length).to.be.lessThan(testData.initialCellCount);
      });

      cy.get<VueWrapper>("@vueWrapper").should((vueWrapper) => {
        const formComponent = vueWrapper.get("cds-table-row").getComponent({
          name: "client-relationship-form",
        });

        // eslint-disable-next-line @typescript-eslint/no-unused-expressions
        expect(formComponent.element).to.be.visible;
      });
    });

    it("value of CLIENT_RELATIONSHIPS_EDIT_COLUMN_COUNT is correct", () => {
      /*
      While this test might look out of place, it's an important test and this is the perfect
      context where the expected constant value could be calculated.
      And it should also serve to point the root cause in case of a failure after some columns are
      ever added or removed from the corresponding data table.
      */

      cy.get("cds-table-cell").then((cellList) => {
        const dataCellsCount = testData.initialCellCount - cellList.length;
        expect(CLIENT_RELATIONSHIPS_EDIT_COLUMN_COUNT).to.eq(dataCellsCount);
      });
    });

    it("the form fills the whole table width accordingly", () => {
      // Makes sure the colspan in the td tag matches the number of missing table cells
      cy.get("td").should("have.attr", "colspan", `${CLIENT_RELATIONSHIPS_EDIT_COLUMN_COUNT}`);
    });

    it('applies the css class "edit" to the table row', () => {
      cy.get("cds-table-row").should("be.visible").should("have.class", "edit");
    });

    describe("and the Cancel button is clicked", () => {
      beforeEach(() => {
        cy.get(`#rc-${currentProps.locationIndex}-${currentProps.row.index}-CancelBtn`).click();
      });

      it("renders the table cells back in place of the form", () => {
        cy.get("cds-table-cell").should("have.length", testData.initialCellCount);
      });

      it('removes the css class "edit" to the table row', () => {
        cy.get("cds-table-row").should("be.visible").should("not.have.class", "edit");
      });
    });

    describe("and the Save button is clicked", () => {
      beforeEach(() => {
        cy.clearFormEntry(
          `#rc-${currentProps.locationIndex}-${currentProps.row.index}-percentageOwnership`,
        );

        cy.fillFormEntry(
          `#rc-${currentProps.locationIndex}-${currentProps.row.index}-percentageOwnership`,
          "44",
        );

        cy.get(`#rc-${currentProps.locationIndex}-${currentProps.row.index}-SaveBtn`).click();
      });

      it('calls the injected "operateRelatedClient" and passes the "SaveableComponent" set of callbacks', () => {
        // eslint-disable-next-line @typescript-eslint/no-unused-expressions
        expect(spyOperateRelatedClient).to.be.called;

        const spyCall = spyOperateRelatedClient.getCall(0);
        const { saveableComponent } = spyCall.args[1];
        expect(saveableComponent).to.be.an("object");
        expect(saveableComponent.setSaving).to.be.a("function");
        expect(saveableComponent.lockEditing).to.be.a("function");
      });

      it("sends the percentageOwnership as a number", () => {
        // eslint-disable-next-line @typescript-eslint/no-unused-expressions
        expect(spyOperateRelatedClient).to.be.called;

        const spyCall = spyOperateRelatedClient.getCall(0);
        const { patch, updatedData } = spyCall.args[0];

        expect(patch[0].value).to.be.a("number");
        expect(patch[0].value).to.eq(44);

        expect(updatedData.percentageOwnership).to.be.a("number");
        expect(updatedData.percentageOwnership).to.eq(44);
      });
    });
  });
});
