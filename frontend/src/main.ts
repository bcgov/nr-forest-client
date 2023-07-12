import { createApp } from 'vue'
import { Amplify } from 'aws-amplify'

import App from '@/App.vue'
import { router } from '@/routes'
import { amplifyConfig } from '@/CoreConstants'
import { masking } from '@/helpers/CustomDirectives'
import type { AmplifyCustomProperties } from '@/dto/CommonTypesDto'
import AmplifyUserSession from '@/helpers/AmplifyUserSession'

// Importing Styles
import '@/styles';

const app = createApp(App)

app.use(router)
app.directive('mask', masking('.bx--text-input__field-wrapper input'))
Amplify.configure(amplifyConfig)
app.config.globalProperties.$session = AmplifyUserSession
app.mount('#app')

declare module '@vue/runtime-core' {
  // eslint-disable-next-line no-unused-vars
  interface ComponentCustomProperties {
    $session: AmplifyCustomProperties
  }
}
