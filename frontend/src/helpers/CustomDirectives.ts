/* eslint-disable @typescript-eslint/explicit-module-boundary-types */
import { nextTick } from "vue";
import { mask, tokens } from "vue-the-mask";

// add custom token
tokens.N = { pattern: /[0-9a-zA-Z]/, transform: (v) => v.toLocaleUpperCase() };
tokens.U = { pattern: /./, transform: (v) => v.toLocaleUpperCase() };

export const masking = (shadowSelector: string) => (el: any, binding: any) => {
  if (el.shadowRoot) {
    /*
    nextTick allows to wait for the HTML input element to be loaded, so the mask is properly
    applied before the first key gets typed on it.
    */
    nextTick(() => {
      const input = el.shadowRoot.querySelector(shadowSelector)
      if (input) {
        if (binding.value !== input.getAttribute('v-mask')) {
          input.setAttribute('v-mask', binding.value)
          if (binding.value) {
            mask(input, binding);
          } else {
            input.oninput = null;
          }
        }
      }
    });
  }
}

const setPart = (nodes: HTMLCollection, currentLevel: number, maxLevel: number) => {
  for (const node of nodes) {
    if (node.nodeType === Node.ELEMENT_NODE) {
      if (!node.getAttribute('part')) {
        const partValues = [node.tagName.toLowerCase()]
        if (node.className) {
          partValues.push(node.className)
        }
        node.setAttribute('part', partValues.join(' '))
      }

      if (currentLevel < maxLevel) {
        setPart(node.children, currentLevel + 1, maxLevel)
      }
    }
  }
}

const handleMutations = (maxLevel: number) => (mutationsList: any) => {
  for (const mutation of mutationsList) {
    if (mutation.type === 'childList') {
      setPart(mutation.addedNodes, 1, maxLevel)
    }
  }
}

export const shadowPart = {
  mounted: (el: any, binding = { value: 1 }) => {
    if (el.shadowRoot) {
      if (!el.getAttribute('parting')) {
        const observer = new MutationObserver(handleMutations(binding.value))
        observer.observe(el.shadowRoot, { childList: true, subtree: true })
        el.setAttribute('parting', 'true')
      }
    }
  },
}
