<script setup lang="ts">
import { useFetchTo } from '@/services/ForestClientService'
import { ref, watch } from 'vue'

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

const fetchContent = () => useFetchTo(props.url, content, props.params)

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
  fetchContent()
}

//If there is a watcher, watch for changes

watch(
  () => props.url,
  () => {
    if (
      calculateStringDifference(initialUrlValue, props.url) >= props.minLength
    ) {
      fetchContent()
    }
  }
)
</script>
<template>
  <slot :content="content"></slot>
</template>
