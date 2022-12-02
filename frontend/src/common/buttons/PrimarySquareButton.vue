<template>
  <b-button
    variant="primary"
    class="primarySquareButton"
    :disabled="disabled"
    :style="style"
    @click="click"
    >{{ text }}</b-button
  >
</template>

<script setup lang="ts">
import { primary, primaryDisabled } from "../../utils/color";

const props = defineProps({
  text: String,
  disabled: { type: Boolean, default: false },
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
  border-color: v-bind(primary);
  margin-top: 16px;
  margin-bottom: 8px;
}

.primarySquareButton.disabled {
  background-color: v-bind(primaryDisabled);
  border-color: v-bind(primaryDisabled);
  margin-top: 16px;
  margin-bottom: 8px;
}
</style>
