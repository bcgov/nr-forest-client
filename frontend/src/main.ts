// Base libraries
import { createApp } from "vue";
import VueDOMPurifyHTML from "vue-dompurify-html";
import { Amplify } from "aws-amplify";
import { CookieStorage } from 'aws-amplify/utils';
import { cognitoUserPoolsTokenProvider } from 'aws-amplify/auth/cognito';
import VueTheMask from "vue-the-mask";

// App Related
import App from "@/App.vue";
import { router } from "@/routes";
import directivesMap from "@/directivesMap";

// Session related
import type { SessionProperties } from "@/dto/CommonTypesDto";
import ForestClientUserSession from "@/helpers/ForestClientUserSession";

// Constants
import { featureFlags, backendUrl, awsconfig } from "@/CoreConstants";

// AWS Amplify and Cookie Storage
Amplify.configure(awsconfig);
cognitoUserPoolsTokenProvider.setKeyValueStorage(new CookieStorage());

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
