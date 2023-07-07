import './commands'

import { mount } from 'cypress/vue'
import '@cypress/code-coverage/support'

declare global {
  namespace Cypress {
    interface Chainable {
      mount: typeof mount
    }
  }
}

Cypress.Commands.add('mount', mount)
