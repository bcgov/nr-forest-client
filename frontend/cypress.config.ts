import { defineConfig } from 'cypress'

export default defineConfig({
  env: {
    coverage: true,
    codeCoverage: {
      exclude: [
        '**/services/**/*',
        '**/helpers/**/*',
        '**/dto/**/*',
        '**/core/**/*',
        '**/composables/**/*',
        '**/.nyc*/**/*',
        '**/coverage/**/*',
        '**/src/routes.ts',
        '**/src/shims-vue.d.ts ',
        '**/src/CoreConstants.ts',
        '**/src/main.ts',
        '**/stub/**/*'
      ]
    }
  },

  e2e: {
    baseUrl: 'http://127.0.0.1:3000/',
    setupNodeEvents: (on, config) => {
      require('@cypress/code-coverage/task')(on, config)
      return config
    }
  },

  component: {
    devServer: {
      framework: 'vue',
      bundler: 'vite'
    },
    setupNodeEvents: (on, config) => {
      require('@cypress/code-coverage/task')(on, config)
      on('task', {
        log: message => {
          console.log(message);
          return null;
        },
      });
      return config
    }
  },

  retries: 0,

  includeShadowDom: true
})
