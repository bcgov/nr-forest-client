<template>
  <header>
    <div class="banner">
      <a href="https://gov.bc.ca">
        <img
          src="/img/bc_for_logo.png"
          alt="Go to the Government of British Columbia website"
          width="150"
        />
      </a>
      <h1>Ministry of Forests</h1>
    </div>
    <b-button
      v-if="$keycloak"
      @click="$keycloak.logoutFn"
      variant="primary"
      style="margin: 12px"
    >
      Log Out
    </b-button>
  </header>
</template>

<script setup lang="ts">
import { headerBlue } from "../utils/color";
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "MainHeader",
});
</script>

<style scoped>
header {
  background-color: #0073e6;
  padding: 0 30px 0 30px;
  color: #fff;
  display: flex;
  height: 65px;
  top: 0px;
  position: static;
  width: 100%;
}

header h1 {
  font-family: ‘BCSans’, ‘Noto Sans’, Verdana, Arial, sans-serif;
  font-weight: normal; /* 400 */
  margin: 5px 5px 0 18px;
  visibility: hidden;

  font-size: 15px;
  line-height: 18px;
  font-weight: 700;
  letter-spacing: 0.16px;
}

header .banner {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  margin: 0 10px 0 0;
  flex-grow: 1;
}

/*
  These are sample media queries only. Media queries are quite subjective
  but, in general, should be made for the three different classes of screen
  size: phone, tablet, full.
*/

@media screen and (min-width: 600px) and (max-width: 899px) {
  header h1 {
    font-size: calc(7px + 2.2vw);
    visibility: visible;
  }
}

@media screen and (min-width: 900px) {
  header h1 {
    visibility: visible;
  }
}
</style>
