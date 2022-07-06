import { fileURLToPath, URL } from "url";
import "dotenv/config";
import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";

// https://vitejs.dev/config/
export default defineConfig(({ command, mode }) => {
  const env = loadEnv(mode, process.cwd());
  const port = parseInt(env.VITE_PORT || "8080");

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        "@": fileURLToPath(new URL("./src", import.meta.url)),
      },
    },
    server: {
      port: port,
    },
  };
});
