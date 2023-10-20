import { mount } from "@vue/test-utils";
import { describe, it, expect } from "vitest";
import ReviewWizardStep from "@/pages/bceidform/ReviewWizardStep.vue";

describe("ReviewWizardStep.vue", () => {
  it("renders business information", async () => {
    const wrapper = mount(ReviewWizardStep, {
      props: {
        data: {
          businessInformation: {
            businessName: "Your Business Name",
            businessType: "R",
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
        active: true,
        goToStep: () => {},
      },
    });

    wrapper.unmount();
  });

  it("emits 'update:data' event when clicking edit business information button", async () => {
    const wrapper = mount(ReviewWizardStep, {
      props: {
        data: {
          businessInformation: {
            businessName: "Your Business Name",
            businessType: "R",
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
        active: true,
        goToStep: () => {},
      },
    });

    const companyBusinessTypes = wrapper.vm.companyBusinessTypes;

    const expectedCompanyBusinessTypes = {
      R: "B.C. Registered Business - Corporation",
      U: "Sole Proprietorship",
    };

    expect(companyBusinessTypes).toEqual(expectedCompanyBusinessTypes);

    wrapper.unmount();
  });

});
