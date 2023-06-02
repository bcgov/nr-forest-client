<script setup lang="ts">
import { useFetchTo } from "@/services/ForestClientService";
import { ref, watch } from "vue";

const props = defineProps({
  url: { type: String, required: true },
  params: { type: Object, required: false },
  minLength: { type: Number, required: true },
  initValue: { type: Object, required: true },
  initFetch: { type: Boolean, required: false },
});

//Set the initial value to the content
const content = ref<any>(props.initValue);

const initialValue = props.url;

//If initial fetch is required, fetch
if (props.initFetch) {
  useFetchTo(props.url, content, props.params);
}

//If there is a watcher, watch for changes

watch(
  () => props.url,
  () => {
    if (props.url.length - initialValue.length >= props.minLength) {
      useFetchTo(props.url, content, props.params);
    }
  }
);
</script>
<template>
  <slot :content="content"></slot>
</template>
