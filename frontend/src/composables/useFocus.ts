import { ref } from 'vue'

export default function useFocus () {
  const focusedComponent = ref(null)
  const changeFocus = () => {
    console.log(`[useFocus] changeFocus: ${focusedComponent.value}`, document.all)
    if (focusedComponent.value) {
      const element = document.querySelector(`[data-scroll="${focusedComponent.value}"]`)
      if (element) {
        console.log(`[useFocus] element: ${element}`)
        element.focus()
        element.scrollIntoView({ behavior: 'smooth', block: 'start' })
      } else {
        console.log('[useFocus] element not found')
      }
    }
  }
  const setFocusedComponent = (componentName:any) => {
    focusedComponent.value = componentName
    setTimeout(() => changeFocus(), 100)
  }
  return { setFocusedComponent }
}
