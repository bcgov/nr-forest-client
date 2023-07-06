import { describe, it, expect, afterEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref, watch } from 'vue'
import axios from 'axios'
import { useFetch, usePost } from '@/composables/useFetch'

describe('useFetch', () => {
  let axiosMock

  afterEach(() => {
    axiosMock.mockRestore()
  })

  it('should make a GET request using Axios', async () => {
    axiosMock = vi
      .spyOn(axios, 'request')
      .mockImplementation(() => Promise.resolve({ data: 'Mock data' }))

    const responseData = ref(null)

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
      baseURL: 'http://localhost:3000',
      headers: {
        'Access-Control-Allow-Origin': 'http://localhost:8080',
        'Content-Type': 'application/json'
      },
      skip: true,
      url: '/api/data'
    })
  })

  it('should make a POST request using Axios', async () => {
    axiosMock = vi
      .spyOn(axios, 'request')
      .mockImplementation(() => Promise.resolve({ data: 'Mock data' }))
    const responseData = ref(null)

    const TestComponent = {
      template: '<div></div>',
      setup: () => {
        const { fetch, data } = usePost(
          '/api/data',
          { name: 'test' },
          { skip: true }
        )
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
      baseURL: 'http://localhost:3000',
      headers: {
        'Access-Control-Allow-Origin': 'http://localhost:8080',
        'Content-Type': 'application/json'
      },
      skip: true,
      url: '/api/data',
      method: 'POST',
      data: { name: 'test' }
    })
  })

  it('should make a GET request using Axios and get an error', async () => {
    axiosMock = vi
      .spyOn(axios, 'request')
      .mockImplementation(() =>
        Promise.reject(
          new Error({ response: { status: 500, data: { message: 'Error' } } })
        )
      )
    const responseData = ref(null)

    const TestComponent = {
      template: '<div></div>',
      setup: () => {
        const { fetch, error } = useFetch('/api/data', { skip: true })
        watch(error, (value) => (responseData.value = value))
        fetch()
      }
    }

    watch(responseData, (value) => {
      expect(value).toStrictEqual(
        new Error({ response: { status: 500, data: { message: 'Error' } } })
      )
    })

    const wrapper = mount(TestComponent)

    await wrapper.vm.$nextTick()

    expect(axiosMock).toHaveBeenCalledWith({
      baseURL: 'http://localhost:3000',
      headers: {
        'Access-Control-Allow-Origin': 'http://localhost:8080',
        'Content-Type': 'application/json'
      },
      skip: true,
      url: '/api/data'
    })
  })

  it('should make a POST request using Axios and get an error', async () => {
    axiosMock = vi.spyOn(axios, 'request').mockImplementation(() =>
      Promise.reject(
        new Error({
          response: { status: 500, data: { message: 'Error' } }
        })
      )
    )
    const responseData = ref(null)

    const TestComponent = {
      template: '<div></div>',
      setup: () => {
        const { fetch, error } = usePost(
          '/api/data',
          { name: 'test' },
          { skip: true }
        )
        watch(error, (value) => (responseData.value = value))
        fetch()
      }
    }

    watch(responseData, (value) => {
      expect(value).toStrictEqual(
        new Error({ response: { status: 500, data: { message: 'Error' } } })
      )
    })

    const wrapper = mount(TestComponent)

    await wrapper.vm.$nextTick()

    expect(axiosMock).toHaveBeenCalledWith({
      baseURL: 'http://localhost:3000',
      headers: {
        'Access-Control-Allow-Origin': 'http://localhost:8080',
        'Content-Type': 'application/json'
      },
      skip: true,
      url: '/api/data',
      method: 'POST',
      data: { name: 'test' }
    })
  })
})
