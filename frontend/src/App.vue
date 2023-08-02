<template>
  <main-header-component v-if="!$route.meta.hideHeader"></main-header-component>
  <router-view></router-view>
</template>

<script setup lang="ts"></script>

<style>
.row {
  padding: 0px 0px 0px 0px !important;
  margin: 0px 0px 0px 0px !important;
}

.container,
.container-fluid,
.container-xxl,
.container-xl,
.container-lg,
.container-md,
.container-sm {
  padding: 0px 0px 0px 0px !important;
  margin: 0px 0px 0px 0px !important;
  max-width: 100% !important;
}

.col-3 {
  padding-right: 1rem !important;
}

/* ------------ nav bar ------------------- */
.nav.nav-pills {
  background-color: #38598a !important;
  height: 45px;
}

.nav.nav-pills .nav-item button {
  color: #fff;
  height: 100%;
}

.nav.nav-pills .nav-item button[aria-selected='true'] {
  background-color: rgba(84, 117, 167, 1);
  border-color: rgba(84, 117, 167, 1);
}
</style>
