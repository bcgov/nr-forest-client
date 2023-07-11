import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'

import NoteComponent from '@/components/NoteComponent.vue'

describe('NoteComponent.vue', () => {
  it('renders the note', () => {
    const wrapper = mount(NoteComponent, {
      props: {
        note: 'This is a note'
      }
    })

    expect(wrapper.text()).toContain('This is a note')
  })
})
