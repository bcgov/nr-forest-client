<template>
  <div
    data-testid="display-block-wrapper"
    :class="{
      'display-block-wrapper': true,
      [`display-block-wrapper-${kind}`]: true
    }"
  >
    <component
      data-testid="display-block-icon"
      :is="iconsForKinds[kind]"
      :class="{
        'display-block-icon': true,
        [`display-block-icon-${kind}`]: true
      }"
      :alt="kind"
    />
    <div class="display-block-body">
      <p>
        <span>{{ title }}</span>
        {{ subtitle }}
      </p>
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  kind: string
  title: string
  subtitle?: string
}>()

import CheckmarkFilled20 from '@carbon/icons-vue/es/checkmark--filled/24'
import ErrorFilled20 from '@carbon/icons-vue/es/error--filled/24'
import WarningFilled20 from '@carbon/icons-vue/es/warning--filled/24'
import InfoFilled20 from '@carbon/icons-vue/es/information--filled/24'

const iconsForKinds: Record<string, any> = {
  ['success']: CheckmarkFilled20,
  ['info']: InfoFilled20,
  ['warning']: WarningFilled20,
  ['error']: ErrorFilled20
}
</script>

<style scoped>
.display-block-wrapper {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  padding: 15px;
  gap: 16px;
}

.display-block-wrapper p span {
  font-weight: bold;
}

.display-block-wrapper-info {
  background: #c2e0ff;
  border: 1px solid #0073E6;
  border-radius: 4px;
  border-left: 4px solid #0073E6;
}

.display-block-wrapper-success {
  background: #defbe6;
  border: 1px solid #198038;
  border-radius: 4px;
  border-left: 4px solid #198038;
}

.display-block-wrapper-error {
  background: #fff1f1;
  border: 1px solid #E72000;
  border-radius: 4px;
  border-left: 4px solid #E72000;
}

.display-block-wrapper-warning {
  background: #fdf6dd;
  border: 1px solid #f1c21b;
  border-radius: 4px;
  border-left: 4px solid #f1c21b;
}

.display-block-icon-info {
  color: #0073E6;
}
.display-block-icon-success {
  color: #198038;
}
.display-block-icon-error {
  color: #E72000;
}
.display-block-icon-warning {
  color: #f1c21b;
}
</style>
