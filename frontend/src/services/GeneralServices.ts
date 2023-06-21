import { h, defineComponent } from 'vue'

/**
 * This file will have control over fetching of everything
 */
export const conversionFn = (code: any) => {
  return {
    value: { value: code.code, text: code.name },
    text: code.name,
    legalType: code.legalType
  }
}

export const buildFromSvg = (svg: any) =>
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
