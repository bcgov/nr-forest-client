import { createApp } from 'vue'
import VueTheMask from 'vue-the-mask'
import App from '@/App.vue'
import { router } from '@/routes'
import { featureFlags, backendUrl } from '@/CoreConstants'
import { masking, shadowPart } from '@/helpers/CustomDirectives'
import type { SessionProperties } from '@/dto/CommonTypesDto'
import ForestClientUserSession from '@/helpers/ForestClientUserSession'

// Importing Styles
import '@/styles'

const app = createApp(App)

app.use(router)
app.use(VueTheMask)
app.directive('masked', masking('.cds--text-input__field-wrapper input'))
app.directive('shadow', shadowPart)
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
