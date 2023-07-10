<script setup lang="ts">
import Seeding from '../assets/images/seeding.png'
import logo from '../assets/images/bc-gov-logo.png'
import login16 from '@carbon/icons-vue/es/login/16'
</script>

<template>
  <div class="full-width">
    <div class="row landing-grid">
      <div class="col-sm-6 col-md-7 col-lg-7">
        <img :src="logo" alt="BCGov Logo" width="160" class="logo" />
        <h1 id="landing-title" class="landing-title">Welcome to FAM</h1>
        <h2 id="landing-subtitle" class="landing-subtitle">
          Forestry Access Management
        </h2>

        <p id="landing-desc" class="landing-desc">Grant access to your users</p>

        <bx-btn kind="primary" iconLayout="" class="landing-button">
          <span>Login with IDIR</span>
          <login16 slot="icon" />
        </bx-btn>
        <bx-btn kind="tertiary" iconLayout="" class="landing-button">
          <span>Login with Business BCeID</span>
          <login16 slot="icon" />
        </bx-btn>
      </div>
      <div class="col-sm-6 col-md-5 col-lg-5 landing-img-column">
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
