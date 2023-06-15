<template>
  <header class="wizard-header">
    <h5 class="wizard-title">{{ title }}</h5>
    <span class="wizard-subtitle">{{ subtitle }}</span>

    <bx-progress-indicator class="form-progress">
      <bx-progress-step
        class="form-progress-step"
        v-for="aslot in currentSlots"
        :key="aslot.props?.title"
        :label-text="aslot.props?.title"
        :secondary-label-text="'Step ' + (aslot.props?.index + 1)"
        state="complete"
      ></bx-progress-step>
    </bx-progress-indicator>
  </header>

  <slot :processValidity="processValidity" />

  <hr />

  <Note
    v-if="!isStateValid(currentTab)"
    note='All fields must be filled out correctly to enable the "Next" button below'
  /><br /><br />

  <bx-btn
    v-show="!isFirst"
    kind="secondary"
    iconLayout=""
    class="bx--btn"
    :disabled="isFirst"
    @click.prevent="onBack"
    size="field"
  >
  <span>Back</span>
  </bx-btn>

 
  <bx-btn
    kind="primary"
    iconLayout=""
    class="bx--btn"
    :disabled="isNextAvailable"
    v-show="!isLast"
    @click.prevent="onNext"
    size="field"
  >
    <span>Next</span>
    <arrowRight16 slot="icon"/>    
  </bx-btn>

  <bx-btn 
  kind="primary"
    iconLayout=""
    class="bx--btn"
    :disabled="isNextAvailable"
    size="field"
  v-show="isLast"> Submit </bx-btn>
</template>

<script setup lang="ts">
import { ref, computed, useSlots, provide, reactive } from "vue";
import arrowRight16 from "@carbon/icons-vue/es/arrow--right/16";
import Note from "@/common/NoteComponent.vue";

defineProps<{
  title: string;
  subtitle: string;
}>();

//Start from the first tab of the wizard
const currentTab = ref(0);

//Slot control to handle data from active slot
const slots = useSlots();
const currentSlots = slots.default ? slots.default() : [];

//Grab a list of tab status
const validTabs = reactive(
  currentSlots.map((tab) => {
    return { index: tab.props?.index, valid: false };
  })
);

//Provide the value of the current index to be consumed
//by anyone who want to consume it below
provide("currentIndex", currentTab);

//Validation and handling

//Check if current state is valid or not
const isStateValid = (index: number) => {
  const currentState = validTabs.find((state) => state.index === index);
  return currentState ? currentState.valid : false;
};

const processValidity = (tabStatus: { index: number; valid: boolean }) =>
  (validTabs[tabStatus.index] = tabStatus);

//Wizard State Management
const isFirst = computed(() => currentTab.value === 0);
const isLast = computed(() => currentTab.value === validTabs.length - 1);
const isCurrentValid = computed(() => isStateValid(currentTab.value));
const isNextAvailable = computed(() => !isCurrentValid.value || isLast.value);

//Button Actions
const onBack = () => {
  if (currentTab.value - 1 >= 0) {
    currentTab.value--;
  }
};

const onNext = () => {
  if (currentTab.value + 1 < currentSlots.length) {
    currentTab.value++;
  }
};
</script>

<style scoped>
.chefsBlue {
  background: #0073E6;
}
.wizard-header {
  align-items: flex-start;
  flex-direction: column;
  display: flex;
  padding: 0;
}
.wizard-title {
  /*Fixed heading styles/heading-05*/
  font-family: "BC Sans";
  font-size: 32px;
  font-weight: 400;
  line-height: 40px;
  letter-spacing: 0px;
  text-align: left;
}
.wizard-subtitle {
  font-family: "BC Sans";
  font-size: 14px;
  font-weight: 400;
  line-height: 20px;
  letter-spacing: 0.16px;
  text-align: left;
  margin-bottom: 24px;
}
.form-progress {
  margin-bottom: 40px;
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  padding: 0px 0px 40px 0px;
  align-self: stretch;
}
.form-progress-step::before {
  border-top: 2px solid #0073e6;
}
.form-progress-step:after {
  border-top: 2px solid #dfdfe1;
}
</style>
