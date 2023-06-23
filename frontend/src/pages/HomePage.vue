<template>
  <div style="margin: 24px">Welcome to forest client!</div>
  <div>
    <router-link :to="{ path: '/new-client' }">New Client</router-link><br />
    <router-link :to="{ path: '/form-submission' }"
      >Form Submission</router-link
    >
  </div>
</template>

<script setup lang="ts"></script>

<style scoped></style>
