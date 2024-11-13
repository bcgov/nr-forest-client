import { defineConfig } from "cypress";
import * as webpack from "@cypress/webpack-preprocessor";
import { addCucumberPreprocessorPlugin } from "@badeball/cypress-cucumber-preprocessor";

async function setupNodeEvents(on, config) {
  await addCucumberPreprocessorPlugin(on, config);

  on("file:preprocessor", webpack({
    webpackOptions: {
      resolve: {
        extensions: [".ts", ".js"],
      },
      module: {
        rules: [
          {
            test: /\.ts$/,
            exclude: [/node_modules/],
            use: [{ loader: "ts-loader" }],
          },
          {
            test: /\.feature$/,
            use: [{
              loader: "@badeball/cypress-cucumber-preprocessor/webpack",
              options: config,
            }],
          },
        ],
      },
    },
  }));

  on("before:browser:launch", (browser, launchOptions) => {
    if (browser.name === "chrome" || browser.name === "electron") {
      launchOptions.args.push("--js-flags=--expose-gc");
      launchOptions.args.push("--enable-precise-memory-info");
    }
    return launchOptions;
  });

  return config;
}

export default defineConfig({
  e2e: {
    setupNodeEvents,
    defaultCommandTimeout: 15000,
    pageLoadTimeout: 45000,
    browser: "chrome",
    experimentalMemoryManagement: true,
    numTestsKeptInMemory: 1,
  },
  includeShadowDom: true,
  viewportHeight: 1080,
  viewportWidth: 1920,
  retries: {    
    runMode: 3,
    openMode: 0,
  },
});
