

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
