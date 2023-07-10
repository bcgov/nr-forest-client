<template>
  <div class="wizard-wrap">
    <div class="wizard-head">
      <div class="wizard-head-text">
        <label class="bx--title">{{ title }}</label>
        <p class="bx--description" v-if="subtitle">{{ subtitle }}</p>
      </div>

      <bx-toast-notification
        v-if="toastContent.active"
        class="wizard-head-toast"
        timeout="8000"
        kind="success"
        :title="toastContent.kind"
        :subtitle="toastContent.message"
      >
      </bx-toast-notification>
    </div>
    <wizard-progress-indicator-component
      :model-value="progressData"
      @go-to="goToStep"
    />

    <display-block-component
      v-if="globalErrorMessage"
      kind="error"
      title="There was an error submitting your application."
      :subtitle="globalErrorMessage"
    >
    </display-block-component>
  </div>

  <div class="wizard-body wizard-mid-content">
    <slot :processValidity="processValidity" :goToStep="goToStep" />
  </div>
  <div class="wizard-footer wizard-mid-content">
    <div class="wizard-wrap" v-if="isLast && !endAndLogOut && !mailAndLogOut">
      <hr />

      <div>
        <bx-btn
          data-test="wizard-back-button"
          kind="secondary"
          iconLayout=""
          class="bx--btn"
          @click.prevent="onBack"
        >
          <span>Back</span>
        </bx-btn>

        <bx-btn
          data-test="wizard-submit-button"
          kind="primary"
          iconLayout=""
          class="bx--btn"
          @click.prevent="submit"
        >
          <span>Submit</span>
        </bx-btn>
      </div>
    </div>

    <div
      class="wizard-wrap"
      v-if="!isLast && !isFormValid && !endAndLogOut && !mailAndLogOut"
    >
      <hr />

      <span class="bx--description" v-if="!isStateValid(currentTab)"
        >All fields must be filled out correctly to enable the "Next" button
        below</span
      >
      <div>
        <bx-btn
          data-test="wizard-back-button"
          v-show="!isFirst"
          kind="secondary"
          iconLayout=""
          class="bx--btn"
          :disabled="isFirst"
          @click.prevent="onBack"
        >
          <span>Back</span>
        </bx-btn>

        <bx-btn
          data-test="wizard-next-button"
          kind="primary"
          iconLayout=""
          class="bx--btn"
          :disabled="isNextAvailable"
          v-show="!isLast"
          @click.prevent="onNext"
        >
          <span>Next</span>
          <ArrowRight16 slot="icon" />
        </bx-btn>
      </div>
    </div>

    <div
      class="wizard-wrap"
      v-if="!isLast && isFormValid && !endAndLogOut && !mailAndLogOut"
    >
      <hr />
      <div>
        <bx-btn
          data-test="wizard-save-button"
          kind="primary"
          iconLayout=""
          class="bx--btn"
          :disabled="isNextAvailable"
          v-show="!isLast"
          @click.prevent="saveChange"
        >
          <span>Save</span>
          <Save16 slot="icon" />
        </bx-btn>
      </div>
    </div>

    <div class="wizard-wrap" v-if="endAndLogOut || mailAndLogOut">
      <div>
        <bx-btn
          data-test="wizard-logout-button"
          kind="primary"
          iconLayout=""
          class="bx--btn"
          v-show="!isLast"
          @click.prevent="processAndLogOut"
        >
          <span
            >{{ endAndLogOut ? 'End application' : 'Receive email' }} and
            logout</span
          >
          <LogOut16 slot="icon" />
        </bx-btn>
      </div>
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
import { ref, computed, useSlots, reactive, provide } from 'vue'
import { useEventBus } from '@vueuse/core'
import ArrowRight16 from '@carbon/icons-vue/es/arrow--right/16'
import Save16 from '@carbon/icons-vue/es/save/16'
import LogOut16 from '@carbon/icons-vue/es/logout/16'

import type { ModalNotification } from '@/dto/CommonTypesDto'

const props = defineProps<{
  title: string
  subtitle: string
  submit: () => void
  mail: () => void
  end: () => void
}>()

//Defining the event bus to receive notifications
const modalBus = useEventBus<ModalNotification>('modal-notification')
const toastBus = useEventBus<ModalNotification>('toast-notification')
const exitBus = useEventBus<Record<string, boolean | null>>('exit-notification')
const generalErrorBus = useEventBus<string>('general-error-notification')

//Start from the first tab of the wizard
const currentTab = ref(0)

//Slot control to handle data from active slot
const slots = useSlots()
const currentSlots = slots.default ? slots.default() : []

//Grab a list of tab status
const validTabs = reactive(
  currentSlots.map((tab) => {
    return { index: tab.props?.index, valid: false }
  })
)

//Provide the value of the current index to be consumed
//by anyone who want to consume it below
provide('currentIndex', currentTab)

//Validation and handling

//Check if current state is valid or not
const isStateValid = (index: number) => {
  const currentState = validTabs.find((state) => state.index === index)
  return currentState ? currentState.valid : false
}

const processValidity = (tabStatus: { index: number; valid: boolean }) =>
  (validTabs[tabStatus.index] = tabStatus)

//Wizard State Management
const isFirst = computed(() => currentTab.value === 0)
const isLast = computed(() => currentTab.value === validTabs.length - 1)
const isCurrentValid = computed(() => isStateValid(currentTab.value))
const isNextAvailable = computed(() => !isCurrentValid.value || isLast.value)
const isFormValid = computed(() => validTabs.every((entry: any) => entry.valid))
const currentTabName = computed(
  () => currentSlots[currentTab.value].props?.title
)

const ddd = ref<boolean>(false)

//Button Actions
const onBack = () => {
  if (currentTab.value - 1 >= 0) {
    currentTab.value--
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

const onNext = () => {
  if (currentTab.value + 1 < currentSlots.length) {
    currentTab.value++
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

const goToStep = (index: number) => (currentTab.value = index)

const stateIcon = (index: number) => {
  if (currentTab.value == index) return 'current'
  if (currentTab.value > index || validTabs[index].valid) return 'complete'
  return 'queued'
}

const progressData = computed(() =>
  currentSlots.map((aSlot: any) => {
    return {
      title: aSlot.props?.title,
      subtitle: `Step ${aSlot.props?.index + 1}`,
      kind: stateIcon(aSlot.props?.index),
      enabled: true
    }
  })
)

//Toast opening and closing
const toastContent = ref<ModalNotification>({
  active: false,
  message: '',
  kind: '',
  handler: () => {}
})

const openToast = (event: ModalNotification) => {
  toastContent.value = event
  setTimeout(() => (toastContent.value.active = false), 8000)
}

toastBus.on(openToast)

//Modal opening and closing
const modalContent = ref<ModalNotification>({
  active: false,
  message: '',
  kind: '',
  handler: () => {}
})

const openModal = (event: ModalNotification) => (modalContent.value = event)

const closeModal = () =>
  (modalContent.value = { ...modalContent.value, active: false })

const deleteContentModal = () => {
  openToast({
    message: `“${modalContent.value.message}” additional ${modalContent.value.kind} was deleted successfully`,
    kind: 'Success',
    active: true,
    handler: () => {}
  })
  modalContent.value.handler()
  closeModal()
}

modalBus.on(openModal)

const saveChange = () => {
  openToast({
    message: `“${currentTabName.value}” changes was saved successfully.`,
    kind: 'Success',
    active: true,
    handler: () => {}
  })
  goToStep(3)
}

const endAndLogOut = ref<boolean>(false)
const mailAndLogOut = ref<boolean>(false)

exitBus.on((event: Record<string, boolean | null>) => {
  endAndLogOut.value = event.goodStanding ? event.goodStanding : false
  mailAndLogOut.value = event.duplicated ? event.duplicated : false
})

const processAndLogOut = () => {
  if (mailAndLogOut.value) {
    props.mail()
  }
  props.end()
}

const globalErrorMessage = ref<string>('')
generalErrorBus.on((event: string) => (globalErrorMessage.value = event))
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
