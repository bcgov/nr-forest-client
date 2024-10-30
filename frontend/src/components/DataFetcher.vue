<script setup lang="ts">
import { ref, watch, computed } from "vue";
// Composables
import { useFetchTo } from "@/composables/useFetch";

const props = withDefaults(defineProps<{
  url: string;
  params?: object;
  minLength: number;
  initValue: object;
  initFetch?: boolean;
  disabled?: boolean;
  debounce?: number;
}>(),
  {
    minLength: 3,
    disabled: false,
    debounce: 300,
  }
);

// Set the initial value to the content
const content = ref<any>(props.initValue);

const response = ref<any>();
const loading = ref<boolean>();

const lastUpdateRequestTime = ref<number>(0);
let debounceTimer: number | null = null;

const initialUrlValue = props.url;
const searchURL = computed(() => props.url);

const { loading: fetchLoading, error, fetch } = useFetchTo(searchURL, response, {
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


// Watch for changes in the fetch loading state
// Doing like this now due to the debounce
watch(() => fetchLoading.value, (newVal) => {
  loading.value = newVal;
});

// Watch for changes in the url, and if the difference is greater than the min length, fetch
watch([() => props.url, () => props.disabled], () => {
  if (!props.disabled && calculateStringDifference(initialUrlValue, props.url) >= props.minLength) {
    
    // added a manual loading state to set the loading state when the user types
    loading.value = true;
    const curRequestTime = Date.now();

    if (debounceTimer) {
      clearTimeout(debounceTimer);
    }
    debounceTimer = setTimeout(() => {
      fetch().then(() => {
        // Discard the response from old request when a newer one was already responded.
        if (curRequestTime >= lastUpdateRequestTime.value) {
          content.value = response.value;
          lastUpdateRequestTime.value = curRequestTime;
        }
      });
  }, props.debounce); // Debounce time
  }
});
</script>
<template>
  <slot :content="content" :loading="loading" :error="error"></slot>
</template>
