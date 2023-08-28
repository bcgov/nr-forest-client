<script setup lang="ts">
import { ref } from 'vue'
// Composables
import { useEventBus } from '@vueuse/core'
// Imported Types
import type { ModalNotification } from '@/dto/CommonTypesDto'
// @ts-ignore
import Delete16 from '@carbon/icons-vue/es/trash-can/16'

const modalBus = useEventBus<ModalNotification>('modal-notification')
const toastBus = useEventBus<ModalNotification>('toast-notification')

//Modal opening and closing
const modalContent = ref<ModalNotification>({
  active: false,
  message: '',
  kind: '',
  toastTitle: '',
  handler: () => {}
})

const toastContent = ref<ModalNotification>({
  active: false,
  message: '',
  kind: '',
  toastTitle: '',
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
    toastTitle: modalContent.value.toastTitle,
    active: true,
    handler: () => {}
  })
  modalContent.value.handler()
  closeModal()
}

modalBus.on(openModal)
toastBus.on(openToast)
</script>

<template>
  <div class="headers" v-if="$session?.isLoggedIn()">
    <main-header-component v-if="!$route.meta.hideHeader"></main-header-component>
  </div>
  <div :class="{[`${$route.meta.format}`]:true}">
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
      <bx-modal-heading
        >Are you sure you want to delete "{{ modalContent.message }}" additional
        {{ modalContent.kind }}?</bx-modal-heading
      >
    </bx-modal-header>
    <bx-modal-body><p></p></bx-modal-body>

    <bx-modal-footer>
      <div class="modal-footer">
        <bx-btn kind="secondary" 
              data-modal-close
              class="bx--modal-btn bx--modal-btn1">
        Cancel
      </bx-btn>
      
      <bx-btn kind="danger" 
              @click.prevent="deleteContentModal"
              class="bx--modal-btn bx--modal-btn2">
        Delete
        <Delete16 slot="icon" />
      </bx-btn>
      </div>
    </bx-modal-footer>
  </bx-modal>
</div>

  <bx-toast-notification
      v-if="toastContent.active"
      class="wizard-head-toast"
      timeout="8000"
      kind="success"
      :title="toastContent.toastTitle"
      subtitle=""
    >
  </bx-toast-notification>
</template>

