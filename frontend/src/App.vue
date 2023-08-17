<template>
  <div class="headers">
    <main-header-component v-if="!$route.meta.hideHeader"></main-header-component>
  </div>
  <div class="screen">
    <div class="content">
    <router-view></router-view>
    </div>
  </div>

  <div class="modals">
    
  <bx-modal
    id="modal-example"
    size="sm"
    :open="modalContent.active"
    @bx-modal-closed="closeModal"
  >
    <bx-modal-header>
      <bx-modal-close-button></bx-modal-close-button>
      <bx-modal-label>Delete additional {{ modalContent.kind }}</bx-modal-label>
      <bx-modal-heading
        >Are you sure you want to delete "{{ modalContent.message }}" additional
        {{ modalContent.kind }}</bx-modal-heading
      >
    </bx-modal-header>
    <bx-modal-body><p></p></bx-modal-body>

    <bx-modal-footer>
      <bx-btn kind="secondary" 
              data-modal-close>
        Cancel
      </bx-btn>
      <span style="width: 0.5rem;"></span>
      <bx-btn kind="danger" 
              @click.prevent="deleteContentModal">
        Delete additional {{ modalContent.kind }}
      </bx-btn>
    </bx-modal-footer>
  </bx-modal>
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
</template>

<script setup lang="ts">
import { ref, computed, useSlots, reactive, provide } from 'vue'
import { useEventBus } from '@vueuse/core'
import type { ModalNotification } from '@/dto/CommonTypesDto'

const modalBus = useEventBus<ModalNotification>('modal-notification')
const toastBus = useEventBus<ModalNotification>('toast-notification')

//Modal opening and closing
const modalContent = ref<ModalNotification>({
  active: false,
  message: '',
  kind: '',
  handler: () => {}
})

const toastContent = ref<ModalNotification>({
  active: false,
  message: '',
  kind: '',
  handler: () => {}
})

const openModal = (event: ModalNotification) => (modalContent.value = event)

const closeModal = () =>
  (modalContent.value = { ...modalContent.value, active: false })

const openToast = (event: ModalNotification) => {
  toastContent.value = event
  setTimeout(() => (toastContent.value.active = false), 8000)
}

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
</script>
