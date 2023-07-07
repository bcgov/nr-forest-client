import { defineConfig } from 'cypress'

export default defineConfig({
  env: { coverage: true },
  component: {
    specPattern: 'tests/**/*.cy.{ts,tsx}',
    devServer: {
      framework: 'vue',
      bundler: 'vite'
    },
    setupNodeEvents: (on, config) => {
      require('@cypress/code-coverage/task')(on, config)
      return config
    }
  }
})
