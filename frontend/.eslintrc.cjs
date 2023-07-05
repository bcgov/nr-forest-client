/* eslint-env node */
require('@rushstack/eslint-patch/modern-module-resolution')

module.exports = {
  root: true,
  'import/resolver': {
    node: {
      extensions: ['.js', '.jsx', '.ts', '.tsx', '.vue'],
      moduleDirectory: ['node_modules', 'src/']
    },
    typescript: {
      alwaysTryTypes: true
    }
  },
  extends: [
    'plugin:vue/vue3-essential',
    'plugin:import/errors',
    'plugin:import/warnings',
    'plugin:import/typescript',
    'eslint:recommended',
    'prettier',
    '@vue/eslint-config-typescript/recommended',
    '@vue/eslint-config-prettier'
  ],
  env: {
    'vue/setup-compiler-macros': true
  },
  overrides: [
    {
      files: ['cypress/integration/**.spec.{js,ts,jsx,tsx}'],
      extends: ['plugin:cypress/recommended']
    }
  ]
}
