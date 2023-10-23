import { config } from '@vue/test-utils'
import directivesMap from "./src/directivesMap";

if (!globalThis.defined) {
  config.global.directives = directivesMap;
  globalThis.defined = true
}
