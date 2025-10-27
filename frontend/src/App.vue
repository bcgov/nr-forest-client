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
import UnauthorizedErrorPage from '@/pages/UnauthorizedErrorPage.vue';

const modalBus = useEventBus<ModalNotification>('modal-notification')
const toastBus = useEventBus<ModalNotification>('toast-notification')
const overlayBus = useEventBus<boolean>('overlay-event')

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

const overlayContent = ref<{
  isVisible: boolean;
  message: string;
  showLoading: boolean;
}>({
  isVisible: false,
  message: '',
  showLoading: false
})

const openModal = (event: ModalNotification) => (modalContent.value = event)

const closeModal = () =>
  (modalContent.value = { ...modalContent.value, active: false })

const openToast = (event: ModalNotification) => {
  toastContent.value = event
  setTimeout(() => (toastContent.value.active = false), 8000)
}

const openOverlay = (event: {
  isVisible: boolean;
  message: string;
  showLoading: boolean;
}) => (overlayContent.value = event)

const deleteContentModal = () => {
  openToast({
    message: modalContent.value.message,
    kind: "Success",
    toastTitle: "Success",
    active: true,
    handler: () => {}
  })
  modalContent.value.handler()
  closeModal()
}

modalBus.on(openModal)
toastBus.on(openToast)
overlayBus.on(openOverlay)
</script>

<template>
  <div :class="$route.meta.headersStyle" v-if="$session?.isLoggedIn()">
    <main-header-component v-if="!$route.meta.hideHeader"></main-header-component>
  </div>
  <div :class="$route.meta.format">
    <div :class="$route.meta.style" aria-live="polite">
      <unauthorized-error-page v-if="$route.meta.showUnauthorized" />
      <router-view v-else v-slot="{ Component }">
        <keep-alive include="SearchPage">
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </div>
  </div>

  <div class="modals">
    
    <cds-modal
      id="modal-global"
      aria-labelledby="modal-global-heading"
      size="sm"
      :open="modalContent.active"
      @cds-modal-closed="closeModal"
    >
      <cds-modal-header>
        <cds-modal-close-button></cds-modal-close-button>
        <cds-modal-heading id="modal-global-heading"
          >Are you sure you want to delete "{{ modalContent.name }}" additional
          {{ modalContent.kind }}?
        </cds-modal-heading>
      </cds-modal-header>
      

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
          v-on:click="deleteContentModal"
          :danger-descriptor="`Delete &quot;${modalContent.name}&quot;`"
        >
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
      :kind="toastContent.kind.toLowerCase()"
      :title="toastContent.toastTitle"
    >
    <div slot="subtitle" v-dompurify-html="toastContent.message"></div>
  </cds-toast-notification>

  <loading-overlay-component 
      :is-visible="overlayContent.isVisible"
      :message="overlayContent.message"
      :show-loading="overlayContent.showLoading"
    />
</template>
