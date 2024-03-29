module.exports = {
  root: true,
  env: {
    browser: true,
    es2021: true,
    jest: true,
    node: true,
    'vue/setup-compiler-macros': true,
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
  extends: [
    'plugin:import/errors',
    'plugin:import/warnings',
    'plugin:import/typescript',
    'standard',
    'prettier',
    'plugin:vue/vue3-essential',
    'plugin:prettier/recommended',
    'eslint:recommended',
    '@vue/typescript/recommended',
    '@vue/eslint-config-typescript/recommended',
    '@vue/eslint-config-prettier',
  ],
  parser: 'vue-eslint-parser',
  parserOptions: {
    parser: '@typescript-eslint/parser',
    ecmaVersion: '11',
    sourceType: 'module',
  },
  plugins: ['@typescript-eslint', 'prettier', 'vue'],
  rules: {
    'vue/no-v-html': 'error',
    'vue/require-prop-types': 'error',
    'prettier/prettier': 'error',
    'import/extensions': ['error', 'always', { js: 'never', ts: 'never', vue: 'never' }],
    '@typescript-eslint/explicit-module-boundary-types': 'error',
    '@typescript-eslint/no-unused-vars': 'error',
    '@typescript-eslint/no-explicit-any': 'off',
  },
  overrides: [
    {
      files: ['cypress/integration/**.spec.{js,ts,jsx,tsx}'],
      extends: ['plugin:cypress/recommended'],
    },
  ],
  globals: {
    config: 'readable',
    cy: 'readonly',
    Cypress: 'readonly',
  },
};
