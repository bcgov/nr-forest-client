import './commands'
import '@cypress/code-coverage/support'

Cypress.on('uncaught:exception', (err) => {
  if (err.message.includes('ResizeObserver loop completed with undelivered notifications')) {
    return false
  }
  return true
})
