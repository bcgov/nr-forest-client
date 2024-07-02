import LocationsWizardStep from "@/pages/staffform/LocationsWizardStep.vue";
import { emptyAddress } from "@/dto/ApplyClientNumberDto";
import type { Address, FormDataDto } from "@/dto/ApplyClientNumberDto";
import type { ModalNotification } from "@/dto/CommonTypesDto";
import { useEventBus } from "@vueuse/core";

describe("<LocationsWizardStep />", () => {
  beforeEach(() => {
    cy.intercept("GET", `/api/addresses?country=CA&maxSuggestions=10&searchTerm=*`, {
      fixture: "addressSearch.json",
    });
  });

  it("renders the LocationsWizardStep component", () => {
    cy.mount(LocationsWizardStep, {
      props: {
        data: {
          location: {
            addresses: [
              {
                locationName: "Mailing address",
                complementaryAddressOne: "",
                complementaryAddressTwo: null,
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

  describe("additional delivery information", () => {
    let bus: ReturnType<typeof useEventBus<ModalNotification>>;

    beforeEach(() => {
      bus = useEventBus<ModalNotification>("modal-notification");
      bus.on((payload) => {
        payload.handler(); // automatically proceed with the deletion
      });

      cy.mount(LocationsWizardStep, {
        props: {
          data: {
            location: {
              addresses: [
                {
                  locationName: "",
                  complementaryAddressOne: "",
                  complementaryAddressTwo: null,
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
    });

    afterEach(() => {
      bus.reset();
    });

    it("should add the Additional delivery information and then remove it", () => {
      // should not display the Additional delivery information input initially
      cy.get("#complementaryAddressTwo_0").should("not.exist");

      // click to add it
      cy.contains("Add more delivery information").should("be.visible").click();

      // should display the Additional delivery information input
      cy.get("#complementaryAddressTwo_0").should("be.visible");

      // should hide the button
      cy.contains("Add more delivery information").should("not.exist");

      // click to remove it
      cy.get("#deleteAdditionalDeliveryInformation_0").click();

      // should hide the Additional delivery information input again
      cy.get("#complementaryAddressTwo_0").should("not.exist");

      // the button to add it is visible again
      cy.contains("Add more delivery information").should("be.visible");
    });

    it("should display an error on the Delivery information when it's empty and Additional delivery information is displayed", () => {
      cy.contains("Add more delivery information").click();

      cy.get("#complementaryAddressTwo_0").should("be.visible");

      // the error arrises
      cy.get("#complementaryAddressOne_0")
        .find("input")
        .should("have.class", "cds--text-input--invalid");

      // click to remove it
      cy.get("#deleteAdditionalDeliveryInformation_0").click();

      // the error goes away
      cy.get("#complementaryAddressOne_0")
        .find("input")
        .should("not.have.class", "cds--text-input--invalid");

      cy.contains("Add more delivery information").click();

      cy.get("#complementaryAddressTwo_0").should("be.visible");

      // the error is back
      cy.get("#complementaryAddressOne_0")
        .find("input")
        .should("have.class", "cds--text-input--invalid");

      cy.get("#complementaryAddressOne_0").find("input").type("anything");

      // the error goes away
      cy.get("#complementaryAddressOne_0")
        .find("input")
        .should("not.have.class", "cds--text-input--invalid");
    });
  });

  describe("when an address which is not the last one gets deleted", () => {
    const otherAddressNames = ["Sales Office", "Beach Office"];
    const addAddress = (addressId: number, name: string) => {
      cy.contains("Add another location").should("be.visible").click();

      // TODO: check focus on section title instead
      // cy.focused().should("contain.text", "Additional location");

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

    const mountTest = (includeOtherAddressesInProps: boolean) => {
      cy.mount(LocationsWizardStep, {
        props: {
          data: {
            location: {
              addresses: [
                {
                  locationName: "Mailing address",
                  complementaryAddressOne: "",
                  complementaryAddressTwo: null,
                  streetAddress: "123 Forest Street",
                  country: { value: "CA", text: "Canada" },
                  province: { value: "BC", text: "British Columbia" },
                  city: "Victoria",
                  postalCode: "A0A0A0",
                } as Address,
                ...(includeOtherAddressesInProps ? otherAddressNames : []).map(
                  (locationName) => ({
                    ...emptyAddress(),
                    locationName,
                  }),
                ),
              ],
              contacts: [],
            },
          } as FormDataDto,
          active: false,
        },
      })
        .its("wrapper")
        .as("vueWrapper");
    };

    // Multi-scenario test
    [
      { includeOtherAddressesInProps: true, predicate: "are provided in the props" },
      { includeOtherAddressesInProps: false, predicate: "are added manually" },
    ].forEach(({ includeOtherAddressesInProps, predicate }) =>
      describe(`when other addresses ${predicate}`, () => {
        beforeEach(() => {
          bus = useEventBus<ModalNotification>("modal-notification");
          bus.on((payload) => {
            payload.handler(); // automatically proceed with the deletion
          });

          mountTest(includeOtherAddressesInProps);

          if (!includeOtherAddressesInProps) {
            otherAddressNames.forEach((name, index) => {
              addAddress(index + 1, name);
            });
          }
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
          });
        });
      }),
    );
  });

  it("should display error messages when two locations have the same name", () => {
    cy.mount(LocationsWizardStep, {
      props: {
        data: {
          location: {
            addresses: [
              {
                locationName: "My Office",
                complementaryAddressOne: "",
                complementaryAddressTwo: null,
                streetAddress: "",
                country: { value: "CA", text: "Canada" },
                province: { value: "BC", text: "British Columbia" },
                city: "",
                postalCode: "",
              } as Address,
            ],
            contacts: [],
          },
        } as FormDataDto,
        active: false,
      },
    });

    cy.contains("Add another location").should("be.visible").click();

    cy.get("#name_1").should("be.visible").find("input").type("my office");

    cy.get("#name_0").find("input").should("have.class", "cds--text-input--invalid");
    cy.get("#name_1").find("input").should("have.class", "cds--text-input--invalid");
  });

  it("should not display any error when two locations have the same address data", () => {
    const address = {
      streetAddress: "123 Forest Street",
      country: { value: "CA", text: "Canada" },
      province: { value: "BC", text: "British Columbia" },
      city: "Victoria",
      postalCode: "A0A0A0",
    } as Address;
    cy.mount(LocationsWizardStep, {
      props: {
        data: {
          location: {
            addresses: [
              {
                locationName: "My Office",
                complementaryAddressOne: "",
                complementaryAddressTwo: null,
                ...address,
              } as Address,
              {
                locationName: "My Store",
                complementaryAddressOne: "",
                complementaryAddressTwo: null,
                ...address,
              } as Address,
            ],
            contacts: [],
          },
        } as FormDataDto,
        active: false,
      },
    });

    cy.get("#addr_1").find("div").should("not.have.class", "cds--dropdown--invalid");
    cy.get("#city_1").find("input").should("not.have.class", "cds--text-input--invalid");
  });
});
