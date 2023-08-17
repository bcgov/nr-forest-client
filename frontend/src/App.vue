<template>
  <div class="headers">
    <main-header-component v-if="!$route.meta.hideHeader"></main-header-component>
  </div>
  <div class="screen">
    <div class="content">
    <router-view></router-view>
    </div>
  </div>
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
