<script setup lang="ts">
import { ref, watch, computed } from "vue";
// Composables
import { useFetchTo } from "@/composables/useFetch";

const props = withDefaults(
  defineProps<{
    url: string;
    params?: object;
    minLength: number;
    initValue: object;
    emptyValue?: any;
    initFetch?: boolean;
    disabled?: boolean;
    debounce?: number;
  }>(),
  {
    minLength: 3,
    disabled: false,
    debounce: 300,
  },
);

// Set the initial value to the content
const content = ref<any>(props.initValue);

const emptyValue = computed(() => props.emptyValue || props.initValue);

const response = ref<any>();
const loading = ref<boolean>();

let debounceTimer: NodeJS.Timeout | null = null;

const initialUrlValue = props.url;
const searchURL = computed(() => props.url);

const { error, fetch } = useFetchTo(searchURL, response, {
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
  fetch().asyncResponse.then(() => {
    content.value = response.value;
  });
}

const controllerList: Record<string, AbortController> = {};

const abortOutdatedRequests = () => {
  Object.values(controllerList).forEach((controller) => {
    controller.abort();
  });
};

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
      content.value = [];
      const { asyncResponse, controller } = fetch();
      abortOutdatedRequests();
      controllerList[curRequestTime] = controller;
      asyncResponse
        .then(() => {
          // Disregard aborted requests
          /*
          Note: the asyncResponse of an aborted request is not rejected due to the error handling
          performed by useFetchTo.
          */
          if (!controller.signal.aborted) {
            content.value = response.value;
          }
        })
        .finally(() => {
          // Disregard aborted requests
          if (!controller.signal.aborted) {
            loading.value = false;
          }

          /*
          At this point the request has been either completed or aborted.
          So it's time to remove its controller from the list
          */
          delete controllerList[curRequestTime];
        });
    }, props.debounce); // Debounce time
  }
});

watch(
  () => props.disabled,
  () => {
    if (props.disabled) {
      abortOutdatedRequests();
      content.value = emptyValue.value;
    }
  },
);
</script>
<template>
  <slot :content="content" :loading="loading" :error="error"></slot>
</template>
