import { config } from '@vue/test-utils'
import directivesMap from "./src/directivesMap";
import VueDOMPurifyHTML from "vue-dompurify-html";

if (!globalThis.defined) {
  config.global.directives = directivesMap;

  config.global.plugins = [VueDOMPurifyHTML];

  globalThis.defined = true;
}
