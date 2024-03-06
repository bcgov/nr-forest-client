import { mount } from "@vue/test-utils";
import { describe, it, expect } from "vitest";
import ReviewWizardStep from "@/pages/bceidform/ReviewWizardStep.vue";

describe("ReviewWizardStep.vue", () => {
  const globalDefault = {
    config: {
      globalProperties: {
        $features: {
          BCEID_MULTI_ADDRESS: false,
        },
      },
    },
  };

  const districtsList = [
    {
      code: "DMH",
      name: "DMH - 100 Mile House Natural Resource District",
    },
    {
      code: "DCR",
      name: "DCR - Campbell River Natural Resource District",
    },
    {
      code: "DCC",
      name: "DCC - Cariboo-Chilcotin Natural Resource District",
    },
  ];

  it("renders business information", async () => {
    const wrapper = mount(ReviewWizardStep, {
      props: {
        data: {
          businessInformation: {
            businessName: "Your Business Name",
            businessType: "R",
            district: "DCR",
          },
          location: {
            addresses: [
              {
                locationName: "Location 1",
                streetAddress: "123 Main St",
                city: "City",
                province: { text: "Province" },
                country: { text: "Country" },
                postalCode: "12345",
              },
            ],
            contacts: [
              {
                firstName: "John",
                lastName: "Doe",
                locationNames: [{ text: "Location 1" }],
                contactType: { text: "Contact Type" },
                email: "john@example.com",
                phoneNumber: "123-456-7890",
              },
            ],
          },
        },
        districtsList,
        active: true,
        goToStep: () => {},
      },
      global: globalDefault,
    });

    wrapper.unmount();
  });

  it("should render the district name", () => {
    const wrapper = mount(ReviewWizardStep, {
      props: {
        data: {
          businessInformation: {
            businessName: "Your Business Name",
            businessType: "R",
            district: "DCR",
          },
          location: {
            addresses: [
              {
                locationName: "Location 1",
                streetAddress: "123 Main St",
                city: "City",
                province: { text: "Province" },
                country: { text: "Country" },
                postalCode: "12345",
              },
            ],
            contacts: [
              {
                firstName: "John",
                lastName: "Doe",
                locationNames: [{ text: "Location 1" }],
                contactType: { text: "Contact Type" },
                email: "john@example.com",
                phoneNumber: "123-456-7890",
              },
            ],
          },
        },
        districtsList,
        active: true,
        goToStep: () => {},
      },
      global: globalDefault,
    });

    expect(wrapper.get("#district").text()).toEqual(
      "DCR - Campbell River Natural Resource District",
    );
  });

  const locationName = "Location 1";

  describe("when feature BCEID_MULTI_ADDRESS is enabled", () => {
    const global = {
      config: {
        globalProperties: {
          $features: {
            BCEID_MULTI_ADDRESS: true,
          },
        },
      },
    };
    it("should render the locationNames information", () => {
      const wrapper = mount(ReviewWizardStep, {
        props: {
          data: {
            businessInformation: {
              businessName: "Your Business Name",
              businessType: "R",
              district: "DCR",
            },
            location: {
              addresses: [
                {
                  locationName,
                  streetAddress: "123 Main St",
                  city: "City",
                  province: { text: "Province" },
                  country: { text: "Country" },
                  postalCode: "12345",
                },
              ],
              contacts: [
                {
                  firstName: "John",
                  lastName: "Doe",
                  locationNames: [{ text: locationName }],
                  contactType: { text: "Contact Type" },
                  email: "john@example.com",
                  phoneNumber: "123-456-7890",
                },
              ],
            },
          },
          districtsList,
          active: true,
          goToStep: () => {},
        },
        global,
      });
      const contactsSection = wrapper
        .findAll(".grouping-05")
        .find((value) => value.find("h3").text() === "Contacts");

      expect(contactsSection.text()).toMatch(locationName);
    });
  });

  describe("when feature BCEID_MULTI_ADDRESS is disabled", () => {
    const global = {
      config: {
        globalProperties: {
          $features: {
            BCEID_MULTI_ADDRESS: false,
          },
        },
      },
    };
    it("should not render the locationNames information", () => {
      const wrapper = mount(ReviewWizardStep, {
        props: {
          data: {
            businessInformation: {
              businessName: "Your Business Name",
              businessType: "R",
              district: "DCR",
            },
            location: {
              addresses: [
                {
                  locationName,
                  streetAddress: "123 Main St",
                  city: "City",
                  province: { text: "Province" },
                  country: { text: "Country" },
                  postalCode: "12345",
                },
              ],
              contacts: [
                {
                  firstName: "John",
                  lastName: "Doe",
                  locationNames: [{ text: locationName }],
                  contactType: { text: "Contact Type" },
                  email: "john@example.com",
                  phoneNumber: "123-456-7890",
                },
              ],
            },
          },
          districtsList,
          active: true,
          goToStep: () => {},
        },
        global,
      });
      const contactsSection = wrapper
        .findAll(".grouping-05")
        .find((value) => value.find("h3").text() === "Contacts");

      expect(contactsSection.text()).not.toMatch(locationName);
    });
  });

  it("emits 'update:data' event when clicking edit business information button", async () => {
    const wrapper = mount(ReviewWizardStep, {
      props: {
        data: {
          businessInformation: {
            businessName: "Your Business Name",
            businessType: "R",
            clientType: "C",
            district: "DCR",
          },
          location: {
            addresses: [
              {
                locationName,
                streetAddress: "123 Main St",
                city: "City",
                province: { text: "Province" },
                country: { text: "Country" },
                postalCode: "12345",
              },
            ],
            contacts: [
              {
                firstName: "John",
                lastName: "Doe",
                locationNames: [{ text: locationName }],
                contactType: { text: "Contact Type" },
                email: "john@example.com",
                phoneNumber: "123-456-7890",
              },
            ],
          },
        },
        districtsList,
        active: true,
        goToStep: () => {},
      },
      global: globalDefault,
    });

    await wrapper.vm.$nextTick();

    wrapper.unmount();
  });

});
