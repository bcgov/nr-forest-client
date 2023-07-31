import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'

import MainHeaderComponent from '@/components/MainHeaderComponent.vue'

describe('MainHeaderComponent.vue', () => {
  it('renders the component when authenticated', async () => {
    const session = {
      isLoggedIn: () => true,
      logOut: vi.fn()
    }

    const wrapper = mount(MainHeaderComponent, {
      global: {
        mocks: {
          $session: session
        }
      }
    })

    expect(wrapper.find('header').exists()).toBe(true)
    expect(wrapper.find('bx-btn').exists()).toBe(true)
    expect(wrapper.html()).toContain('Ministry of Forests')
  })

  it('renders the component when unauthenticated', async () => {
    const session = {
      isLoggedIn: () => false,
      logOut: vi.fn()
    }

    const wrapper = mount(MainHeaderComponent, {
      global: {
        mocks: {
          $session: session
        }
      }
    })

    expect(wrapper.find('header').exists()).toBe(true)
    expect(wrapper.find('bx-btn').exists()).toBe(false)
    expect(wrapper.html()).toContain('Ministry of Forests')
  })
})
