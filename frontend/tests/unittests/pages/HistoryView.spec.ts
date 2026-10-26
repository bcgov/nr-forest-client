import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
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
    mockResponse.value = { headers: { "x-total-count": "100" } };
    mockLoading.value = false;
    (useFetchTo as any).mockImplementation(defaultFetchReturn);
  });

  afterEach(() => {
    document.body.innerHTML = "";
  });

  it("renders history view and pagination", async () => {
    const wrapper = await createComponent();
    expect(wrapper.exists()).toBe(true);
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
  });

  it("updates page size and resets page on cds-select-selected from page-size-select", async () => {
    const wrapper = await createComponent();
    const pagination = wrapper.find("cds-pagination");

    const pageSizeSelect = document.createElement("div");
    pageSizeSelect.id = "page-size-select";

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

    expect(mockFetch).toHaveBeenCalled();
  });
});
