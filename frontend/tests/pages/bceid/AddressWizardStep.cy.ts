import AddressWizardStep from "@/pages/bceidform/AddressWizardStep.vue";
import type { Address, FormDataDto } from "@/dto/ApplyClientNumberDto";
import type { ModalNotification } from "@/dto/CommonTypesDto";
import { useEventBus } from "@vueuse/core";

describe("<AddressWizardStep />", () => {
  beforeEach(() => {
    cy.fixture("contact.json").as("contactFixture");
    cy.fixture("roles.json").as("rolesFixture");
    cy.fixture("addresses.json").as("addressesFixture");
  });

  it("renders the AddressWizardStep component", () => {
    cy.mount(AddressWizardStep, {
      props: {
        data: {
          location: {
            addresses: [
              {
                locationName: "Mailing address",
                streetAddress: "123 Forest Street",
                country: { value: "CA", text: "Canada" },
                province: { value: "BC", text: "British Columbia" },
                city: "Victoria",
                postalCode: "A0A0A0",
              } as Address,
            ],
            contacts: [],
          },
        } as FormDataDto,
        active: false,
      },
    });

    // Assert that the main component is rendered
    cy.get(".frame-01").should("exist");

    // Assert that the first field is displayed
    cy.get("#addr_0").should("exist");
  });

  describe("when an address which is not the last one gets deleted", () => {
    const otherAddressNames = ["Sales Office", "Beach Office"];
    const addAddress = (addressId: number, name: string) => {
      cy.contains("Add another address").should("be.visible").click();

      cy.get(`#name_${addressId}`)
        .should("be.visible")
        .shadow()
        .find("input")
        .should("have.value", "")
        .type(name);
    };
    const fillAddress = (
      addressId: number,
      { streetAddress, postalCode }: { streetAddress: string; postalCode: string },
    ) => {
      cy.get(`#addr_${addressId}`)
        .should("be.visible")
        .shadow()
        .find("input")
        .should("have.value", "")
        .type(streetAddress);

      // blurs in case any address options cover the city field.
      cy.get(`#addr_${addressId}`).blur();

      cy.get(`#city_${addressId}`)
        .should("be.visible")
        .shadow()
        .find("input")
        .should("have.value", "")
        .type("Victoria");

      cy.get(`#postalCode_${addressId}`)
        .should("be.visible")
        .shadow()
        .find("input")
        .should("have.value", "")
        .type(postalCode);
    };

    let bus: ReturnType<typeof useEventBus<ModalNotification>>;

    beforeEach(() => {
      bus = useEventBus<ModalNotification>("modal-notification");
      bus.on((payload) => {
        payload.handler(); // automatically proceed with the deletion
      });
      cy.mount(AddressWizardStep, {
        props: {
          data: {
            location: {
              addresses: [
                {
                  locationName: "Mailing address",
                  streetAddress: "123 Forest Street",
                  country: { value: "CA", text: "Canada" },
                  province: { value: "BC", text: "British Columbia" },
                  city: "Victoria",
                  postalCode: "A0A0A0",
                } as Address,
              ],
              contacts: [],
            },
          } as FormDataDto,
          active: false,
        },
      })
        .its("wrapper")
        .as("vueWrapper");

      otherAddressNames.forEach((name, index) => {
        addAddress(index + 1, name);
      });
    });

    afterEach(() => {
      bus.reset();
    });

    it("removes the intended address from the DOM", () => {
      cy.get("#deleteAddress_1").click();
      cy.wait(150);
      cy.get("#name_1").should("not.exist");
      cy.get("#name_2").should("exist");
    });

    it("can submit the form (regardless of the deleted address being invalid)", () => {
      cy.get("#deleteAddress_1").click();
      cy.wait(150);

      fillAddress(2, {
        streetAddress: "456 Lumber Street",
        postalCode: "B0B0B0",
      });

      cy.get("@vueWrapper").should((vueWrapper) => {
        const lastValid = vueWrapper.emitted("valid").slice(-1)[0];

        // The last valid event was emitted with true.
        expect(lastValid[0]).to.equal(true);
      })
    });
  });
});
