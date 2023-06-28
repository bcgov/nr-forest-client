<template>
  <div class="wizard-wrap-indicator">
    <div
      v-for="(step, index) in values"
      :class="{
        'wizard-wrap-item': true,
        [`wizard-wrap-item-${step.kind}`]: true
      }"
    >
      <component
        :is="iconsForKinds[step.kind]"
        :alt="step.kind"
        :class="{
          'wizard-wrap-item-icon': true,
          [`wizard-wrap-item-icon-${step.kind}`]: true
        }"
      />
      <div class="wizard-wrap-item-text">
        <p>
          <a
            v-if="canShowLink(step)"
            :href="'#' + index"
            @click.prevent="emit('go-to', index)"
            >{{ step.title }}</a
          >
          <span
            v-else
            :class="{
              'inner-text': true,
              'inner-text-disabled': !step.enabled
            }"
            >{{ step.title }}</span
          >
        </p>
        <span
          :class="{
            'inner-text': true,
            'inner-text-disabled': !step.enabled
          }"
          >{{ step.subtitle }}</span
        >
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { toRef, watch } from 'vue'
import { useEventBus } from '@vueuse/core'
import type { ProgressData } from '@/core/CommonTypes'

import Checkmark16 from '@carbon/icons-vue/es/checkmark--outline/16'
import Error16 from '@carbon/icons-vue/es/error--outline/16'
import Circle16 from '@carbon/icons-vue/es/circle-dash/16'
import Info16 from '@carbon/icons-vue/es/incomplete/16'

const props = defineProps<{ modelValue: Array<ProgressData> }>()

const emit = defineEmits<{ (e: 'go-to', value: number): void }>()

const bus = useEventBus<boolean>('navigation-notification')

const values = toRef(props.modelValue)
watch(
  () => props.modelValue,
  () => {
    values.value = props.modelValue
  }
)
bus.on(
  (value: boolean) =>
    (values.value = values.value.map((item: ProgressData) => {
      if (item.kind === 'queued') {
        item.enabled = value
      }
      return item
    }))
)

const iconsForKinds: Record<string, any> = {
  ['complete']: Checkmark16,
  ['current']: Info16,
  ['queued']: Circle16,
  ['error']: Error16
}

const canShowLink = (step: ProgressData) => {
  return step.kind === 'current' || step.kind === 'complete'
}
</script>

<style scoped>
.wizard-wrap-indicator {
  display: flex;
  justify-content: space-between;
  margin-bottom: 48px;
}
.wizard-wrap-item {
  flex-grow: 1;
  display: flex;
  justify-content: space-between;
  border-top: solid 2px;
}

.wizard-wrap-item-icon {
  margin: 4px 10px 0px 0px;
}

.wizard-wrap-item-text {
  flex-grow: 1;
}
.wizard-wrap-item-text p {
  font-size: 14px;
}
.wizard-wrap-item-text p a {
  text-decoration: none;
  color: inherit;
}
.wizard-wrap-item-text p a:hover {
  text-decoration: underline;
  cursor: pointer;
}
.wizard-wrap-item-text span {
  font-size: 12px;
}

.wizard-wrap-item-current {
  border-color: #0073e6;
}

.wizard-wrap-item-queued {
  border-color: #dfdfe1;
}

.wizard-wrap-item-error {
  border-color: #e72000;
}

.wizard-wrap-item-complete {
  border-color: #0073e6;
}

.wizard-wrap-item-icon-current {
  color: #0073e6;
}

.wizard-wrap-item-icon-queued {
  color: #dfdfe1;
}

.wizard-wrap-item-icon-error {
  color: #e72000;
}

.wizard-wrap-item-icon-complete {
  color: #0073e6;
}
</style>
