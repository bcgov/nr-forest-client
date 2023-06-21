<template>
  <div class="wizard-wrap">
    <div class="wizard-head">
      <div class="wizard-head-text">
        <h4 class="form-header">{{ title }}</h4>
        <p class="inner-text" v-if="subtitle">{{ subtitle }}</p>
      </div>

      <bx-toast-notification
        v-if="toastContent.active"
        class="wizard-head-toast"
        timeout="8000"
        kind="success"
        title="Success"
        :subtitle="
          '“' +
          toastContent.name +
          '” additional ' +
          toastContent.kind +
          ' was deleted successfully'
        "
      >
      </bx-toast-notification>
    </div>
    <wizard-progress-indicator-component
      :model-value="progressData"
      @go-to="goToStep"
    />
  </div>

  <slot :processValidity="processValidity" :goToStep="goToStep" />

  <div class="wizard-wrap" v-if="isLast">
    <hr />

    <div>
      <bx-btn
        kind="secondary"
        iconLayout=""
        class="bx--btn rounded"
        @click.prevent="onBack"
        size="field"
      >
        <span>Back</span>
      </bx-btn>

      <bx-btn kind="primary" iconLayout="" class="bx--btn rounded" size="field">
        <span>Submit</span>
      </bx-btn>
    </div>
  </div>

  <div class="wizard-wrap" v-if="!isLast && !isFormValid">
    <hr />

    <span class="inner-text" v-if="!isStateValid(currentTab)"
      >All fields must be filled out correctly to enable the "Next" button
      below</span
    >
    <div>
      <bx-btn
        v-show="!isFirst"
        kind="secondary"
        iconLayout=""
        class="bx--btn rounded"
        :disabled="isFirst"
        @click.prevent="onBack"
        size="field"
      >
        <span>Back</span>
      </bx-btn>

      <bx-btn
        kind="primary"
        iconLayout=""
        class="bx--btn rounded"
        :disabled="isNextAvailable"
        v-show="!isLast"
        @click.prevent="onNext"
        size="field"
      >
        <span>Next</span>
        <arrowRight16 slot="icon" />
      </bx-btn>
    </div>
  </div>

  <div class="wizard-wrap" v-if="!isLast && isFormValid">
    <hr />
    <div>
      <bx-btn
        kind="primary"
        iconLayout=""
        class="bx--btn rounded"
        :disabled="isNextAvailable"
        v-show="!isLast"
        @click.prevent="goToStep(3)"
        size="field"
      >
        <span>Save</span>
        <save16 slot="icon" />
      </bx-btn>
    </div>
  </div>

  <bx-modal
    id="modal-example"
    :open="modalContent.active"
    @bx-modal-closed="closeModal"
  >
    <bx-modal-header>
      <bx-modal-close-button></bx-modal-close-button>
      <bx-modal-label>Delete additional {{ modalContent.kind }}</bx-modal-label>
      <bx-modal-heading
        >Are you sure you want to delete "{{ modalContent.name }}" additional
        {{ modalContent.kind }}</bx-modal-heading
      >
    </bx-modal-header>
    <bx-modal-body><p></p></bx-modal-body>

    <bx-modal-footer>
      <bx-btn kind="secondary" data-modal-close>Cancel</bx-btn>
      <bx-btn kind="danger" @click.prevent="deleteContentModal"
        >Delete additional {{ modalContent.kind }}</bx-btn
      >
    </bx-modal-footer>
  </bx-modal>
</template>

<script setup lang="ts">
import { ref, computed, useSlots, reactive, provide } from "vue";
import { useEventBus } from '@vueuse/core';
import arrowRight16 from "@carbon/icons-vue/es/arrow--right/16";
import save16 from "@carbon/icons-vue/es/save/16";

import type { ModalNotification } from "@/core/CommonTypes";

defineProps<{
  title: string;
  subtitle: string;
}>();

//Defining the event bus to receive notifications
const modalBus = useEventBus<ModalNotification>("modal-notification");
const toastBus = useEventBus<ModalNotification>("toast-notification");

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
const isFormValid = computed(() =>
  validTabs.every((entry: any) => entry.valid)
);

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

const goToStep = (index: number) => (currentTab.value = index);

const stateIcon = (index: number) => {
  if (currentTab.value == index) return "current";
  if (currentTab.value > index || validTabs[index].valid) return "complete";
  return "queued";
};

const progressData = computed(() =>
  currentSlots.map((aSlot: any) => {
    return {
      title: aSlot.props?.title,
      subtitle: `Step ${aSlot.props?.index + 1}`,
      kind: stateIcon(aSlot.props?.index),
    };
  })
);

//Toast opening and closing
const toastContent = ref<ModalNotification>({ active: false,name:"",kind:"",handler:() =>{}});

const openToast = (event: ModalNotification) => {
  toastContent.value = event;
  setTimeout(() => (toastContent.value.active = false), 8000);
};

toastBus.on(openToast);

//Modal opening and closing
const modalContent = ref<ModalNotification>({ active: false,name:"",kind:"",handler:() =>{}});

const openModal = (event: ModalNotification) => (modalContent.value = event);

const closeModal = () => (modalContent.value = { ...modalContent.value, active: false });

const deleteContentModal = () => {
  openToast({
    name: modalContent.value.name,
    kind: modalContent.value.kind,
    active: true,
    handler: () =>{}
  });
  modalContent.value.handler();
  closeModal();
};

modalBus.on(openModal);

</script>

<style scoped>
.form-progress {
  margin-bottom: 64px;
  display: flex;
  justify-content: space-between;
}
.form-progress-step {
  flex-grow: 1;
}

.form-progress-step > * {
  overflow: initial;
}
</style>
