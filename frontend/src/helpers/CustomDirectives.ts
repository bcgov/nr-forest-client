/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import { mask } from 'vue-the-mask'

export const masking = (shadowSelector: string) => (el: any, binding: any) => {
  if (el.shadowRoot && binding.value) {
    const input = el.shadowRoot.querySelector(shadowSelector)
    if (input) {
      input.setAttribute('v-mask', binding.value)
      const observer = new MutationObserver(() => mask(input, binding))
      observer.observe(input, { attributes: true })
    }
  }
}
