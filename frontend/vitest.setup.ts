import { config } from '@vue/test-utils'
import directivesMap from "./src/directivesMap";
import VueDOMPurifyHTML from "vue-dompurify-html";

// jsdom can lose the Storage prototype when --localstorage-file is passed
// without a valid path. Patch localStorage back to a working implementation.
if (typeof window !== 'undefined' && typeof window.localStorage?.getItem !== 'function') {
  const store: Record<string, string> = {};
  Object.defineProperty(window, 'localStorage', {
    value: {
      getItem: (key: string) => Object.prototype.hasOwnProperty.call(store, key) ? store[key] : null,
      setItem: (key: string, value: string) => { store[key] = String(value); },
      removeItem: (key: string) => { delete store[key]; },
      clear: () => { Object.keys(store).forEach((k) => delete store[k]); },
      get length() { return Object.keys(store).length; },
      key: (i: number) => Object.keys(store)[i] ?? null,
    },
    writable: true,
    configurable: true,
  });
}

// IntersectionObserver is not available in jsdom; stub it so Carbon web
// components (e.g. CDSTabs) don't throw unhandled rejections.
if (typeof window !== 'undefined' && !('IntersectionObserver' in window)) {
  (window as any).IntersectionObserver = class {
    observe() {}
    unobserve() {}
    disconnect() {}
  };
}

if (!globalThis.defined) {
  config.global.directives = directivesMap;

  config.global.plugins = [VueDOMPurifyHTML];

  globalThis.defined = true;
}
