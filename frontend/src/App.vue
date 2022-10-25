<template>
  <div>
    <MainHeader />
    <div>
      <b-tabs pills card>
        <b-tab title="Home" active><p>Welcome to forest client!</p></b-tab>
        <b-tab title="My Application"><MyApplication /></b-tab>
        <b-tab title="Create a New Client"><CreateNewClient /></b-tab>
      </b-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import MainHeader from "./common/MainHeader.vue";
import SearchPage from "./pages/SearchPage.vue";
import MyApplication from "./pages/MyApplication.vue";
import CreateNewClient from "./pages/CreateNewClient.vue";

// composition api
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "App",
});
</script>

<style>
#app {
  /* font-family: Avenir, Helvetica, Arial, sans-serif; */
  font-family: -apple-system, BlinkMacSystemFont, BCSans, Roboto, Verdana, Arial,
    sans-serif;
  font-size: 0.875rem;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  /* color: #2c3e50; */
  color: rgba(0, 0, 0, 0.87);
}

.row > * {
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

.col {
  padding: 0px 0px 0px 0px;
  margin: 0px 0px 0px 0px;
}

.nav.nav-pills {
  background-color: #38598a !important;
  height: 45px;
}
.nav.nav-pills .nav-item button {
  color: #fff;
  height: 100%;
}
.nav.nav-pills .nav-item button[aria-selected="true"] {
  background-color: rgba(84, 117, 167, 1);
  border-color: rgba(84, 117, 167, 1);
  /* font-weight: bold; */
}
</style>
