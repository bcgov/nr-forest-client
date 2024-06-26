<script setup lang="ts">
import { ref, watch, computed } from "vue";
// Composables
import { useFetchTo } from "@/composables/useFetch";

const props = defineProps<{
  url: string;
  params?: object;
  minLength: number;
  initValue: object;
  initFetch?: boolean;
  disabled?: boolean;
}>();

// Set the initial value to the content
const content = ref<any>(props.initValue);

const response = ref<any>();

const lastUpdateRequestTime = ref<number>(0);

const initialUrlValue = props.url;
const searchURL = computed(() => props.url);

const { loading, error, fetch } = useFetchTo(searchURL, response, {
  skip: true,
  ...props.params,
});

const calculateStringDifference = (
  initial: string,
  current: string
): number => {
  if (initial === current) return 0;
  if (initial.length > current.length)
    return calculateStringDifference(current, initial);
  return current.replace(initial, "").length;
};

// If initial fetch is required, fetch
if (!props.disabled && props.initFetch) {
  fetch().then(() => {
    content.value = response.value;
  });
}

// Watch for changes in the url, and if the difference is greater than the min length, fetch
watch([() => props.url, () => props.disabled], () => {
  if (!props.disabled && calculateStringDifference(initialUrlValue, props.url) >= props.minLength) {
    const curRequestTime = Date.now();

    fetch().then(() => {
      // Discard the response from old request when a newer one was already responded.
      if (curRequestTime >= lastUpdateRequestTime.value) {
        content.value = response.value;
        lastUpdateRequestTime.value = curRequestTime;
      }
    });
  }
});
</script>
<template>
  <slot :content="content" :loading="loading" :error="error"></slot>
</template>
