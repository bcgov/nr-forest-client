<template>
  <div style="margin: 24px">Welcome to forest client!</div>
  <div>
    <router-link :to="{ name: 'form' }">New Client</router-link><br />
    <router-link :to="{ name: 'internal' }">Form Submission</router-link><br />
    <router-link :to="{ name: 'login' }">Login</router-link>
  </div>
</template>

<script setup lang="ts"></script>
