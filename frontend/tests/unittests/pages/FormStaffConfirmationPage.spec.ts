import { describe, it, expect, vi } from "vitest";

import { mount } from "@vue/test-utils";
import FormStaffConfirmationPage from "@/pages/FormStaffConfirmationPage.vue";
import * as constants from "@/CoreConstants";

const { featureFlags, greenDomain } = constants;

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
    const notifyClientInd = "Y";
    const wrapper = createComponent({ clientNumber, clientEmail, notifyClientInd });

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

  it("opens client details in the same application when the feature flag is enabled", () => {
    vi.spyOn(constants, "featureFlags", "get").mockReturnValue({
      ...featureFlags,
      STAFF_CLIENT_DETAIL: true,
    });
    const clientNumber = "123";
    const wrapper = createComponent({ clientNumber, clientEmail: "test@example.com" });
    const button = wrapper.find("#openClientDtlsBtnId");
    const spy = vi.spyOn(window, "open");
    button.trigger("click");
    expect(spy).toBeCalledWith(`/clients/details/${clientNumber}`, "_self");
  });

  it("opens client details in the legacy application when the feature flag is disabled", () => {
    vi.spyOn(constants, "featureFlags", "get").mockReturnValue({
      ...featureFlags,
      STAFF_CLIENT_DETAIL: false,
    });
    const clientNumber = "123";
    const wrapper = createComponent({ clientNumber, clientEmail: "test@example.com" });
    const button = wrapper.find("#openClientDtlsBtnId");
    const spy = vi.spyOn(window, "open");
    button.trigger("click");
    expect(spy).toBeCalledWith(
      `https://${greenDomain}/int/client/client02MaintenanceAction.do?bean.clientNumber=${clientNumber}`,
      "_blank",
      "noopener",
    );
  });
});
