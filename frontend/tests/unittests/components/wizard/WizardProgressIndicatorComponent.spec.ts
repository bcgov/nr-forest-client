import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import WizardProgressIndicatorComponent from '@/components/wizard/WizardProgressIndicatorComponent.vue'

describe('WizardProgressIndicatorComponent.vue', () => {
  it('should render the component', () => {
    const wrapper = mount(WizardProgressIndicatorComponent, {
      props: {
        modelValue: [
          {
            kind: 'current',
            title: 'First step',
            subtitle: 'Classic step',
            enabled: true
          },
          {
            kind: 'queued',
            title: 'Second step',
            subtitle: 'Classic step',
            enabled: true
          }
        ]
      }
    })
    expect(wrapper.exists()).toBe(true)
  })

  it('should update component when queue moved', async () => {
    const wrapper = mount(WizardProgressIndicatorComponent, {
      props: {
        modelValue: [
          {
            kind: 'current',
            title: 'First step',
            subtitle: 'Classic step',
            enabled: true
          },
          {
            kind: 'queued',
            title: 'Second step',
            subtitle: 'Classic step',
            enabled: true
          }
        ]
      }
    })

    let step0Icon = await wrapper.find('[name="icon"]')
    expect(step0Icon.element.tagName).toBe('svg')
    expect(step0Icon.attributes('alt')).toBe('current')

    await wrapper.setProps({
      modelValue: [
        {
          kind: 'complete',
          title: 'First step',
          subtitle: 'Classic step',
          enabled: true
        },
        {
          kind: 'current',
          title: 'Second step',
          subtitle: 'Classic step',
          enabled: true
        }
      ]
    })

    step0Icon = await wrapper.find('[name="icon"]')
    expect(step0Icon.element.tagName).toBe('svg')
    expect(step0Icon.attributes('alt')).toBe('complete')
  })

  it('should be able to move back to previous step once is completed', async () => {
    const wrapper = mount(WizardProgressIndicatorComponent, {
      props: {
        modelValue: [
          {
            kind: 'complete',
            title: 'First step',
            subtitle: 'Classic step',
            enabled: true
          },
          {
            kind: 'current',
            title: 'Second step',
            subtitle: 'Classic step',
            enabled: true
          }
        ]
      }
    })

    const step0 = wrapper.find(
      'div[data-test="step-0"] > div.bx--progress-step-text > p > a'
    )
    expect(step0.exists()).toBe(true)

    await step0.trigger('click')
    expect(wrapper.emitted('go-to')![0][0]).toBe(0)
  })
})
