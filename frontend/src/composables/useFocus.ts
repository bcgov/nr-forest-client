import { ref } from 'vue'
import type { Ref } from 'vue'
import { isTouchScreen } from "./useScreenSize";
import useElementVisibility from "./useElementVisibility";

type OptionalElement = Element | HTMLElement | undefined

/**
 * useFocus composable is used to set focus and/or scroll to a component
 * @returns two functions to set focus and scroll to a component
 */
export const useFocus = (): {
  setFocusedComponent: (
    componentName: string,
    time?: number,
    callback?: (refComponent: Ref<OptionalElement>) => void,
  ) => Ref<OptionalElement>;
  safeSetFocusedComponent: (
    componentName: string,
    time?: number,
  ) => Ref<OptionalElement>;
  setScrollPoint: (
    componentName: string,
    time?: number,
  ) => Ref<OptionalElement>;
} => {
  // setActionOn is a function that execute the action on an a component
  const setActionOn = (
    focusedComponent: string,
    key: string,
    action: (target: Element | HTMLElement) => unknown,
  ) => {
    if (focusedComponent) {
      const element = document.querySelector(`[${key}="${focusedComponent}"]`)
      if (element) {
        action(element)
        return element
      }
      return undefined
    }
  }

  // execute is a function that execute the action on an a component after a delay
  // The delay is here to allow the component to be rendered before to execute the action
  const execute = (
    componentName: string,
    key: string,
    action: (target: Element | HTMLElement) => unknown,
    time: number = 100,
    callback?: (refComponent: Ref<OptionalElement>) => void,
  ): Ref<OptionalElement> => {
    const refComponent = ref<OptionalElement>(undefined)
    setTimeout(() => {
      refComponent.value = setActionOn(componentName, key, action)
      if (callback) {
        // We need to detect the end of the scrolling
        // Use event 'scroll' if browser does not support 'scrollend'
        const eventName = 'onscrollend' in window ? 'scrollend' : 'scroll'

        const scrollEndListener = () => {
          callback(refComponent)
          document.removeEventListener(eventName, polyfilledListener)
        }

        let scrollEndTimer: NodeJS.Timeout

        const polyfilledListener =
          eventName === 'scrollend'
            ? scrollEndListener
            : () => {
                clearTimeout(scrollEndTimer)
                scrollEndTimer = setTimeout(scrollEndListener, 100)
              }

        document.addEventListener(eventName, polyfilledListener)
      }
    }, time)
    return refComponent
  }

  // Set the focus on a component with the data-focus attribute
  const setFocusedComponent = (
    componentName: string,
    time: number = 100,
    callback?: (refComponent: Ref<OptionalElement>) => void,
  ): Ref<OptionalElement> =>
    execute(
      componentName,
      'data-focus',
      (element) => (element instanceof HTMLElement ? element.focus() : undefined),
      time,
      callback,
    )
  
  /**
   * Set the focus on a component with the data-focus attribute as long as:
   * - the component is already visible (100%);
   * - and the device is not a touch device (because we want to prevent the touch device's
   * virtual keyboard from either covering the component or triggering automatic scroll).
   * 
   * Otherwise, does nothing.
   */
  const safeSetFocusedComponent = (
    componentName: string,
    time: number = 100,
  ): Ref<OptionalElement> => {
    return execute(
      componentName,
      "data-focus",
      async (element) => {
        if (!(element instanceof HTMLElement) || isTouchScreen.value) {
          return;
        }

        const { elementIsVisibleRefPromise, stop } = useElementVisibility(element, {
          threshold: 1,
        });

        const elementIsVisibleRef = await elementIsVisibleRefPromise;
        stop();
        if (!elementIsVisibleRef.value) {
          return;
        }
        element.focus();
      },
      time,
    );
  };

  // Scroll into view a component with the data-scroll attribute
  const setScrollPoint = (
    componentName: string,
    time: number = 100,
    callback?: (refComponent: Ref<OptionalElement>) => void,
  ): Ref<Element | undefined> =>
    execute(
      componentName,
      'data-scroll',
      (element) => element.scrollIntoView({ behavior: 'smooth', block: 'start' }),
      time,
      callback,
    )

  return { safeSetFocusedComponent, setFocusedComponent, setScrollPoint };
}
