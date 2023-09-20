<script setup lang="ts">
import { ref } from 'vue'
// Carbon
import '@carbon/web-components/es/components/modal/index';
import '@carbon/web-components/es/components/notification/index';
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
    <div :class="$route.meta.style">
    <router-view></router-view>
    </div>
  </div>

  <div class="modals">
    
    <cds-modal
      id="modal-global"
      size="sm"
      :open="modalContent.active"
      @cds-modal-closed="closeModal"
    >
      <cds-modal-header>
        <cds-modal-close-button></cds-modal-close-button>
        <cds-modal-heading
          >Are you sure you want to delete "{{ modalContent.message }}" additional
          {{ modalContent.kind }}?
        </cds-modal-heading>
      </cds-modal-header>
      <cds-modal-body><p></p></cds-modal-body>

      <cds-modal-footer>
          <cds-modal-footer-button 
            kind="secondary"
            data-modal-close
            class="cds--modal-close-btn">
            Cancel
          </cds-modal-footer-button>
          
          <cds-modal-footer-button 
            kind="danger"
            class="cds--modal-submit-btn"
            v-on:click="deleteContentModal">
            Delete
            <Delete16 slot="icon" />
          </cds-modal-footer-button>

        </cds-modal-footer>
    </cds-modal>
  </div>

  <cds-toast-notification
      v-if="toastContent.active"
      class="wizard-head-toast"
      timeout="8000"
      kind="success"
      :title="toastContent.toastTitle"
      subtitle=""
      low-contrast="false"
    >
  </cds-toast-notification>
</template>

