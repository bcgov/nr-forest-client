/* eslint-disable import/no-unresolved */
/* eslint-disable no-undef */
/* eslint-disable no-unused-vars */
import './commands'

import { mount } from 'cypress/vue'
import '@cypress/code-coverage/support'

import VueDOMPurifyHTML from "vue-dompurify-html";
import '@/styles';
import directivesMap from '@/directivesMap';

Cypress.on('uncaught:exception', (err) => {
  if (err.message.includes('ResizeObserver loop completed with undelivered notifications')) {
    return false
  }
  return true
})

declare global {
  namespace Cypress {
    interface Chainable {
      mount: typeof mount
    }
  }
}

Cypress.Commands.add('mount', (component, options = {}) => {
  options.global = options.global || {};
  options.global.directives = directivesMap;
  options.global.plugins = options.global.plugins || [];
  options.global.plugins.push(VueDOMPurifyHTML);

  return mount(component, options);
});
