<template>
  <div v-show="index == selectedContent">
    
    <div class="wizard-wrap">
      <div><slot name="pre-header"></slot></div>

      <div>
        <h4 class="form-header">{{title}}</h4>  
        <p class="inner-text" v-if="subTitle">{{subTitle}}</p>
      </div>
      <div><slot name="header"></slot></div>
    </div>

    <div class="wizard-wrap">
    <slot :validateStep="validateStep" :active="index == selectedContent" />
    </div>
  </div>
</template>
<script setup lang="ts">
import { inject } from "vue";

const props = defineProps<{
  title: string;
  subTitle?: string;
  index: number;
  valid: boolean;
  wizard: any;
}>();

const emit = defineEmits<{
  (e: "update:valid", value: { index: number; valid: boolean }): void;
}>();

const selectedContent = inject<number>("currentIndex");

const validateStep = (valid: boolean) => {
  emit("update:valid", { index: props.index, valid });
  props.wizard.processValidity({ index: props.index, valid });
};

emit("update:valid", { index: props.index, valid: false });
</script>

<style scoped>


</style>