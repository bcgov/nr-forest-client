import path from 'node:path'
import { fileURLToPath } from 'node:url'

import js from '@eslint/js'
import eslintConfigPrettier from '@vue/eslint-config-prettier/skip-formatting'
import {
  configureVueProject,
  defineConfigWithVueTs,
  vueTsConfigs,
} from '@vue/eslint-config-typescript'
import cypressPlugin from 'eslint-plugin-cypress'
import globals from 'globals'
import importPlugin from 'eslint-plugin-import'
import pluginVue from 'eslint-plugin-vue'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

configureVueProject({
  rootDir: __dirname,
  scriptLangs: ['ts'],
  tsSyntaxInTemplates: true,
})

const sharedIgnores = [
  'coverage/**',
  'dist/**',
  'node_modules/**',
  'reports/**',
  'reports-merge/**',
  '.nyc_output/**',
  '.nyc_report/**',
  '.vite_report/**',
  'cypress/results/**',
  'cypress/reports/**',
  'cypress/screenshots/**',
  'cypress/videos/**',
  'stub-tests/**',
  '.cache/**',
  '.parcel-cache/**',
  '.vscode/**',
]

const vueCompilerMacros = {
  defineEmits: 'readonly',
  defineExpose: 'readonly',
  defineModel: 'readonly',
  defineProps: 'readonly',
  defineSlots: 'readonly',
  withDefaults: 'readonly',
}

const cypressFiles = ['cypress/**/*.{js,jsx,ts,tsx}', 'tests/**/*.cy.{js,jsx,ts,tsx}']

export default defineConfigWithVueTs(
  {
    ignores: sharedIgnores,
  },
  js.configs.recommended,
  importPlugin.flatConfigs.errors,
  importPlugin.flatConfigs.warnings,
  importPlugin.flatConfigs.typescript,
  ...pluginVue.configs['flat/essential'],
  vueTsConfigs.recommended,
  {
    files: ['**/*.{js,cjs,mjs,ts,tsx,vue}'],
    languageOptions: {
      ecmaVersion: 2021,
      globals: {
        ...globals.browser,
        ...globals.es2021,
        ...globals.node,
        ...vueCompilerMacros,
        config: 'readonly',
      },
      sourceType: 'module',
    },
    settings: {
      'import/resolver': {
        node: {
          extensions: ['.js', '.jsx', '.ts', '.tsx', '.vue'],
          moduleDirectory: ['node_modules', 'src/'],
        },
        typescript: {
          alwaysTryTypes: true,
        },
      },
    },
    rules: {
      '@typescript-eslint/explicit-module-boundary-types': 'error',
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-unused-vars': [
        'error',
        {
          argsIgnorePattern: '^_',
          caughtErrorsIgnorePattern: '^_',
        },
      ],
      'import/extensions': ['error', 'always', { js: 'never', ts: 'never', vue: 'never' }],
      'vue/no-v-html': 'error',
      'vue/require-prop-types': 'error',
    },
  },
  {
    files: ['**/*.{config,spec,cy}.{js,cjs,mjs,ts,tsx}', 'vitest.setup.ts', 'stub-tests.ts'],
    rules: {
      '@typescript-eslint/explicit-module-boundary-types': 'off',
    },
  },
  {
    files: ['cypress.config.ts'],
    rules: {
      '@typescript-eslint/no-require-imports': 'off',
    },
  },
  {
    ...cypressPlugin.configs.recommended,
    files: cypressFiles,
    rules: {
      ...cypressPlugin.configs.recommended.rules,
      'cypress/no-unnecessary-waiting': 'off',
      'cypress/unsafe-to-chain-command': 'off',
    },
  },
  eslintConfigPrettier,
)
