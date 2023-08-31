import { createApp } from 'vue'

import App from '@/App.vue'
import { router } from '@/routes'
import { featureFlags, backendUrl } from '@/CoreConstants'
import { masking } from '@/helpers/CustomDirectives'
import type { SessionProperties } from '@/dto/CommonTypesDto'
import ForestClientUserSession from '@/helpers/ForestClientUserSession'

// Importing Styles
import '@/styles'

const app = createApp(App)

app.use(router)
app.directive('mask', masking('.cds--text-input__field-wrapper  input'))
app.config.globalProperties.$session = ForestClientUserSession
app.config.globalProperties.$features = featureFlags
app.config.globalProperties.$backend = backendUrl
app.mount('#app')

declare module '@vue/runtime-core' {
  // eslint-disable-next-line no-unused-vars
  interface ComponentCustomProperties {
    $session: SessionProperties
    $features: Record<string, any>
    $backend: string
  }
}
