<template>
  <div style="margin-bottom: 24px">
    <b-icon-check-circle
      style="font-size: 16px; color: green; margin-bottom: 2px"
    />
    Thanks for your submission! Your form has been submitted successfully. Your
    confirmation ID is [{{ confirmationId }}]. We also sent a confirmation email
    to your email address provided. You can review or modify your application
    anytime under "My Application" tab.
  </div>
</template>

<script setup lang="ts">
// composition api
const props = defineProps({
  confirmationId: String,
});
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "SubmitSucessText",
});
</script>

<style scoped></style>
