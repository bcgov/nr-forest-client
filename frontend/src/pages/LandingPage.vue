<script setup lang="ts">
import Seeding from '../assets/images/seeding.png'
import logo from '../assets/images/bc-gov-logo.png'
import login16 from '@carbon/icons-vue/es/login/16'
</script>

<template>
  <div class="full-width">
    <div class="row landing-grid bx--row">
      <div class="bx--col-sm-8 bx--col-md-9 bx--col-lg-9">
        <img :src="logo" alt="BCGov Logo" width="160" class="logo" />
        <h1 id="landing-title" class="landing-title">Welcome to CLIENT</h1>
        <h2 id="landing-subtitle" class="landing-subtitle">
          The Ministry of Forests' client management system
        </h2>

        <p id="landing-desc" class="landing-desc">Check and manage client submissions</p>

        <bx-btn kind="primary" iconLayout="" class="landing-button">
          <span>Login with IDIR</span>
          <login16 slot="icon" />
        </bx-btn>
        <bx-btn kind="tertiary" iconLayout="" class="landing-button">
          <span>Login with Business BCeID</span>
          <login16 slot="icon" />
        </bx-btn>
      </div>
      <div class="bx--col-sm-8 bx--col-md-7 bx--col-lg-7 landing-img-column">
        <img
          :src="Seeding"
          alt="Small green seedling on the dirt and watered"
          class="landing-img"
        />
      </div>
    </div>
  </div>
</template>

<style lang="scss">
@import '@bcgov-nr/nr-fsa-theme/style-sheets/landing-page-components-overrides.scss';
</style>
