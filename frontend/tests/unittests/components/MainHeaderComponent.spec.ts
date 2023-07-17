import { describe, it, expect, vi, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'

import MainHeaderComponent from '@/components/MainHeaderComponent.vue'

import AmplifyUserSession from '@/helpers/AmplifyUserSession'

describe('MainHeaderComponent.vue', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })
  it('renders the component when authenticated', async () => {
    vi.spyOn(AmplifyUserSession, 'isLoggedIn').mockImplementation(() =>
      Promise.resolve(true)
    )

    const wrapper = mount(MainHeaderComponent)

    await new Promise((resolve) => {
      setTimeout(() => {
        resolve(true)
      }, 10) // Small delay to allow computedAsync to catch up, nextTick doesn't work
    })

    console.log(wrapper.html())

    expect(wrapper.find('header').exists()).toBe(true)
    expect(wrapper.find('bx-btn').exists()).toBe(true)
    expect(wrapper.html()).toContain('Ministry of Forests')
  })

  it('renders the component when unauthenticated', async () => {
    vi.spyOn(AmplifyUserSession, 'isLoggedIn').mockImplementation(() =>
      Promise.resolve(false)
    )

    const wrapper = mount(MainHeaderComponent)

    expect(wrapper.find('header').exists()).toBe(true)
    expect(wrapper.find('bx-btn').exists()).toBe(false)
    expect(wrapper.html()).toContain('Ministry of Forests')
  })
})
