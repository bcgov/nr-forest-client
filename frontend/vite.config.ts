import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import Components from "unplugin-vue-components/vite";
import Icons from "unplugin-icons/vite";
import IconsResolver from "unplugin-icons/resolver";
import istanbul from "vite-plugin-istanbul";

// https://vitejs.dev/config/
export default defineConfig(({ command, mode }) => {
  return {
    plugins: [
      vue({
        template: {
          transformAssetUrls: false,
          compilerOptions: {
            isCustomElement: (tag) => tag.includes("cds-"),
          },
        },
      }),
      Components({
        resolvers: [IconsResolver()],
      }),
      Icons(),
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
      }),
    ],
    resolve: {
      alias: {
        "@": fileURLToPath(new URL("./src", import.meta.url)),
      },
    },
    build: {
      cssMinify: "esbuild",
    },
    test: {
      globals: true,
      reporters: ["verbose"],
      coverage: {
        reportsDirectory: "reports/.vite_report",
        provider: "istanbul",
        reporter: ["text", "json", "lcov"],
        include: ["src/**/*.{ts,vue}"],
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
      exclude: [
        "**/node_modules/**",
        "**/dist/**",
        "**/cypress/**",
        "**/.{idea,git,cache,output,temp}/**",
        "**/{karma,rollup,webpack,vite,vitest,jest,ava,babel,nyc,cypress,tsup,build,eslint,prettier}.config.*",
      ],
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
    server: {
      watch: {
        ignored: [
          "**/coverage/**", // Ignore coverage directory
          "**/node_modules/**", // Ignore node_modules
          "**/.git/**", // Ignore .git directory
          "**/dist/**", // Ignore dist directory
          "**/reports*/**", // Ignore reports directory
        ],
      },
    },
  };
});
