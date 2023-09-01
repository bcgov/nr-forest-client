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
