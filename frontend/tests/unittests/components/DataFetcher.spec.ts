import { describe, it, expect, vi, beforeEach } from "vitest";
import { mount, type VueWrapper } from "@vue/test-utils";
import { nextTick, ref, type Ref } from "vue";
import * as fetcher from "@/composables/useFetch";

import DataFetcher from "@/components/DataFetcher.vue";

vi.useFakeTimers();



describe("DataFetcher", () => {
  const mockedFetchTo = (url: Ref<string>, received: Ref<any>, config: any = {}) => ({
    response: ref({}),
    error: ref({}),
    data: received,
    loading: ref(false),
    fetch: async () => {
      received.value = { name: "Loaded" };
    },
  });

  const mockedFetchToFunction =
    (
      fetchData: (url: string) => Promise<any> = async () => ({
        name: "Loaded",
      }),
    ) =>
    (url: Ref<string>, received: Ref<any>, config: any = {}) => ({
      response: ref({}),
      error: ref({}),
      data: received,
      loading: ref(false),
      fetch: async () => {
        received.value = await fetchData(url.value);
        console.log(received.value);
      },
    });

  const simpleFetchData = async (url: string) => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ name: url });
      }, 1000);
    });
  };

  const mockFetchSimple = mockedFetchToFunction(simpleFetchData);

  let lastResponse: any;

  const fetchDataSleepByParam = async (url: string) => {
    const regex = /.*\/(.+)/;
    const regexResult = regex.exec(url);
    const lastParam = regexResult[1];
    const time = parseInt(lastParam);

    return new Promise((resolve) => {
      setTimeout(() => {
        const response = { name: url };
        resolve(response);

        // Just for checking on the tests.
        lastResponse = response;
      }, time);
    });
  };

  const mockFetchSleepByParam = mockedFetchToFunction(fetchDataSleepByParam);

  it("should render", () => {
    const wrapper = mount(DataFetcher, {
      props: {
        url: "/api/data",
        minLength: 1,
        initValue: { name: "test" },
      },
    });
    expect(wrapper.html()).toBe("");
  });

  it("should render slot with initial data", () => {
    const wrapper = mount(DataFetcher, {
      props: {
        url: "/api/data",
        minLength: 1,
        initValue: { name: "test" },
      },
      slots: {
        default: "<div>slot content is {{ content.name }}</div>",
      },
    });
    expect(wrapper.html()).toBe("<div>slot content is test</div>");
    expect(wrapper.find("div").text()).toBe("slot content is test");
  });

  it("should render slot with fetched data on init", async () => {
    vi.spyOn(fetcher, "useFetchTo").mockImplementation(mockedFetchTo);

    const wrapper = mount(DataFetcher, {
      props: {
        url: "/api/data",
        minLength: 1,
        initValue: { name: "test" },
        initFetch: true,
      },
      slots: {
        default: "<div>slot content is {{ content.name }}</div>",
      },
    });

    vi.advanceTimersByTime(305);
    await nextTick();

    expect(wrapper.html()).toBe("<div>slot content is Loaded</div>");
    expect(wrapper.find("div").text()).toBe("slot content is Loaded");
  });

  it("should not render slot with fetched data on init if disabled", async () => {
    vi.spyOn(fetcher, "useFetchTo").mockImplementation(mockedFetchTo);

    const wrapper = mount(DataFetcher, {
      props: {
        url: "/api/data",
        minLength: 1,
        initValue: { name: "test" },
        initFetch: true,
        disabled: true,
      },
      slots: {
        default: "<div>slot content is {{ content.name }}</div>",
      },
    });

    vi.advanceTimersByTime(305);
    await nextTick();

    // still the same
    expect(wrapper.html()).toBe("<div>slot content is test</div>");
    expect(wrapper.find("div").text()).toBe("slot content is test");
  });

  it("should render slot with fetched data on url change", async () => {
    vi.spyOn(fetcher, "useFetchTo").mockImplementation(mockedFetchTo);

    const wrapper = mount(DataFetcher, {
      props: {
        url: "/api/data",
        minLength: 1,
        initValue: { name: "test" },
        debounce: 1,
      },
      slots: {
        default: "<div>slot content is {{ content.name }}</div>",
      },
    });

    expect(wrapper.html()).toBe("<div>slot content is test</div>");
    expect(wrapper.find("div").text()).toBe("slot content is test");

    await wrapper.setProps({ url: "/api/data/changed" });
    
    vi.advanceTimersByTime(305);
    await nextTick();
    await wrapper.vm.$nextTick();

    expect(wrapper.html()).toBe("<div>slot content is Loaded</div>");
    expect(wrapper.find("div").text()).toBe("slot content is Loaded");
  });

  it("should not update the rendered slot if disabled", async () => {
    vi.spyOn(fetcher, "useFetchTo").mockImplementation(mockedFetchTo);

    const wrapper = mount(DataFetcher, {
      props: {
        url: "/api/data",
        minLength: 1,
        initValue: { name: "test" },
        disabled: true,
        debounce: 0,
      },
      slots: {
        default: "<div>slot content is {{ content.name }}</div>",
      },
    });

    expect(wrapper.html()).toBe("<div>slot content is test</div>");
    expect(wrapper.find("div").text()).toBe("slot content is test");

    await wrapper.setProps({ url: "/api/data/changed" });

    vi.advanceTimersByTime(305);
    await nextTick();

    // still the same
    expect(wrapper.html()).toBe("<div>slot content is test</div>");
    expect(wrapper.find("div").text()).toBe("slot content is test");
  });

  it("should render slot with fetched data on enabled", async () => {
    vi.spyOn(fetcher, "useFetchTo").mockImplementation(mockedFetchTo);

    const wrapper = mount(DataFetcher, {
      props: {
        url: "/api/data",
        minLength: 1,
        initValue: { name: "test" },
        disabled: true,
        debounce: 1,
      },
      slots: {
        default: "<div>slot content is {{ content.name }}</div>",
      },
    });

    expect(wrapper.html()).toBe("<div>slot content is test</div>");
    expect(wrapper.find("div").text()).toBe("slot content is test");

    await wrapper.setProps({
      disabled: false,
      url: "/api/data/changed",
    });

    vi.advanceTimersByTime(305);
    await nextTick();
    await wrapper.vm.$nextTick();

    expect(wrapper.html()).toBe("<div>slot content is Loaded</div>");
    expect(wrapper.find("div").text()).toBe("slot content is Loaded");
  });

  it("should clear the previous content once a new fetch starts", async () => {
    vi.spyOn(fetcher, "useFetchTo").mockImplementation(mockFetchSimple);

    const wrapper = mount(DataFetcher, {
      props: {
        url: "/api/",
        minLength: 1,
        initValue: { name: "test" },
        debounce: 1,
      },
      slots: {
        default: "<div>slot content is {{ content.name }}</div>",
      },
    });

    expect(wrapper.find("div").text()).toBe("slot content is test");

    await wrapper.setProps({
      url: "/api/one",
    });

    await vi.advanceTimersByTimeAsync(1001);

    expect(wrapper.find("div").text()).toBe("slot content is /api/one");

    await wrapper.setProps({
      url: "/api/two",
    });

    // Not enough time to get the response
    await vi.advanceTimersByTimeAsync(10);

    // It becomes empty
    expect(wrapper.find("div").text()).toBe("slot content is");

    // More time but still not enough time to get the response
    await vi.advanceTimersByTimeAsync(900);

    // It's still empty
    expect(wrapper.find("div").text()).toBe("slot content is");

    // Remaining time
    await vi.advanceTimersByTimeAsync(91);

    expect(wrapper.find("div").text()).toBe("slot content is /api/two");
  });

  describe("when there is a request in progress", () => {
    let wrapper: VueWrapper;
    beforeEach(async () => {
      vi.spyOn(fetcher, "useFetchTo").mockImplementation(mockFetchSleepByParam);

      wrapper = mount(DataFetcher, {
        props: {
          url: "/api/",
          minLength: 1,
          initValue: { name: "test" },
          debounce: 1,
        },
        slots: {
          default: "<div>slot content is {{ content.name }}</div>",
        },
      });

      expect(wrapper.find("div").text()).toBe("slot content is test");

      await wrapper.setProps({
        url: "/api/one/1000",
      });
    });

    describe("and a new request gets started before the first one gets responded", () => {
      beforeEach(async () => {
        // Not enough time to get the response from api/one/1000
        await vi.advanceTimersByTimeAsync(500);

        await wrapper.setProps({
          url: "/api/two/1000",
        });
      });

      it("should discard the response from the first request", async () => {
        // More time, enough to get the response from /api/one/1000
        await vi.advanceTimersByTimeAsync(600);

        // It was effectively responded
        expect(lastResponse?.name).toEqual("/api/one/1000");

        // It should remain empty regardless
        expect(wrapper.find("div").text()).toBe("slot content is");

        // More time, enough to get the response from /api/two/1000
        await vi.advanceTimersByTimeAsync(410);

        expect(wrapper.find("div").text()).toBe("slot content is /api/two/1000");
      });
    });

    describe("and a new request gets started and responded before the first one gets responded", () => {
      beforeEach(async () => {
        // Not enough time to get the response from api/one/1000
        await vi.advanceTimersByTimeAsync(100);

        await wrapper.setProps({
          url: "/api/two/500",
        });
      });

      it("should discard the response from the first request", async () => {
        // Enough to get only the response from /api/two/500
        await vi.advanceTimersByTimeAsync(600);

        expect(wrapper.find("div").text()).toBe("slot content is /api/two/500");

        // More time, enough to get the response from /api/one/1000
        await vi.advanceTimersByTimeAsync(310);

        // It was effectively responded
        expect(lastResponse?.name).toEqual("/api/one/1000");

        // But it should keep the current value
        expect(wrapper.find("div").text()).toBe("slot content is /api/two/500");
      });
    });
  });
});
