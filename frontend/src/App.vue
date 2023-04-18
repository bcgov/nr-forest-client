<template>
  <div>
    <MainHeader />
    <div>
      <b-tabs pills card>
        <b-tab
          v-for="(tab, index) in tabs"
          :title="tab.title"
          :key="index"
          :active="index == 0"
          >
          <component :is="tab.content" :businessName="bceidBusinessName"/>
        </b-tab>
      </b-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { inject, ref } from "vue";
import MainHeader from "./common/MainHeaderComponent.vue";
import ReviewApplicationPage from "./pages/ReviewApplicationPage.vue";
import ApplyClientNumber from "./pages/applyclientnumber/ApplyClientNumberPage.vue";
import type { Ref, DefineComponent } from "vue";
import type { KeycloakInstance } from "keycloak-js";
import { navBlue, navSelectBlue } from "./utils/color";

const keycloak: KeycloakInstance | undefined = inject("keycloak");
let tabs: Ref<Array<{ title: string; content: DefineComponent }>> = ref([]);
let bceidBusinessName = ref();

if (keycloak && 
    keycloak.tokenParsed && 
    keycloak.tokenParsed.identity_provider === "idir") {
  tabs.value = [{ title: "Review Applications", content: ReviewApplicationPage }];
} 
else {
  tabs.value = [{ title: "Apply a New Client", content: ApplyClientNumber }];
  bceidBusinessName = keycloak && keycloak.tokenParsed ? keycloak.tokenParsed.displayed : "Dev Test Client Name";
}
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
  /* text-align: center; */
  /* color: #2c3e50; */
  color: rgba(0, 0, 0, 0.87);
}

.card-header {
  /*padding: 0.5rem 0.5rem 0.5rem 0.5rem !important;*/
}

.row {
  padding: 0px 0px 0px 0px !important;
  margin: 0px 0px 0px 0px !important;
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

.col-3 {
  padding-right: 1rem !important;
}

/* ------------ nav bar ------------------- */
.nav.nav-pills {
  background-color: v-bind(navBlue) !important;
  height: 45px;
}
.nav.nav-pills .nav-item button {
  color: #fff;
  height: 100%;
}
.nav.nav-pills .nav-item button[aria-selected="true"] {
  background-color: v-bind(navSelectBlue);
  border-color: v-bind(navSelectBlue);
}
</style>
