import { useIntersectionObserver } from "@vueuse/core";
import { ref, watch } from "vue";
import type { Ref } from "vue";

const isClient = typeof window !== 'undefined' && typeof document !== 'undefined';
const defaultWindow = isClient ? window : void 0;

// Based on @vueuse/core
export default function useElementVisibility(element, options = {}) {
  const { window = defaultWindow, scrollTarget, threshold } = options;
  const elementIsVisible = ref<boolean>(undefined);
  
  let done: () => void;
  const elementIsVisibleRefPromise = new Promise<Ref<boolean>>((resolve) => {
    done = () => {
      resolve(elementIsVisible)
    };
  });
  const { stop } = useIntersectionObserver(
    element,
    ([{ isIntersecting }]) => {
      elementIsVisible.value = isIntersecting;

      // signalize the value is ready
      done();
    },
    {
      root: scrollTarget,
      window,
      threshold,
    }
  );
  return { elementIsVisibleRefPromise, stop };
}
