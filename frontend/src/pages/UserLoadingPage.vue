<template>
  <div>
    <div class="waiting">
      <bx-loading type="small"> </bx-loading>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { backendUrl } from "@/CoreConstants";
const router = useRouter();

</script>

<style scoped>
.waiting {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
.waiting bx-loading {
  height: 5.5rem;
}
</style>
