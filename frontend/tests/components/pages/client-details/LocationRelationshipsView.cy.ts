import type { ClientLocation } from "@/dto/CommonTypesDto";
import LocationRelationshipsView from "@/pages/client-details/LocationRelationshipsView.vue";
import { formatAddress } from "@/services/ForestClientService";

import type { ComponentProps } from "vue-component-type-helpers";

import "@carbon/web-components/es/components/modal/index";

describe("<location-relationships-view />", () => {
  type RawProps = ComponentProps<typeof LocationRelationshipsView>;

  type IfEquals<X, Y, A = X, B = never> =
    (<T>() => T extends X ? 1 : 2) extends <T>() => T extends Y ? 1 : 2 ? A : B;

  type ReadonlyKeys<T> = {
    [P in keyof T]-?: IfEquals<{ [Q in P]: T[P] }, { -readonly [Q in P]: T[P] }, never, P>;
  }[keyof T];

  type Mutable<T> = {
    -readonly [K in keyof T]: T[K];
  };

  type Props = Mutable<Pick<RawProps, ReadonlyKeys<RawProps>>>;

  const getDefaultProps = (): Props => ({
    data: [
      {
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
          code: "SH",
          name: "Shareholder",
        },
        percentageOwnership: 33.0,
        hasSigningAuthority: true,
        isMainParticipant: true,
      },
      {
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
            code: "00000177",
            name: "GRAND FARM",
          },
          location: {
            code: "00",
            name: "Headquarters",
          },
        },
        relationship: {
          code: "SH",
          name: "Shareholder",
        },
        percentageOwnership: 33.0,
        hasSigningAuthority: false,
        isMainParticipant: true,
      },
      {
        client: {
          client: {
            code: "00000137",
            name: "MATELDA LINDHE",
          },
          location: {
            code: "00",
            name: "MAPLEWOOD HEIGHTS ADDRESS",
          },
        },
        relatedClient: {
          client: {
            code: "00000001",
            name: "Thiz",
          },
          location: {
            code: "01",
            name: "Accountant address",
          },
        },
        relationship: {
          code: "PA",
          name: "General Partner",
        },
        percentageOwnership: null,
        hasSigningAuthority: null,
        isMainParticipant: false,
      },
    ],
    location: {
      clientLocnName: "Mailing address",
      clientLocnCode: "00",
      addressOne: "C/O Tony Pineda",
      addressTwo: "Sample additional info",
      addressThree: "886 Richmond Ave",
      countryCode: "CA",
      countryDesc: "Canada",
      provinceCode: "SK",
      provinceDesc: "Saskatchewan",
      city: "Hampton",
      postalCode: "T4G5J1",
      emailAddress: "contact@mail.com",
      businessPhone: "2502863767",
      cellPhone: "2505553700",
      homePhone: "2505553101",
      faxNumber: "2502863768",
      cliLocnComment: "Sample location 00 comment",
      locnExpiredInd: "N",
    } as ClientLocation,
    userRoles: ["CLIENT_EDITOR"],
    isReloading: false,
  });

  let currentProps: ReturnType<typeof getDefaultProps> = null;
  const mount = (props = getDefaultProps()) => {
    currentProps = props;
    return cy
      .mount(LocationRelationshipsView, {
        props,
      })
      .its("wrapper")
      .as("vueWrapper");
  };

  const testField = (selector: string, value: string, linkPrefix?: string) => {
    cy.get(selector).should("be.visible");
    cy.get(selector).contains(value);
    expect(value.length).to.be.greaterThan(0);

    if (linkPrefix) {
      cy.get(selector).within(() => {
        cy.get("a").should("have.attr", "href", `${linkPrefix}${value}`);
      });
    }
  };

  it("renders the LocationRelationships component", () => {
    mount();

    const mailingAddress = formatAddress(currentProps.location);
    testField("#location-relationships-00-mailingAddress", mailingAddress);

    cy.wrap(currentProps.data).should("have.length", 3);

    cy.get("#relatioships-table").should("be.visible");
  });

  it("links to Google Maps search using the address properly encoded", () => {
    mount();

    const formattedAddress = formatAddress(currentProps.location);
    const encodedAddress = encodeURIComponent(formattedAddress);

    cy.get("#location-relationships-00-mailingAddress")
      .find("a")
      .should(
        "have.attr",
        "href",
        `https://www.google.com/maps/search/?api=1&query=${encodedAddress}`,
      );
  });

  it("renders the relationships table contents properly", () => {
    mount();

    cy.get("#relatioships-table")
      .find("cds-table-row")
      .should("have.length", 3)
      .should("be.visible");

    // Row 0
    cy.get("#relatioships-table")
      .find("cds-table-row")
      .eq(0)
      .within(() => {
        // Relationship type
        cy.get("cds-table-cell").eq(1).contains("General Partner");
        cy.get("cds-table-cell").eq(1).should("not.contain", "Primary");

        // Related client location
        cy.get("cds-table-cell")
          .eq(2)
          .find("a")
          .should("have.attr", "href", "/clients/details/00000137")
          .should("have.attr", "target", "_blank")
          .contains("00000137");

        cy.get("cds-table-cell").eq(2).contains("Matelda Lindhe");

        cy.get("cds-table-cell")
          .eq(2)
          .find("a")
          .should("have.attr", "href", "/clients/details/00000137")
          .should("have.attr", "target", "_blank")
          .contains("00 - MAPLEWOOD HEIGHTS ADDRESS");

        // Percentage owned
        cy.get("cds-table-cell").eq(3).contains("-");

        // Signing authority
        cy.get("cds-table-cell").eq(4).contains("-");
      });

    // Row 1
    cy.get("#relatioships-table")
      .find("cds-table-row")
      .eq(1)
      .within(() => {
        // Relationship type
        cy.get("cds-table-cell").eq(1).contains("Shareholder");
        cy.get("cds-table-cell").eq(1).contains("Primary");

        // Related client location
        cy.get("cds-table-cell")
          .eq(2)
          .find("a")
          .should("have.attr", "href", "/clients/details/00000177")
          .should("have.attr", "target", "_blank")
          .contains("00000177");

        cy.get("cds-table-cell").eq(2).contains("Grand Farm");

        cy.get("cds-table-cell")
          .eq(2)
          .find("a")
          .should("have.attr", "href", "/clients/details/00000177")
          .should("have.attr", "target", "_blank")
          .contains("00 - Headquarters");

        // Percentage owned
        cy.get("cds-table-cell").eq(3).contains("33%");

        // Signing authority
        cy.get("cds-table-cell").eq(4).contains("No");
      });

    // Row 2
    cy.get("#relatioships-table")
      .find("cds-table-row")
      .eq(2)
      .within(() => {
        // Relationship type
        cy.get("cds-table-cell").eq(1).contains("Shareholder");
        cy.get("cds-table-cell").eq(1).contains("Primary");

        // Related client location
        cy.get("cds-table-cell")
          .eq(2)
          .find("a")
          .should("have.attr", "href", "/clients/details/00000172")
          .should("have.attr", "target", "_blank")
          .contains("00000172");

        cy.get("cds-table-cell").eq(2).contains("Nature Nurturers");

        cy.get("cds-table-cell")
          .eq(2)
          .find("a")
          .should("have.attr", "href", "/clients/details/00000172")
          .should("have.attr", "target", "_blank")
          .contains("00 - MAIN OFFICE");

        // Percentage owned
        cy.get("cds-table-cell").eq(3).contains("33%");

        // Signing authority
        cy.get("cds-table-cell").eq(4).contains("Yes");
      });
  });

  it("makes the content invisible when isReloading is true", () => {
    const props = getDefaultProps();
    props.isReloading = true;
    mount(props);

    cy.get("div.grouping-12").should("have.class", "invisible");
  });

  it("displays the Actions column with buttons", () => {
    mount();

    cy.get("#relatioships-table").contains("Actions");
    cy.get("#relatioships-table").find("cds-button").should("be.visible");
  });

  describe("when user is a CLIENT_VIEWER only", () => {
    beforeEach(() => {
      const props = getDefaultProps();
      props.userRoles = ["CLIENT_VIEWER"];
      mount(props);
    });

    it("doesn't display the Actions column", () => {
      cy.get("#relatioships-table").should("not.contain", "Actions");
      cy.get("#relatioships-table").find("cds-button").should("not.exist");
    });
  });

  describe("when client is the main participant", () => {
    beforeEach(() => {
      const props = getDefaultProps();
      props.data = [props.data[0]];
      props.data[0].isMainParticipant = true;
      mount(props);
    });

    it("displays the primary tag", () => {
      cy.get("#relatioships-table")
        .find("cds-table-row")
        .eq(0)
        .find("cds-table-cell")
        .eq(1)
        .contains("Primary");
    });

    it("enables the action buttons", () => {
      cy.get("#location-00-row-0-EditBtn")
        .should("be.visible")
        .shadow()
        .find("button")
        .should("be.enabled");
    });
  });

  describe("when client is not the main participant", () => {
    beforeEach(() => {
      const props = getDefaultProps();
      props.data = [props.data[0]];
      props.data[0].isMainParticipant = false;
      mount(props);
    });

    it("doesn't display the primary tag", () => {
      cy.get("#relatioships-table")
        .find("cds-table-row")
        .eq(0)
        .find("cds-table-cell")
        .eq(1)
        .should("not.contain", "Primary");
    });

    it("disables the action buttons", () => {
      cy.get("#location-00-row-0-EditBtn")
        .should("be.visible")
        .shadow()
        .find("button")
        .should("be.disabled");
    });
  });
});
