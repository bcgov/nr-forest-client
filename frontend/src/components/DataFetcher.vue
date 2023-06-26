<script setup lang="ts">
import { useFetchTo } from '@/services/ForestClientService'
import { ref, watch, computed } from 'vue'

const props = defineProps<{
  url: string
  params?: object
  minLength: number
  initValue: object
  initFetch?: boolean
}>()

//Set the initial value to the content
const content = ref<any>(props.initValue)

const initialUrlValue = props.url
const searchURL = computed(() => props.url)

const { loading, error, fetch } = useFetchTo(searchURL, content, {
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
      fetch()
    }
  }
)
</script>
<template>
  <slot :content="content" :loading="loading" :error="error"></slot>
</template>
