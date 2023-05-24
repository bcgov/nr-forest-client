import { fileURLToPath, URL } from "url";
import "dotenv/config";
import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";
import Components from "unplugin-vue-components/vite";
import { BootstrapVue3Resolver } from "unplugin-vue-components/resolvers";
import Icons from "unplugin-icons/vite";
import IconsResolver from "unplugin-icons/resolver";

// https://vitejs.dev/config/
export default defineConfig(({ command, mode }) => {
  const env = loadEnv(mode, process.cwd());
  const port = parseInt(env.VITE_PORT || "8080");

  return {
    plugins: [
      vue(),
      Components({
        resolvers: [BootstrapVue3Resolver(), IconsResolver()],
      }),
      Icons(),
    ],
    resolve: {
      alias: {
        "@": fileURLToPath(new URL("./src", import.meta.url)),
      },
    },
    server: {
      port: port,
    },
    test: {
      globals: true,
      reporters: ["verbose"],
      coverage: {
        provider: 'c8',
        reporter: ['text', 'json', 'lcov']
      },
      environment: "jsdom"
    }
  };
});
