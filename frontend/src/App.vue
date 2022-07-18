<template>
  <div>
    <MainHeader />
    <b-container> Hello World! </b-container>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import MainHeader from "./common/MainHeader.vue";

export default defineComponent({
  name: "App",
  components: {
    MainHeader,
  },
  data() {
    return {};
  },
  methods: {},
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
</style>
