import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import App from '@/App.vue'

describe('App.vue', () => {
  const TestComponent = {
    template: '<span id="provide-test">Hello World</span>'
  }

  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: '/',
        name: 'home',
        component: TestComponent,
        props: true,
        meta: {
          format: 'internal'
        }
      },
      {
        path: '/form',
        name: 'form',
        component: TestComponent,
        props: true,
        meta: {
          format: 'internal'
        }
      },
      {
        path: '/internal',
        name: 'internal',
        component: TestComponent,
        props: true,
        meta: {
          format: 'internal'
        }
      }
    ]
  })

  const session = {
    session: { user: { provider: 'bcsc' } },
    isLoggedIn: () => true,
    logOut: vi.fn()
  }

  it('should set submitter information and redirect for idir identity provider', async () => {
    const wrapper = mount(App, {
      global: {
        stubs: {
          MainHeaderComponent: TestComponent
        },
        mocks: {
          $session: session
        },
        plugins: [router]
      }
    })

    await wrapper.vm.$nextTick()
    const span = await wrapper.find('#provide-test')

    expect(span.text()).toBe('Hello World')
  })
})
