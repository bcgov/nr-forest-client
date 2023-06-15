<template>
  <div class="wizard-wrap">
     <div>
        <h4 class="form-header">{{title}}</h4>  
        <p class="inner-text" v-if="subtitle">{{subtitle}}</p>
      </div>
      
      <wizard-progress-indicator-component :model-value="progressData" />
  </div>

  <slot :processValidity="processValidity" />
  <div class="wizard-wrap">
    <hr />

    <span class="inner-text" v-if="!isStateValid(currentTab)">All fields must be filled out correctly to enable the "Next" button below</span>
  
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
      v-show="isLast">
      <span>Submit</span>
    </bx-btn>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, useSlots, provide, reactive } from "vue";
import arrowRight16 from "@carbon/icons-vue/es/arrow--right/16";

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

const stateIcon = (index: number) =>{
  console.log(`Index ${index} is ${(currentTab.value == index)} ${(currentTab.value < index)}`)
  if(currentTab.value == index) return "current";
  if(currentTab.value > index) return "complete";
  return "queued";
}


const progressData = computed(() => currentSlots.map((aSlot:any) => { return { title:aSlot.props?.title, subtitle:`Step ${aSlot.props?.index + 1}`, kind:stateIcon(aSlot.props?.index) } }));

</script>

<style scoped>
.form-progress {
  margin-bottom: 64px;
  display: flex;
  justify-content: space-between;
}
.form-progress-step{
  flex-grow: 1;
}

.form-progress-step > *{
  overflow: initial;
}
</style>
