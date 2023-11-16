import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { mount } from "@vue/test-utils";

import MainHeaderComponent from "@/components/MainHeaderComponent.vue";
import { nextTick } from "vue";

describe("MainHeaderComponent.vue", () => {
  const mockRoute = {
    path: "/",
    meta:{
      profile: false
    }
  };
  describe("Authenticated Scenario", () => {
    const session = {
      user: {
        provider: "bcsc",
        name: "John Doe",
      },
      isLoggedIn: () => true,
      logOut: vi.fn(),
    };

    it("renders the component correctly when authenticated", async () => {

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

    describe("when route meta profile is truthy", () => {
      const mockRoute = {
        path: "/",
        meta: {
          profile: true,
        },
      };

      describe("My profile panel", () => {
        let wrapper: ReturnType<typeof mount<typeof MainHeaderComponent>>;
        beforeEach(async () => {
          wrapper = mount(MainHeaderComponent, {
            global: {
              mocks: {
                $session: session,
                $route: mockRoute,
              },
            },
            attachTo: document.body, // fixes the click event on the web component
          });

          // The test fails without this. It's probably something related to the use of MutationObserver.
          await nextTick();

          expect(wrapper.html()).toBeTruthy();
          expect(wrapper.html()).toContain("Client Management System");

          const panelAction = wrapper.find("[data-testid='panel-action']");

          // patch stuff missing in the web component in this test environment
          Object.defineProperty(panelAction.element, "_buttonNode", {
            value: {
              classList: {
                add: () => {},
                remove: () => {},
              },
            },
          });

          expect(panelAction.exists()).toBe(true);
          await panelAction.trigger("click");
          const panel = wrapper.find("#my-profile-panel");
          expect(panel.attributes().expanded).toEqual("true");
        });
        afterEach(() => {
          wrapper.unmount();
        })
        it("closes the panel when the inner close button is clicked", async () => {
          const button = wrapper.find(".close-panel-button");
          expect(button.exists()).toBe(true);
          await button.trigger("click");
          const panel = wrapper.find("#my-profile-panel");
          expect(panel.attributes().expanded).toBeUndefined();
        });

        it("closes the panel when the backdrop is clicked", async () => {
          const backdrop = wrapper.find("[data-testid='my-profile-backdrop']");
          expect(backdrop.exists()).toBe(true);
          await backdrop.trigger("click");
          const panel = wrapper.find("#my-profile-panel");
          expect(panel.attributes().expanded).toBeUndefined();
        });

        it("displays a backdrop only when the panel is open", async () => {
          const backdrop = wrapper.find("[data-testid='my-profile-backdrop']");

          // backdrop is active
          expect(backdrop.classes()).toContain("overlay-active");

          const button = wrapper.find(".close-panel-button");
          expect(button.exists()).toBe(true);
          await button.trigger("click");
          const panel = wrapper.find("#my-profile-panel");
          expect(panel.attributes().expanded).toBeUndefined();

          // backdrop is not active
          expect(backdrop.classes()).not.toContain("overlay-active");
        });
      });
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
