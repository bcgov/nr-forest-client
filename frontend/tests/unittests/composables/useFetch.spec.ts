import { describe, it, expect, afterEach, vi, MockInstance } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref, watch } from 'vue'
import axios from 'axios'
import { useFetch, usePost, useFetchTo } from '@/composables/useFetch'

describe('useFetch', () => {
  let axiosMock

  afterEach(() => {
    if (axiosMock) {
      axiosMock.mockRestore();
    }
  });

  // This test assures we can use this value as initial data (before a fetch is performed).
  it("should preserve the reference to the supplied data parameter", () => {
    const dataRef = ref({ key: "value" });
    const result = useFetchTo("/", dataRef, { skip: true });
    expect(result.data).toBe(dataRef);
  });

  it('should make a GET request using Axios', async () => {
    axiosMock = vi
      .spyOn(axios, 'request')
      .mockImplementation(() => Promise.resolve({ data: 'Mock data' }))

    const responseData = ref('')

    const TestComponent = {
      template: '<div></div>',
      setup: () => {
        const { fetch, data } = useFetch('/api/data', { skip: true })
        watch(data, (value) => (responseData.value = value))
        fetch()
      }
    }

    watch(responseData, (value) => {
      expect(value).toEqual('Mock data')
    })

    const wrapper = mount(TestComponent)

    await wrapper.vm.$nextTick()

    expect(axiosMock).toHaveBeenCalledWith({
      baseURL: 'http://localhost:8080',
      headers: {
        'Access-Control-Allow-Origin': 'http://localhost:3000',
        'Content-Type': 'application/json',
        'Authorization': 'Bearer undefined',
      },
      skip: true,
      url: '/api/data'
    })
  })

  it('should make a POST request using Axios', async () => {
    axiosMock = vi
      .spyOn(axios, 'request')
      .mockImplementation(() => Promise.resolve({ data: 'Mock data' }))
    const responseData = ref('')

    const TestComponent = {
      template: '<div></div>',
      setup: () => {
        const { fetch, responseBody } = usePost(
          '/api/data',
          { name: 'test' },
          { skip: true }
        )
        watch(responseBody, (value) => (responseData.value = value))
        fetch()
      }
    }

    watch(responseData, (value) => {
      expect(value).toEqual('Mock data')
    })

    const wrapper = mount(TestComponent)

    await wrapper.vm.$nextTick()

    expect(axiosMock).toHaveBeenCalledWith({
      baseURL: 'http://localhost:8080',
      headers: {
        'Access-Control-Allow-Origin': 'http://localhost:3000',
        'Content-Type': 'application/json',
        'Authorization': 'Bearer undefined',
      },
      skip: true,
      url: '/api/data',
      method: 'POST',
      data: { name: 'test' }
    })
  })

  it("should make a GET request using Axios and get an error", async () => {
    axiosMock = vi
      .spyOn(axios, "request")
      .mockImplementation(() =>
        Promise.reject(new Error({ response: { status: 500, data: { message: "Error" } } })),
      );
    const responseData = ref(null);

    let fetchWrapper: ReturnType<typeof useFetch>;

    const doSpyHandleErrorDefault = () => vi.spyOn(fetchWrapper, "handleErrorDefault");
    let spyHandleErrorDefault: ReturnType<typeof doSpyHandleErrorDefault>;

    const TestComponent = {
      template: "<div></div>",
      setup: () => {
        fetchWrapper = useFetch("/api/data", { skip: true });
        // spyHandleErrorDefault = doSpyHandleErrorDefault();
        spyHandleErrorDefault = doSpyHandleErrorDefault();
        const { fetch, error } = fetchWrapper;
        watch(error, (value) => (responseData.value = value));
        fetch();
      },
    };

    watch(responseData, (value) => {
      expect(value).toStrictEqual(
        new Error({ response: { status: 500, data: { message: "Error" } } }),
      );
    });

    const wrapper = mount(TestComponent);

    await wrapper.vm.$nextTick();

    expect(axiosMock).toHaveBeenCalledWith({
      baseURL: "http://localhost:8080",
      headers: {
        "Access-Control-Allow-Origin": "http://localhost:3000",
        "Content-Type": "application/json",
        Authorization: "Bearer undefined",
      },
      skip: true,
      url: "/api/data",
    });

    // Await the axios mock to resolve
    await wrapper.vm.$nextTick();

    expect(spyHandleErrorDefault).toHaveBeenCalled();
  });

  it("should get an error from a GET request but not perform the default error handling", async () => {
    axiosMock = vi
      .spyOn(axios, "request")
      .mockImplementation(() =>
        Promise.reject(new Error({ response: { status: 500, data: { message: "Error" } } })),
      );
    const responseData = ref(null);

    let fetchWrapper: ReturnType<typeof useFetch>;

    const doSpyHandleErrorDefault = () => vi.spyOn(fetchWrapper, "handleErrorDefault");
    let spyHandleErrorDefault: ReturnType<typeof doSpyHandleErrorDefault>;

    const TestComponent = {
      template: "<div></div>",
      setup: () => {
        fetchWrapper = useFetch("/api/data", { skip: true, skipDefaultErrorHandling: true });
        spyHandleErrorDefault = doSpyHandleErrorDefault();
        const { fetch, error } = fetchWrapper;
        watch(error, (value) => (responseData.value = value));
        fetch();
      },
    };

    watch(responseData, (value) => {
      expect(value).toStrictEqual(
        new Error({ response: { status: 500, data: { message: "Error" } } }),
      );
    });

    const wrapper = mount(TestComponent);

    await wrapper.vm.$nextTick();

    expect(axiosMock).toHaveBeenCalledWith({
      baseURL: "http://localhost:8080",
      headers: {
        "Access-Control-Allow-Origin": "http://localhost:3000",
        "Content-Type": "application/json",
        Authorization: "Bearer undefined",
      },
      skip: true,
      skipDefaultErrorHandling: true,
      url: "/api/data",
    });

    // Await the axios mock to resolve
    await wrapper.vm.$nextTick();

    expect(spyHandleErrorDefault).not.toHaveBeenCalled();
  });

  it("should make a POST request using Axios and get an error", async () => {
    axiosMock = vi.spyOn(axios, "request").mockImplementation(() =>
      Promise.reject(
        new Error({
          response: { status: 500, data: { message: "Error" } },
        }),
      ),
    );
    const responseData = ref(null);

    let fetchWrapper: ReturnType<typeof usePost>;

    const doSpyHandleErrorDefault = () => vi.spyOn(fetchWrapper, "handleErrorDefault");
    let spyHandleErrorDefault: ReturnType<typeof doSpyHandleErrorDefault>;

    const TestComponent = {
      template: "<div></div>",
      setup: () => {
        fetchWrapper = usePost("/api/data", { name: "test" }, { skip: true });
        spyHandleErrorDefault = doSpyHandleErrorDefault();
        const { fetch, error } = fetchWrapper;
        watch(error, (value) => (responseData.value = value));
        fetch();
      },
    };

    watch(responseData, (value) => {
      expect(value).toStrictEqual(
        new Error({ response: { status: 500, data: { message: "Error" } } }),
      );
    });

    const wrapper = mount(TestComponent);

    await wrapper.vm.$nextTick();

    expect(axiosMock).toHaveBeenCalledWith({
      baseURL: "http://localhost:8080",
      headers: {
        "Access-Control-Allow-Origin": "http://localhost:3000",
        "Content-Type": "application/json",
        Authorization: "Bearer undefined",
      },
      skip: true,
      url: "/api/data",
      method: "POST",
      data: { name: "test" },
    });

    // Await the axios mock to resolve
    await wrapper.vm.$nextTick();

    expect(spyHandleErrorDefault).toHaveBeenCalled();
  });

  it("should get an error from a POST request but not perform the default error handling", async () => {
    axiosMock = vi.spyOn(axios, "request").mockImplementation(() =>
      Promise.reject(
        new Error({
          response: { status: 500, data: { message: "Error" } },
        }),
      ),
    );
    const responseData = ref(null);

    let fetchWrapper: ReturnType<typeof usePost>;

    const doSpyHandleErrorDefault = () => vi.spyOn(fetchWrapper, "handleErrorDefault");
    let spyHandleErrorDefault: ReturnType<typeof doSpyHandleErrorDefault>;

    const TestComponent = {
      template: "<div></div>",
      setup: () => {
        fetchWrapper = usePost(
          "/api/data",
          { name: "test" },
          { skip: true, skipDefaultErrorHandling: true },
        );
        spyHandleErrorDefault = doSpyHandleErrorDefault();
        const { fetch, error } = fetchWrapper;
        watch(error, (value) => (responseData.value = value));
        fetch();
      },
    };

    watch(responseData, (value) => {
      expect(value).toStrictEqual(
        new Error({ response: { status: 500, data: { message: "Error" } } }),
      );
    });

    const wrapper = mount(TestComponent);

    await wrapper.vm.$nextTick();

    expect(axiosMock).toHaveBeenCalledWith({
      baseURL: "http://localhost:8080",
      headers: {
        "Access-Control-Allow-Origin": "http://localhost:3000",
        "Content-Type": "application/json",
        Authorization: "Bearer undefined",
      },
      skip: true,
      skipDefaultErrorHandling: true,
      url: "/api/data",
      method: "POST",
      data: { name: "test" },
    });

    // Await the axios mock to resolve
    await wrapper.vm.$nextTick();

    expect(spyHandleErrorDefault).not.toHaveBeenCalled();
  });
});
