import { createApp } from 'vue'

import App from '@/App.vue'
import { router } from '@/routes'
import { featureFlags, backendUrl } from '@/CoreConstants'
import { masking } from '@/helpers/CustomDirectives'
import type { AmplifyCustomProperties } from '@/dto/CommonTypesDto'
import AmplifyUserSession from '@/helpers/AmplifyUserSession'

// Importing Styles
import '@/styles'

const app = createApp(App)

app.use(router)
app.directive('mask', masking('.bx--text-input__field-wrapper input'))
app.config.globalProperties.$session = AmplifyUserSession
app.config.globalProperties.$features = featureFlags
app.config.globalProperties.$backend = backendUrl
app.mount('#app')

declare module '@vue/runtime-core' {
  // eslint-disable-next-line no-unused-vars
  interface ComponentCustomProperties {
    $session: AmplifyCustomProperties
    $features: Record<string, any>
    $backend: string
  }
}
