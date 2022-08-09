<template>
  <div style="margin: 40px; text-align: left; display: flex">
    <div style="width: 48%">
      <h5 style="margin-bottom: 20px">{{ title }}</h5>
      <slot></slot>
      <b-button
        variant="primary"
        :style="'background-color:' + primary + ';margin-top: 20px'"
        :disabled="buttonDisabled"
        @click="onSearch()"
      >
        Search
      </b-button>
    </div>
    <div style="width: 48%; margin-left: 4%">
      <h5 style="margin-bottom: 20px">Search Result:</h5>
      <b-spinner v-if="loading" variant="primary"></b-spinner>
      <b-card style="height: 100%">{{ result }}</b-card>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import FormInput from "../common/FormInput.vue";
import { primary } from "../utils/color";

export default defineComponent({
  components: {
    FormInput,
  },
  props: {
    loading: { type: Boolean, default: false },
    result: {
      type: [Array, String],
      default: "",
    },
    buttonDisabled: { type: Boolean, default: true },
    title: { type: String, default: "" },
  },
  data() {
    return {
      primary,
    };
  },
  methods: {
    onSearch() {
      this.$emit("onSearch");
    },
  },
});
</script>

<style scoped></style>
