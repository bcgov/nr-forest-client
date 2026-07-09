import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import Components from "unplugin-vue-components/vite";
import Icons from "unplugin-icons/vite";
import IconsResolver from "unplugin-icons/resolver";

export default defineConfig(async ({ command, mode }) => {
  const serverConfig: any = {
    watch: {
      ignored: [
        "**/coverage/**", 
        "**/node_modules/**", 
        "**/.git/**", 
        "**/dist/**", 
        "**/reports*/**", 
      ],
    },
  };
  
  if (command === "serve" && mode !== "test" && process.env.VITE_MODE !== "test" && process.env.NODE_ENV !== "test") {
    serverConfig.fs = {
      deny: [
        '**/.env',
        '**/.env.*',
        '**/*.pem',
        '**/*.key',
        '**/.git/**',
      ]
    };
  }

  return {
    css: {
      preprocessorOptions: {
        scss: {
          api: 'modern-compiler',
        },
      },
    },
    plugins: [
      vue({
        template: {
          compilerOptions: {
            isCustomElement: (tag) => tag.includes("cds-"),
          },
          transformAssetUrls: {
            includeAbsolute: false,
          },
        },
      }),
      Components({
        resolvers: [IconsResolver()],
      }),
      Icons(),
      ...(process.env.VITE_COVERAGE === "true"
        ? [
            await import("vite-plugin-istanbul").then(({ default: istanbul }) =>
              istanbul({
                include: "src/*",
                exclude: [
                  "node_modules/",
                  "test/",
                  "cypress/",
                  "stub/",
                  "coverage/",
                  "reports/",
                  "reports/**/*",
                  ".nyc_output/",
                ],
                extension: [".js", ".ts", ".vue"],
                requireEnv: true,
                nycrcPath: ".nycrc",
              })
            ),
          ]
        : []),
    ],
    resolve: {
      alias: {
        "@": fileURLToPath(new URL("./src", import.meta.url)),
      },
    },
    test: {
      globals: true,
      reporters: ["verbose"],
      coverage: {
        reportsDirectory: "reports/.vite_report",
        provider: "v8",
        reporter: ["text", "json", "lcov"],
        all: true,
        clean: true,
        exclude: [
          "**/node_modules/**",
          "**/tests/**",
          "**/public/**",
          "**/.nyc*/**/*",
          "**/coverage/**/*",
          "**/reports/**/*",
          "**/stub/**/*",
          ".eslintrc.cjs",
          ".eslintrc.js",
          "components.d.ts",
          "env.d.ts",
          "**/routes.ts",
          "**/shims-vue.d.ts ",
          "vite.config.ts",
          "eslint.config.mjs",
          "cypress.config.ts",
          "vitest.setup.ts",
          "stub-tests.ts",
          "**/dto/**/*",
          "**/core/**/*",
          "**/CoreConstants.ts",
          "**/main.ts",
          ".prettierrc.js",
          ".eslintrc.js",
        ],
      },
      environment: "jsdom",
      setupFiles: "vitest.setup.ts",
    },
    optimizeDeps: {
      exclude: ["@carbon/web-components"],
      entries:
        process.env.VITE_MODE === "test"
          ? ["./src/**/*.{vue,js,jsx,ts,tsx}"]
          : undefined,
    },
    build: {
      sourcemap: true,
      cssMinify: "esbuild",
    },
    server: serverConfig,
  };
});
