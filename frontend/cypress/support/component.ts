/* eslint-disable import/no-unresolved */
/* eslint-disable no-undef */
/* eslint-disable no-unused-vars */
import './commands'

import { mount } from 'cypress/vue'
import '@cypress/code-coverage/support'
import VueDOMPurifyHTML from "vue-dompurify-html";
import '@/styles'

declare global {
  namespace Cypress {
    interface Chainable {
      mount: typeof mount
    }
  }
}

Cypress.Commands.add('mount', (component, options = {}) => {
  options.global = options.global || {};
  options.global.plugins = options.global.plugins || [];
  options.global.plugins.push(VueDOMPurifyHTML);

  return mount(component, options);
});
