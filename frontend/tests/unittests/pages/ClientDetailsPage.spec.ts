import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { ref, nextTick } from "vue";
import { mount } from "@vue/test-utils";
import ClientDetailsPage from "@/pages/ClientDetailsPage.vue";
import { createMemoryHistory, createRouter } from "vue-router";
import { useFetchTo } from "@/composables/useFetch";

vi.mock("@/composables/useFetch", () => ({
  useFetchTo: vi.fn(),
  useJsonPatch: vi.fn(),
}));

vi.mock("@/pages/client-details/BcRegistryView.vue", () => ({
  default: {
    name: "BcRegistryView",
    props: ["registrationNumber"],
    template: '<div class="bc-registry-stub" />',
  },
}));

const defaultFetchReturn = () => ({
  loading: ref(false),
  error: ref({}),
  response: ref({}),
  fetch: vi.fn(),
});

describe("ClientDetailsPage.vue", () => {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: "/", component: ClientDetailsPage }],
  });

  // Helper function to mount component with props
  const createComponent = (props = {}) =>
    mount(ClientDetailsPage, {
      props,
      attachTo: document.body,
      global: {
        plugins: [router],
        config: {
          globalProperties: {
            $features: {},
          },
        },
      },
    });

  beforeEach(() => {
    vi.clearAllMocks();
    (useFetchTo as any).mockImplementation(defaultFetchReturn);
  });

  afterEach(() => {
    document.body.innerHTML = "";
  });

  it("renders without crashing", () => {
    const wrapper = createComponent();
    expect(wrapper.exists()).toBe(true);
  });

  describe("bc-registry panel", () => {
    const clientWithRegistration = {
      client: {
        clientNumber: "00000001",
        clientName: "Test Corp",
        legalFirstName: null,
        legalMiddleName: null,
        clientStatusCode: "ACT",
        clientTypeCode: "C",
        registryCompanyTypeCode: "BC",
        corpRegnNmbr: "1234567",
      },
      addresses: [],
      contacts: [],
      relatedClients: {},
    };

    const clientWithoutRegistration = {
      ...clientWithRegistration,
      client: {
        ...clientWithRegistration.client,
        registryCompanyTypeCode: "",
        corpRegnNmbr: "",
      },
    };

    const makeBcRegistryPanelVisible = async () => {
      const panel = document.getElementById("panel-bc-registry");
      if (panel) panel.removeAttribute("hidden");

      const tabs = document.querySelector("cds-tabs");
      if (tabs) {
        tabs.dispatchEvent(
          new CustomEvent("cds-tabs-selected", {
            detail: { item: { value: "bc-registry" } },
          }),
        );
      }

      // Wait for the setTimeout(0) inside the tab-selection handler, then for Vue reactivity
      await new Promise((resolve) => setTimeout(resolve, 0));
      await nextTick();
    };

    it("does not render bc-registry-view initially", async () => {
      (useFetchTo as any).mockImplementation((url: any, data: any) => {
        const resolvedUrl = typeof url === "string" ? url : url?.value ?? "";
        if (resolvedUrl.includes("/clients/details/")) data.value = clientWithRegistration;
        return defaultFetchReturn();
      });

      const wrapper = createComponent();
      await nextTick();

      expect(wrapper.findComponent({ name: "BcRegistryView" }).exists()).toBe(false);
    });

    it("does not render bc-registry-view when panel is visible but registrationNumber is empty", async () => {
      (useFetchTo as any).mockImplementation((url: any, data: any) => {
        const resolvedUrl = typeof url === "string" ? url : url?.value ?? "";
        if (resolvedUrl.includes("/clients/details/")) data.value = clientWithoutRegistration;
        return defaultFetchReturn();
      });

      const wrapper = createComponent();
      await nextTick();
      await makeBcRegistryPanelVisible();

      expect(wrapper.findComponent({ name: "BcRegistryView" }).exists()).toBe(false);
    });

    it("renders bc-registry-view when panel is visible and registrationNumber is set", async () => {
      (useFetchTo as any).mockImplementation((url: any, data: any) => {
        const resolvedUrl = typeof url === "string" ? url : url?.value ?? "";
        if (resolvedUrl.includes("/clients/details/")) data.value = clientWithRegistration;
        return defaultFetchReturn();
      });

      const wrapper = createComponent();
      await nextTick();
      await makeBcRegistryPanelVisible();

      const bcView = wrapper.findComponent({ name: "BcRegistryView" });
      expect(bcView.exists()).toBe(true);
      expect(bcView.props("registrationNumber")).toBe("BC1234567");
    });
  });
});
