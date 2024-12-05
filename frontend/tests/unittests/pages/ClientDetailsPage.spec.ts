import { describe, it, expect } from "vitest";

import { mount } from "@vue/test-utils";
import ClientDetailsPage from "@/pages/ClientDetailsPage.vue";
import { createMemoryHistory, createRouter } from "vue-router";

describe("ClientDetailsPage.vue", () => {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: "/", component: ClientDetailsPage }],
  });

  // Helper function to mount component with props
  const createComponent = (props = {}) => {
    return mount(ClientDetailsPage, {
      props,
      global: {
        plugins: [router],
      },
    });
  };

  it("renders without crashing", () => {
    const wrapper = createComponent();
    expect(wrapper.exists()).toBe(true);
  });
});
