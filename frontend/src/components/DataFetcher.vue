<script setup lang="ts">
import { ref, watch, computed } from 'vue'
// Composables
import { useFetchTo } from '@/composables/useFetch'


const props = defineProps<{
  url: string
  params?: object
  minLength: number
  initValue: object
  initFetch?: boolean
}>()

//Set the initial value to the content
const content = ref<any>(props.initValue)

const response = ref<any>()

const initialUrlValue = props.url
const searchURL = computed(() => props.url)

const { loading, error, fetch } = useFetchTo(searchURL, response, {
  skip: true,
  ...props.params
})

const calculateStringDifference = (
  initial: string,
  current: string
): number => {
  if (initial === current) return 0
  if (initial.length > current.length)
    return calculateStringDifference(current, initial)
  return current.replace(initial, '').length
}

//If initial fetch is required, fetch
if (props.initFetch) {
  fetch()
}

//Watch for changes in the url, and if the difference is greater than the min length, fetch
watch(
  () => props.url,
  () => {
    if (
      calculateStringDifference(initialUrlValue, props.url) >= props.minLength
    ) {
      const fetchUrl = props.url
      fetch().then(() => {
        // Make sure the requested input is up-to-date with current user's input.
        // This prevents an older request which took longer then the newest one from overwriting the results.
        if (fetchUrl === props.url) {
          content.value = response.value
        }
      })
    }
  }
)
</script>
<template>
  <slot :content="content" :loading="loading" :error="error"></slot>
</template>
