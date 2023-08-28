import { ref } from 'vue'
/**
 * useFocus composable is used to set focus and/or scroll to a component
 * @returns two functions to set focus and scroll to a component
 */
export function useFocus () {
  // setActionOn is a function that execute the action on an a component
  const setActionOn = (focusedComponent:string, key: string,action:((target:any) => {})) => {
    console.log('setActionOn',focusedComponent,key)
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
  const execute = (componentName:string,key:string,action:((target:any) => {})) => {
    const refComponent = ref<Element|undefined>(undefined)
    setTimeout(() => (refComponent.value = setActionOn(componentName,key,action)), 100)
    return refComponent
  }

  // Set the focus on a component with the data-focus attribute
  const setFocusedComponent = (componentName:string) => (execute(componentName,'data-focus', (element) => element.focus()))
  // Scroll into view a component with the data-scroll attribute
  const setScrollPoint = (componentName:string) => (execute(componentName,'data-scroll', (element) => element.scrollIntoView({ behavior: 'smooth', block: 'start' })))

  return { setFocusedComponent, setScrollPoint }
}
