<template>
  <b-button
    variant="primary"
    class="primarySquareButton"
    :style="style"
    @click="click"
    >{{ text }}</b-button
  >
</template>

<script setup lang="ts">
import { primary } from "../../utils/color";

const props = defineProps({
  text: String,
  style: { type: String, default: "" },
});

const emit = defineEmits(["click"]);

const click = () => {
  emit("click");
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "PrimarySquareButton",
});
</script>

<style scoped>
.primarySquareButton {
  background-color: v-bind(primary);
  margin-top: 16px;
  margin-bottom: 8px;
}
</style>
