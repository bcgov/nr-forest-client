/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import { mask } from 'vue-the-mask'

export const masking = (shadowSelector: string) => (el: any, binding: any) => {
  if (el.shadowRoot && binding.value) {
    const input = el.shadowRoot.querySelector(shadowSelector)
    if (input) {
      if (binding.value !== input.getAttribute('v-mask')) {
        input.setAttribute('v-mask', binding.value)
        mask(input, binding)
      }
    }
  }
}

const handleMutations = (mutationsList: any) => {
  for (const mutation of mutationsList) {
    if (mutation.type === 'childList') {
      for (const node of mutation.addedNodes) {
        if (node.nodeType === Node.ELEMENT_NODE) {
          if (node.getAttribute('part')) return
          node.setAttribute('part', node.tagName.toLowerCase())
        }
      }
    }
  }
}

export const shadowPart = {
  mounted: (el: any) => {
    if (el.shadowRoot) {
      if (!el.getAttribute('parting')) {
        const observer = new MutationObserver(handleMutations)
        observer.observe(el.shadowRoot, { childList: true, subtree: true })
        el.setAttribute('parting', 'true')
      }
    }
  },
}
