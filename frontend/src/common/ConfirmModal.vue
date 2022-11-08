<template>
  <b-modal
    v-model="computedValue"
    no-close-on-backdrop
    hide-header
    :ok-title="okayText"
    @cancel="onCancel()"
    @ok="onOkay()"
  >
    <p style="margin: 12px">Are you sure you wish to submit your form?</p>
  </b-modal>
</template>

<script setup lang="ts">
// composition api
import { ref, computed } from "vue";

const props = defineProps({
  value: Boolean,
  okayText: { type: String, default: "ok" },
});

const computedValue = computed({
  get() {
    return props.value;
  },
  set(newValue) {},
});

const emit = defineEmits(["onCancel", "onOkay"]);

const onCancel = () => {
  emit("onCancel");
};

const onOkay = () => {
  emit("onOkay");
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "ConfirmModal",
});
</script>

<style>
.modal-footer button.btn-primary {
  background-color: #036;
}
</style>
