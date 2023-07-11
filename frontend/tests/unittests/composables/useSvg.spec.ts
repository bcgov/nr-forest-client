import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import useSvg from '@/composables/useSvg'

describe('useSvg', () => {
  const svgElement = {
    elem: 'svg',
    attrs: {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '0 0 32 32',
      fill: 'currentColor',
      width: 64,
      height: 64
    },
    content: [
      {
        elem: 'path',
        attrs: {
          d: 'M150 0 L75 200 L225 200 Z'
        }
      }
    ],
    name: 'triangle'
  }
  it('should return a component', () => {
    const component = useSvg(svgElement)
    expect(component).toBeDefined()
  })
  it('should render the SVG component correctly', () => {
    const TestComponent = {
      template: '<div><SvgComponent /></div>',
      components: {
        SvgComponent: useSvg(svgElement)
      }
    }

    const wrapper = mount(TestComponent)

    expect(wrapper.html()).toContain(
      '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32" fill="currentColor" width="64" height="64">'
    )
    expect(wrapper.html()).toContain(
      '<path d="M150 0 L75 200 L225 200 Z"></path>'
    )
  })
})
