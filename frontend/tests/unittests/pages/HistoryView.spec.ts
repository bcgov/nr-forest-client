import { describe, it, expect, vi, beforeEach } from "vitest";
import { ref } from "vue";
import { mount, flushPromises } from "@vue/test-utils";
import HistoryView from "@/pages/client-details/HistoryView.vue";
import { useFetchTo } from "@/composables/useFetch";

vi.mock("vue-router", () => ({
  useRouter: () => ({
    currentRoute: {
      value: {
        params: { id: "00000001" },
      },
    },
  }),
}));

vi.mock("@/composables/useFetch", () => ({
  useFetchTo: vi.fn(),
}));

const mockFetch = vi.fn();
const mockResponse = ref({ headers: { "x-total-count": "100" } });
const mockLoading = ref(false);

const defaultFetchReturn = (_url: any, received: any) => {
  received.value = [
    {
      tableName: "ClientInformation",
      idx: "1",
      identifierLabel: "Client summary updated",
      updateTimestamp: "2024-01-01T10:00:00",
      updateUserid: "IDIR/TEST",
      changeType: "UPD",
      details: [{ columnName: "clientName", oldValue: "Old", newValue: "New" }],
      reasons: [{ actionCode: "NAME", reason: "Correction" }],
    },
  ];
  return {
    loading: mockLoading,
    error: ref({}),
    response: mockResponse,
    data: received,
    fetch: mockFetch,
  };
};

describe("HistoryView.vue", () => {
  const createComponent = async () => {
    const wrapper = mount(HistoryView, {
      attachTo: document.body,
      global: {
        stubs: {
          MultiselectInputComponent: {
            template: '<div class="multiselect-stub" />',
            props: ["modelValue", "selectedValues"],
          },
          Location16: true,
          User16: true,
          NetworkEnterprise16: true,
          Enterprise16: true,
          Document16: true,
          TaskAdd16: true,
          ChevronDown16: true,
          UserAvatar20: true,
          UserSearch: true,
        },
      },
    });
    await flushPromises();
    return wrapper;
  };

  beforeEach(() => {
    vi.clearAllMocks();
    (useFetchTo as any).mockImplementation(defaultFetchReturn);
  });

  it("renders history view and pagination", async () => {
    const wrapper = await createComponent();
    expect(wrapper.find(".history-card").exists()).toBe(true);
    expect(wrapper.find("cds-pagination").exists()).toBe(true);
  });

  it("updates page on cds-pagination-changed-current", async () => {
    const wrapper = await createComponent();
    const pagination = wrapper.find("cds-pagination");

    pagination.element.dispatchEvent(
      new CustomEvent("cds-pagination-changed-current", {
        detail: { page: 2, pageSize: 20 },
        bubbles: true,
        composed: true,
      })
    );
    await flushPromises();

    expect(mockFetch).toHaveBeenCalled();
    const uriRef = (useFetchTo as any).mock.calls[0][0];
    expect(uriRef.value).toContain("page=1&size=20");
  });

  it("updates page size and resets page on cds-select-selected from page-size-select", async () => {
    const wrapper = await createComponent();
    const pagination = wrapper.find("cds-pagination");

    // 1. Navigate to page 2 first
    pagination.element.dispatchEvent(
      new CustomEvent("cds-pagination-changed-current", {
        detail: { page: 2, pageSize: 20 },
        bubbles: true,
        composed: true,
      })
    );
    await flushPromises();

    const uriRef = (useFetchTo as any).mock.calls[0][0];
    expect(uriRef.value).toContain("page=1&size=20");

    mockFetch.mockClear();

    // 2. Change page size to 50 on the actual shadowRoot select
    const pageSizeSelect =
      (pagination.element as any).shadowRoot?.querySelector?.("#page-size-select") ||
      (pagination.element as any)._pageSizeSelect;

    const event = new CustomEvent("cds-select-selected", {
      detail: { value: "50" },
      bubbles: true,
      composed: true,
    });
    Object.defineProperty(event, "composedPath", {
      value: () => [pageSizeSelect, pagination.element],
    });

    pagination.element.dispatchEvent(event);
    await flushPromises();

    // 3. Verify fetch called and URI has reset page=0 and size=50
    expect(mockFetch).toHaveBeenCalled();
    expect(uriRef.value).toContain("page=0&size=50");
  });

  it("updates page size using internal select value fallback when detail.value is absent", async () => {
    const wrapper = await createComponent();
    const pagination = wrapper.find("cds-pagination");

    // 1. Navigate to page 2 first
    pagination.element.dispatchEvent(
      new CustomEvent("cds-pagination-changed-current", {
        detail: { page: 2, pageSize: 20 },
        bubbles: true,
        composed: true,
      })
    );
    await flushPromises();

    const uriRef = (useFetchTo as any).mock.calls[0][0];
    expect(uriRef.value).toContain("page=1&size=20");

    mockFetch.mockClear();

    // 2. Fall back to internal select value
    const pageSizeSelect =
      (pagination.element as any).shadowRoot?.querySelector?.("#page-size-select") ||
      (pagination.element as any)._pageSizeSelect;
    if (pageSizeSelect) {
      pageSizeSelect.value = "50";
    }

    const event = new CustomEvent("cds-select-selected", {
      detail: {},
      bubbles: true,
      composed: true,
    });
    Object.defineProperty(event, "composedPath", {
      value: () => [pageSizeSelect, pagination.element],
    });

    pagination.element.dispatchEvent(event);
    await flushPromises();

    // 3. Verify fetch called and URI has reset page=0 and size=50
    expect(mockFetch).toHaveBeenCalled();
    expect(uriRef.value).toContain("page=0&size=50");
  });

  it("updates page size using paginationEl.pageSize fallback when select and detail are absent", async () => {
    const wrapper = await createComponent();
    const pagination = wrapper.find("cds-pagination");

    // 1. Navigate to page 2 first
    pagination.element.dispatchEvent(
      new CustomEvent("cds-pagination-changed-current", {
        detail: { page: 2, pageSize: 20 },
        bubbles: true,
        composed: true,
      })
    );
    await flushPromises();

    const uriRef = (useFetchTo as any).mock.calls[0][0];
    expect(uriRef.value).toContain("page=1&size=20");

    mockFetch.mockClear();

    // 2. Ensure internal select has no value and paginationEl has pageSize = 50
    const pageSizeSelect =
      (pagination.element as any).shadowRoot?.querySelector?.("#page-size-select") ||
      (pagination.element as any)._pageSizeSelect;
    if (pageSizeSelect) {
      pageSizeSelect.value = undefined;
    }
    Object.defineProperty(pagination.element, "pageSize", { value: 50, configurable: true, writable: true });

    const event = new CustomEvent("cds-select-selected", {
      detail: {},
      bubbles: true,
      composed: true,
    });
    Object.defineProperty(event, "composedPath", {
      value: () => [pageSizeSelect, pagination.element],
    });

    pagination.element.dispatchEvent(event);
    await flushPromises();

    // 3. Verify fetch called and URI has reset page=0 and size=50
    expect(mockFetch).toHaveBeenCalled();
    expect(uriRef.value).toContain("page=0&size=50");
  });

  it("ignores cds-select-selected event from pages-select", async () => {
    const wrapper = await createComponent();
    const pagination = wrapper.find("cds-pagination");

    // 1. Navigate to page 2 first
    pagination.element.dispatchEvent(
      new CustomEvent("cds-pagination-changed-current", {
        detail: { page: 2, pageSize: 20 },
        bubbles: true,
        composed: true,
      })
    );
    await flushPromises();

    const uriRef = (useFetchTo as any).mock.calls[0][0];
    expect(uriRef.value).toContain("page=1&size=20");

    mockFetch.mockClear();

    // 2. Dispatch select-selected from pages-select (page selector)
    const pagesSelect =
      (pagination.element as any).shadowRoot?.querySelector?.("#pages-select") ||
      (pagination.element as any)._pagesSelect ||
      document.createElement("div");
    pagesSelect.id = "pages-select";

    const event = new CustomEvent("cds-select-selected", {
      detail: { value: "3" },
      bubbles: true,
      composed: true,
    });
    Object.defineProperty(event, "target", { value: pagesSelect });
    Object.defineProperty(event, "composedPath", {
      value: () => [pagesSelect, pagination.element],
    });

    pagination.element.dispatchEvent(event);
    await flushPromises();

    // 3. Verify handler ignored it and page size is not overwritten with page number
    expect(uriRef.value).toContain("size=20");
    expect(uriRef.value).not.toContain("size=3");
  });
});
