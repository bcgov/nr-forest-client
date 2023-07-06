import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { inject } from 'vue'
import App from '@/App.vue'

describe('App.vue', () => {
  let mockKeycloak
  let push

  const TestComponent = {
    template: '<span id="provide-test">{{value.email}}</span>',
    setup: () => {
      const value = inject('submitterInformation')
      return { value }
    }
  }

  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: '/',
        name: 'home',
        component: TestComponent,
        props: true
      },
      {
        path: '/form',
        name: 'form',
        component: TestComponent,
        props: true
      },
      {
        path: '/internal',
        name: 'internal',
        component: TestComponent,
        props: true
      }
    ]
  })

  beforeEach(async () => {
    // Mock the injected keycloak and router
    mockKeycloak = {
      tokenParsed: {
        given_name: 'John',
        family_name: 'Doe',
        email: 'john.doe@example.com',
        display_name: 'John Doe',
        subject: '1234567890',
        identity_provider: 'idir'
      }
    }

    router.push('/')

    await router.isReady()

    push = vi.spyOn(router, 'push')
  })

  afterEach(() => {})

  it('should set submitter information and redirect for idir identity provider', async () => {
    const wrapper = mount(App, {
      global: {
        plugins: [router],
        provide: {
          keycloak: mockKeycloak
        }
      }
    })

    await wrapper.vm.$nextTick()

    console.log(wrapper.html())

    const span = await wrapper.find('#provide-test')

    expect(span.text()).toBe('john.doe@example.com')

    expect(push).toHaveBeenCalledTimes(1)
    expect(push).toHaveBeenCalledWith({
      name: 'internal'
    })
  })
  it('should set submitter information and redirect for bceid identity provider', async () => {
    mockKeycloak.tokenParsed.identity_provider = 'bceid'
    const wrapper = mount(App, {
      global: {
        plugins: [router],
        provide: {
          keycloak: mockKeycloak
        }
      }
    })

    await wrapper.vm.$nextTick()

    console.log(wrapper.html())

    const span = await wrapper.find('#provide-test')

    expect(span.text()).toBe('john.doe@example.com')

    expect(push).toHaveBeenCalledTimes(1)
    expect(push).toHaveBeenCalledWith({
      name: 'form'
    })
  })
})
