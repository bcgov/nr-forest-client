import { describe, it, expect } from 'vitest'
import { shallowMount } from '@vue/test-utils'

import MainHeader from '@/components/MainHeaderComponent.vue'

describe('MainHeader', () => {
  it('component defined', () => {
    const wrapper = shallowMount(MainHeader)
    expect(wrapper).toBeDefined()
  })

  it('renders find expected text successfully', () => {
    const wrapper = shallowMount(MainHeader)
    expect(wrapper.text()).toContain('Ministry of Forests')
  })
})
