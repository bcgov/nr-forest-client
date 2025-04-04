import { describe, it, expect } from "vitest";

import { mount } from "@vue/test-utils";
import FormStaffConfirmationPage from "@/pages/FormStaffConfirmationPage.vue";

describe("FormStaffConfirmationPage.vue", () => {
  // Helper function to mount component with props
  const createComponent = (propsData) => {
    return mount(FormStaffConfirmationPage, {
      propsData,
    });
  };

  it("renders without crashing", () => {
    const wrapper = createComponent({ clientNumber: "123", clientEmail: "test@example.com" });
    expect(wrapper.exists()).toBe(true);
  });

  it("displays client number and email", () => {
    const clientNumber = "123";
    const clientEmail = "test@example.com";
    const wrapper = createComponent({ clientNumber, clientEmail });

    expect(wrapper.text()).toContain(`New client ${clientNumber} has been created!`);
    expect(wrapper.text()).toContain(
      `We’ll send the client number and details submitted to ${clientEmail}`,
    );
  });

  it("renders SVG component correctly", () => {
    const wrapper = createComponent({ clientNumber: "123", clientEmail: "test@example.com" });
    const svg = wrapper.findComponent({ name: "SVG" }); // Assuming SVG is the name of the component
    expect(svg.exists()).toBe(true);
  });

  it("checks create another client button existence and href attribute", () => {
    const wrapper = createComponent({ clientNumber: "123", clientEmail: "test@example.com" });
    const button = wrapper.find("#createAnotherClientBtnId");
    expect(button.exists()).toBe(true);
    expect(button.attributes("href")).toBe("/new-client-staff");
  });
});
