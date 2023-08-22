import { ref } from 'vue'
export default function useFocus () {
  let focusedComponent = ''
  const changeFocus = () => {
    if (focusedComponent) {
      const element = document.querySelector(`[data-scroll="${focusedComponent}"]`)
      if (element) {
        element.focus()
        element.scrollIntoView({ behavior: 'smooth', block: 'start' })
        return element
      }
      return undefined
    }
  }
  const setFocusedComponent = (componentName:string) => {
    focusedComponent = componentName
    const refComponent = ref<Element|undefined>(undefined)
    console.log('test0')
    setTimeout(() => {
      refComponent.value = changeFocus()
      console.log('test1', refComponent.value)
    }, 100)
    console.log('test2')
    return refComponent
  }
  return { setFocusedComponent }
}
