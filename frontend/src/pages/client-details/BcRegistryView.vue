<script setup lang="ts">
import { useFetchTo } from '@/composables/useFetch';
import { ref } from 'vue';

const props = defineProps<{
  clientNumber: string;
}>();

const data = ref();
const { loading } = useFetchTo(`/api/clients/bc-registry/${props.clientNumber}`, data);

</script>
<template>
  <div
    class="tab-panel tab-panel--empty"
    :class="{
      'flex-center': loading,
    }"
    v-if="loading || !data"
  >
    <cds-loading v-if="loading"></cds-loading>
    <span v-if="!loading && !data" class="body-compact-01">
      BC Registry information not available.
    </span>
  </div>
  <template v-else>
    <div class="tab-header space-between">
      <h3 class="padding-left-1rem">
        BC Registry information
      </h3>
    </div>
    <div class="tab-panel tab-panel--populated">
    </div>
  </template>
</template>
