<template>
  <div class="bx--progress-indicator">
    <div
      v-for="(step, index) in values"
      :class="{
        'bx--progress-step': true,
        [`bx--progress-step-${step.kind}`]: true
      }"
      :data-test="'step-' + index"
    >
      <component
        name="icon"
        :is="iconsForKinds[step.kind]"
        :alt="step.kind"
        :class="{
          'bx--progress-step-icon': true,
          [`bx--progress-step-icon-${step.kind}`]: true
        }"
      />
      <div class="bx--progress-step-text">
        <p>
          <a
            v-if="canShowLink(step)"
            @click.prevent="emit('go-to', index)"
            rel="noopener noreferrer"
            :class="{
              'body-compact-01': true,
              'body-compact-01-active': step.kind === 'current'
            }"
            >{{ step.title }}</a
          >
          <span
            v-else
            :class="{
              'body-compact-01-active': step.kind === 'current',
              'body-compact-01': step.kind !== 'current',

            }"
            >{{ step.title }}</span
          >
        </p>
        <span
        class="label-01"
          >{{ step.subtitle }}</span
        >
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { toRef, watch } from 'vue'
import { useEventBus } from '@vueuse/core'
import type { ProgressData } from '@/dto/CommonTypesDto'

import Checkmark16 from '@carbon/icons-vue/es/checkmark--outline/16'
import Error16 from '@carbon/icons-vue/es/error--outline/16'
import Circle16 from '@carbon/icons-vue/es/circle-dash/16'
import Info16 from '@carbon/icons-vue/es/incomplete/16'

const props = defineProps<{ modelValue: Array<ProgressData> }>()

/* eslint-disable typescript:S6598 */
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