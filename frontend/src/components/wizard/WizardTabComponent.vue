<template>
  <div v-if="index == selectedContent">
    <div class="wizard-wrap">
      <slot name="pre-header"></slot>
      <div>
        <h4 class="form-header">{{ title }}</h4>
        <p class="inner-text" v-if="subTitle">{{ subTitle }}</p>
      </div>
      <slot name="header"></slot>
    </div>

    <div class="wizard-wrap">
      <slot
        :validateStep="validateStep"
        :active="index == selectedContent"
        :goToStep="goToStep"
      ></slot>
    </div>
  </div>
</template>
<script setup lang="ts">
import { inject } from 'vue'

const props = defineProps<{
  title: string
  subTitle?: string
  index: number
  valid: boolean
  wizard: any
}>()

const emit = defineEmits<{
  /* eslint-disable typescript:S6598 */
  (e: 'update:valid', value: { index: number; valid: boolean }): void
}>()

const selectedContent = inject<number>('currentIndex')

const validateStep = (valid: boolean) => {
  emit('update:valid', { index: props.index, valid })
  props.wizard.processValidity({ index: props.index, valid })
}

emit('update:valid', { index: props.index, valid: false })

const goToStep = (index: number) => props.wizard.goToStep(index)
</script>
