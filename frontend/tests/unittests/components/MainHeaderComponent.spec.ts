import { describe, it, expect, vi } from "vitest";
import { mount } from "@vue/test-utils";

import MainHeaderComponent from "@/components/MainHeaderComponent.vue";

describe("MainHeaderComponent.vue", () => {
  const mockRoute = {
    path: "/",
    meta:{
      profile: false
    }
  };
  describe("Authenticated Scenario", () => {
    it("renders the component correctly when authenticated", async () => {
      const session = {
        session: { user: { provider: "bcsc" } },
        isLoggedIn: () => true,
        logOut: vi.fn(),
      };

      const wrapper = mount(MainHeaderComponent, {
        global: {          
          mocks: {
            $session: session,
            $route: mockRoute,
          },
        },
      });

      expect(wrapper.html()).toBeTruthy();
      expect(wrapper.find("cds-button").exists()).toBe(true);
      expect(wrapper.html()).toContain("Ministry of Forests");
    });
  });

  describe("Unauthenticated Scenario", () => {
    it("renders the component correctly when unauthenticated", async () => {
      const session = {
        isLoggedIn: () => false,
        logOut: vi.fn(),
      };

      const wrapper = mount(MainHeaderComponent, {
        global: {
          mocks: {
            $session: session,
            $route: mockRoute,
          },
        },
      });

      expect(wrapper.html()).toBeTruthy();
      expect(wrapper.find("bx-btn").exists()).toBe(false);
      expect(wrapper.html()).toContain("Ministry of Forests");
    });
  });
});
