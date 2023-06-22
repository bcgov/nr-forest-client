import { mask } from 'vue-the-mask'

export default (shadowSelector: string) => (el: any, binding: any) => {
  if (el.shadowRoot && binding.value) {
    const input = el.shadowRoot.querySelector(shadowSelector)
    if (input) {
      const observer = new MutationObserver(() => mask(input, binding))
      observer.observe(input, { attributes: true })
    }
  }
}
