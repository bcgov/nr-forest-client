<template>
  <div v-if="display">
    <b-card no-body class="mb-1" style="margin-bottom: 10px !important">
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
            margin-top: 0.3rem;
            margin-bottom: 0.3rem;
            font-weight: bold;
            display: flex;
          "
        >
          <div v-if="!alwaysOpen">
            <bi-chevron-up
              v-if="visible"
              style="margin-right: 0.5rem; margin-top: -0.5rem; width: 0.9rem; height: 0.9rem;"
            />
            <bi-chevron-down
              v-else
              style="margin-right: 0.5rem; margin-top: -0.5rem; width: 0.9rem; height: 0.9rem;"
            />
          </div>
          {{ title }}
        </div>
      </b-card-header>
      <b-collapse :id="id" role="tabpanel" v-model="visible">
        <b-card-body style="margin: 16px 10px 16px 10px">
          <slot />  
        </b-card-body>
      </b-collapse>
    </b-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import BiChevronUp from "~icons/bi/chevron-up";
import BiChevronDown from "~icons/bi/chevron-down";

const props = defineProps({
  title: String,
  id: String,
  display: { type: Boolean, default: true },
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
