<template>
  <div class="bx--content">
    <div>Something went wrong</div>
    <p>{{ errorCode }}</p>
  </div>
</template>
<script setup lang="ts">
import {ref} from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
const errorCode = ref(router.currentRoute.value.query.error)
</script>
