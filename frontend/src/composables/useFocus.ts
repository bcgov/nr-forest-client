import { ref } from 'vue'
import type { Ref } from 'vue'

type OptionalElement = Element | HTMLElement | undefined

/**
 * useFocus composable is used to set focus and/or scroll to a component
 * @returns two functions to set focus and scroll to a component
 */
export const useFocus = (): {
  setFocusedComponent: (componentName: string, time?: number) => Ref<OptionalElement>
  setScrollPoint: (componentName: string, time?: number) => Ref<OptionalElement>
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
  ): Ref<OptionalElement> => {
    const refComponent = ref<OptionalElement>(undefined)
    setTimeout(() => (refComponent.value = setActionOn(componentName, key, action)), time)
    return refComponent
  }

  // Set the focus on a component with the data-focus attribute
  const setFocusedComponent = (componentName: string, time: number = 100): Ref<OptionalElement> =>
    execute(
      componentName,
      'data-focus',
      (element) => (element instanceof HTMLElement ? element.focus() : undefined),
      time,
    )
  // Scroll into view a component with the data-scroll attribute
  const setScrollPoint = (componentName: string, time: number = 100): Ref<Element | undefined> =>
    execute(
      componentName,
      'data-scroll',
      (element) => element.scrollIntoView({ behavior: 'smooth', block: 'start' }),
      time,
    )

  return { setFocusedComponent, setScrollPoint }
}
