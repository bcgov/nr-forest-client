import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick, ref } from 'vue'
import * as fetcher from '@/composables/useFetch'

import DataFetcher from '@/components/DataFetcher.vue'

describe('DataFetcher', () => {
  const mockedFetchTo = (url: string, received: any, config: any = {}) => ({
    response: ref({}),
    error: ref({}),
    data: received,
    loading: ref(false),
    fetch: async () => {
      received.value = { name: 'Loaded' }
    }
  })

  it('should render', () => {
    const wrapper = mount(DataFetcher, {
      props: {
        url: '/api/data',
        minLength: 1,
        initValue: { name: 'test' }
      }
    })
    expect(wrapper.html()).toBe('')
  })

  it('should render slot with initial data', () => {
    const wrapper = mount(DataFetcher, {
      props: {
        url: '/api/data',
        minLength: 1,
        initValue: { name: 'test' }
      },
      slots: {
        default: '<div>slot content is {{ content.name }}</div>'
      }
    })
    expect(wrapper.html()).toBe('<div>slot content is test</div>')
    expect(wrapper.find('div').text()).toBe('slot content is test')
  })

  it('should render slot with fetched data on init', async () => {
    vi.spyOn(fetcher, 'useFetchTo').mockImplementation(mockedFetchTo)

    const wrapper = mount(DataFetcher, {
      props: {
        url: '/api/data',
        minLength: 1,
        initValue: { name: 'test' },
        initFetch: true
      },
      slots: {
        default: '<div>slot content is {{ content.name }}</div>'
      }
    })

    await nextTick()

    expect(wrapper.html()).toBe('<div>slot content is Loaded</div>')
    expect(wrapper.find('div').text()).toBe('slot content is Loaded')
  })

  it('should render slot with fetched data on url change', async () => {
    vi.spyOn(fetcher, 'useFetchTo').mockImplementation(mockedFetchTo)

    const wrapper = mount(DataFetcher, {
      props: {
        url: '/api/data',
        minLength: 1,
        initValue: { name: 'test' }
      },
      slots: {
        default: '<div>slot content is {{ content.name }}</div>'
      }
    })

    expect(wrapper.html()).toBe('<div>slot content is test</div>')
    expect(wrapper.find('div').text()).toBe('slot content is test')

    await wrapper.setProps({ url: '/api/data/changed' })

    await nextTick()

    expect(wrapper.html()).toBe('<div>slot content is Loaded</div>')
    expect(wrapper.find('div').text()).toBe('slot content is Loaded')
  })
})
