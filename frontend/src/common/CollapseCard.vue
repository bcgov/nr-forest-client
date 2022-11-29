<template>
  <b-card no-body class="mb-1">
    <b-card-header
      header-tag="header"
      :id="'header-' + id"
      role="tab"
      style="display: flex"
      @click="handleClick"
    >
      <div
        :class="visible ? null : 'collapsed'"
        :aria-expanded="visible ? 'true' : 'false'"
        :aria-controls="id"
        style="
          width: 100%;
          margin-top: 8px;
          margin-bottom: 8px;
          font-weight: bold;
          display: flex;
        "
      >
        <div v-if="!alwaysOpen">
          <bi-arrow-up-short
            v-if="visible"
            style="margin-bottom: 2px; margin-right: 2px"
          />
          <bi-arrow-down-short
            v-else
            style="margin-bottom: 2px; margin-right: 2px"
          />
        </div>
        {{ title }}
      </div>
    </b-card-header>
    <b-collapse :id="id" role="tabpanel" v-model="visible">
      <b-card-body style="margin: 16px 10px 16px 10px">
        <slot />
        <b-button
          v-if="nextId"
          variant="primary"
          :style="'background-color:' + primary + ';margin-top: 16px'"
          @click="openNext"
          >{{ nextText }}</b-button
        >
      </b-card-body>
    </b-collapse>
  </b-card>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import BiArrowUpShort from "~icons/bi/arrow-up-short";
import BiArrowDownShort from "~icons/bi/arrow-down-short";
import { primary } from "../utils/color";

const props = defineProps({
  title: String,
  id: String,
  defaultOpen: { type: Boolean, default: false },
  nextId: { type: String || null, default: null }, // id of the collapse content to open when click next button
  nextText: { type: String, default: "" }, // text on the next button
  alwaysOpen: { type: Boolean, default: false },
});

const visible = ref(props.defaultOpen);

const openNext = () => {
  if (
    document.getElementById(props.nextId) &&
    !document.getElementById(props.nextId).classList.contains("show")
  ) {
    document.getElementById("header-" + props.nextId).click();
  }
};
const handleClick = () => {
  if (!props.alwaysOpen) {
    visible.value = !visible.value;
  }
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "CollapseCard",
});
</script>

<style scoped></style>
