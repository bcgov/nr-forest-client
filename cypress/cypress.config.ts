import { defineConfig } from "cypress";

import {
  addCucumberPreprocessorPlugin,
} from "@badeball/cypress-cucumber-preprocessor";

import createBundler from
  "@bahmutov/cypress-esbuild-preprocessor";

import {
  createEsbuildPlugin,
} from "@badeball/cypress-cucumber-preprocessor/esbuild";

async function setupNodeEvents(
  on: Cypress.PluginEvents,
  config: Cypress.PluginConfigOptions
): Promise<Cypress.PluginConfigOptions> {
  await addCucumberPreprocessorPlugin(on, config);

  on(
    "file:preprocessor",
    createBundler({
      plugins: [createEsbuildPlugin(config)],
    })
  );

  return config;
}

export default defineConfig({
  e2e: {
    specPattern: "**/*.feature",
    setupNodeEvents,
    defaultCommandTimeout: 10000,
    pageLoadTimeout: 60000,
  },

  includeShadowDom: true,

  viewportHeight: 1080,
  viewportWidth: 1920,

  retries: {
    runMode: 3,
    openMode: 0,
  },
});
