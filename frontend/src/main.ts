import { createApp } from "vue";
import VueTheMask from "vue-the-mask";
import App from "@/App.vue";
import { router } from "@/routes";
import directivesMap from "@/directivesMap";
import { featureFlags, backendUrl } from "@/CoreConstants";
import type { SessionProperties } from "@/dto/CommonTypesDto";
import ForestClientUserSession from "@/helpers/ForestClientUserSession";
import VueDOMPurifyHTML from "vue-dompurify-html";

// Importing Styles
import "@/styles";

const app = createApp(App);

app.use(router);
app.use(VueTheMask);
app.use(VueDOMPurifyHTML, {
  hooks: {
    afterSanitizeAttributes: (node) => {
      node.setAttribute("target", "_blank");
      node.setAttribute("rel", "noopener");
    },
  },
});

for (const directiveName in directivesMap) {
  const directive = directivesMap[directiveName as keyof typeof directivesMap];
  app.directive(directiveName, directive);
}
app.config.globalProperties.$session = ForestClientUserSession;
app.config.globalProperties.$features = featureFlags;
app.config.globalProperties.$backend = backendUrl;
app.mount("#app");

declare module "@vue/runtime-core" {
  // eslint-disable-next-line no-unused-vars
  interface ComponentCustomProperties {
    $session: SessionProperties;
    $features: Record<string, any>;
    $backend: string;
  }
}
