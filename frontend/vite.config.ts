import { fileURLToPath, URL } from 'url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import Icons from 'unplugin-icons/vite'
import IconsResolver from 'unplugin-icons/resolver'
import istanbul from 'vite-plugin-istanbul'

// https://vitejs.dev/config/
export default defineConfig(({ command, mode }) => {
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
      Icons(),
      istanbul({
        include: 'src/*',
        exclude: ['node_modules/', 'test/', 'cypress/', 'stub/', 'coverage/', 'reports/', 'reports/**/*', '.nyc_output/'],
        extension: ['.js', '.ts', '.vue'],
        requireEnv: true,
        nycrcPath: '.nycrc'
      })
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    test: {
      globals: true,
      reporters: ['verbose'],
      coverage: {
        reportsDirectory: 'reports/.vite_report',
        provider: 'istanbul',
        reporter: ['text', 'json', 'lcov'],
        all: true,
        clean: true,
        exclude: [
          '**/node_modules/**',
          '**/tests/**',
          '**/public/**',
          '**/.nyc*/**/*',
          '**/coverage/**/*',
          '**/reports/**/*',
          '**/stub/**/*',
          '.eslintrc.cjs',
          '.eslintrc.js',
          'components.d.ts',
          'env.d.ts',
          '**/routes.ts',
          '**/shims-vue.d.ts ',
          'vite.config.ts',
          '**/dto/**/*',
          '**/core/**/*',
          '**/CoreConstants.ts',
          '**/main.ts',
          '.prettierrc.js',
          '.eslintrc.js',
        ]
      },
      environment: 'jsdom'
    },
    optimizeDeps: false
  }
})
