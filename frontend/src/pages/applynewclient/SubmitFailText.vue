<template>
  <div style="margin-bottom: 24px">
    <bi-exclamation-circle
      style="font-size: 16px; color: red; margin-bottom: 2px"
    />
    Sorry, your form submittion got an error. Please try it again in a few
    hours, or contact us at [our_contact_email].
  </div>
</template>

<script setup lang="ts">
import BiExclamationCircle from "~icons/bi/exclamation-circle";
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "SubmitFailText",
});
</script>

<style scoped></style>
