// Import the necessary dependencies
import { mount } from "@vue/test-utils";
import { describe, it, expect } from "vitest";
import ReviewWizardStep from "@/pages/bceidform/ReviewWizardStep.vue";

describe("ReviewWizardStep.vue", () => {
  it("renders correctly", async () => {
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

    await wrapper.vm.$nextTick();

    // Debugging: Log the HTML content of the component
    console.log(wrapper.html());

    // Attempt to find the button element
    const buttonElement = wrapper.find(".grouping-06 .cds-button");

    // Debugging: Log the found button element
    console.log(buttonElement);

    // Check if the button element exists
    if (buttonElement.exists()) {
      await buttonElement.trigger("click");
    } else {
      console.error("Button element not found");
    }

    // Unmount the component after the test
    wrapper.unmount();
  });
});
