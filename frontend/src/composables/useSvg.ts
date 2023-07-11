import { h, defineComponent } from 'vue'

/**
 * Creates an SVG component from an svg pictogram object from @carbon/pictograms
 * @param svg The pictogram from @carbon/pictograms
 * @returns A component that renders the SVG
 */
const useSvg = (svg: any) =>
  defineComponent(() => {
    const { elem, attrs, content } = svg
    return () =>
      h(
        elem,
        attrs,
        content.map((_content: any) =>
          h(_content.elem, _content.attrs, _content.content)
        )
      )
  })

export default useSvg
