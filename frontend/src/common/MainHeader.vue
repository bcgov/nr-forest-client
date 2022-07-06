<template>
  <header>
    <div class="banner">
      <a href="https://gov.bc.ca">
        <img
          src="/bc_for_logo.png"
          alt="Go to the Government of British Columbia website"
        />
      </a>
      <h1>FSA - Old Growth</h1>
    </div>
  </header>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({});
</script>

<style scoped>
header {
  background-color: #036;
  border-bottom: 2px solid #fcba19;
  padding: 0 65px 0 65px;
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
}

header .banner {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin: 0 10px 0 0;
  /* border-style: dotted;
    border-width: 1px;
    border-color: lightgrey; */
}

header .other {
  display: flex;
  flex-grow: 1;
  /* border-style: dotted;
    border-width: 1px;
    border-color: lightgrey; */
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
    font-size: 2em;
    visibility: visible;
  }
}
</style>
