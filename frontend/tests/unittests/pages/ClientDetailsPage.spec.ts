import { describe, it, expect } from "vitest";

import { mount } from "@vue/test-utils";
import ClientDetailsPage from "@/pages/ClientDetailsPage.vue";

describe("ClientDetailsPage.vue", () => {
  // Helper function to mount component with props
  const createComponent = (propsData = {}) => {
    return mount(ClientDetailsPage, {
      propsData,
    });
  };

  it("renders without crashing", () => {
    const wrapper = createComponent();
    expect(wrapper.exists()).toBe(true);
  });
});
