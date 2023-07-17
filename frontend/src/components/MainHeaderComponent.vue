

<template>
  <header>
    <div class="bx--header">
      <a href="https://gov.bc.ca">
        <img
          src="/img/bc_for_logo.png"
          alt="Go to the Government of British Columbia website"
          width="150"
        />
      </a>
      <label class="bx--header-site-name">Ministry of Forests</label>
      <bx-btn
        v-if="$keycloak"
        kind="tertiary"
        iconLayout=""
        class="bx--btn bx--btn-header"
        @click.prevent="$keycloak.logoutFn"
        size="field"
      >
        <span>Logout</span>
        <Logout16 slot="icon" />
      </bx-btn>
    </div>
  </header>
</template>

<script setup lang="ts">
import Logout16 from '@carbon/icons-vue/es/logout/16'
</script>

<style scoped>
/*header {
  background-color: #0073E6;
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
  margin: 5px 5px 0 18px;
  visibility: hidden;

  font-size: 15px;
  line-height: 18px;
  font-weight: 700;
  letter-spacing: 0.16px;
}*/


.bx--header {
  background-color: #0073E6 !important;
  display: relative;
  height: 3.5rem;
}

.bx--header-site-name {
  font-family: ‘BCSans’, ‘Noto Sans’, Verdana, Arial, sans-serif;
  font-size: 0.875rem;
  font-weight: 700;
  color: #F3F3F5;
  padding-left: 2rem;
  display: inline-block;
}

/*
  These are sample media queries only. Media queries are quite subjective
  but, in general, should be made for the three different classes of screen
  size: phone, tablet, full.
*/

/*@media screen and (min-width: 600px) and (max-width: 899px) {
  header h1 {
    font-size: calc(7px + 2.2vw);
    visibility: visible;
  }
}

@media screen and (min-width: 900px) {
  header h1 {
    visibility: visible;
  }
}*/
</style>
