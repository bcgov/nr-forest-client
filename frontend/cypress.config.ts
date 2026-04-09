import { fileURLToPath, URL } from 'node:url';
import { defineConfig } from 'cypress'
import { mergeConfig, loadConfigFromFile } from 'vite';
import 'dotenv/config';

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
    },
    AWS_COGNITO_REGION: process.env.VITE_AWS_COGNITO_REGION,
    AWS_COGNITO_CLIENT_ID: process.env.VITE_AWS_COGNITO_CLIENT_ID,
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
      bundler: 'vite',
      async viteConfig() {
        const loaded = await loadConfigFromFile(
          { command: 'serve', mode: 'test' },
          undefined,
          process.cwd(),
        );
        return mergeConfig(loaded?.config ?? {}, {
          optimizeDeps: {
            noDiscovery: true,
            include: [],
          },
        });
      },
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
