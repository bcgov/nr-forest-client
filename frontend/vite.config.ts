import { fileURLToPath, URL } from 'url'
import 'dotenv/config'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import Icons from 'unplugin-icons/vite'
import IconsResolver from 'unplugin-icons/resolver'

// https://vitejs.dev/config/
export default defineConfig(({ command, mode }) => {
  const env = loadEnv(mode, process.cwd())
  const port = parseInt(env.VITE_PORT || '8080')

  return {
    plugins: [
      vue({
        template: {
          compilerOptions: {
            isCustomElement: (tag) => tag.includes('bx-')
          }
        }
      }),
      Components({
        resolvers: [IconsResolver()]
      }),
      Icons()
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port
    },
    test: {
      globals: true,
      reporters: ['verbose'],
      coverage: {
        provider: 'v8',
        reporter: ['text', 'json', 'lcov'],
        all: true,
        clean: true,
        exclude: [
          '**/node_modules/**',
          '**/tests/**',
          '**/public/**',
          '.eslintrc.cjs',
          '.eslintrc.js',
          'components.d.ts',
          'env.d.ts',
          'src/routes.ts',
          'src/shims-vue.d.ts '
        ]
      },
      environment: 'jsdom'
    }
  }
})
