<template>
  <div v-if="validationMessages != null" class="err-msg">
    <span v-if="errorMsg">
      <span v-html="errorMsg" />
    </span>
  </div>
</template>
  
<script setup lang="ts">
  const props = defineProps({
    fieldId: { 
      type: String, 
      required: true 
    },
    validationMessages: { 
      type: Array<ValidationMessageType>, 
      required: true,
      default: null 
    },
    modelValue : { 
      type: null,
      required: true
    }
  });

  let errorMsg = computed(() => {
    return (Array.isArray(props.validationMessages)) ? 
                      props.validationMessages
                                .filter(p => p.fieldId == props.fieldId)
                                .map(p => p.errorMsg)
                                .join('<br>') :
                      "";
  });

  watch(() => props.modelValue, (newVal, oldVal) => {
    if (JSON.stringify(newVal) !== JSON.stringify(oldVal)) {
      props.validationMessages.forEach((item, index) => {
        if (item['fieldId'] === props.fieldId) {
          props.validationMessages.splice(index, 1);
        }
      });
    }
  });
</script>

<script lang="ts">
  import { computed, defineComponent, watch } from "vue";
  import type { ValidationMessageType } from "@/core/CommonTypes";
  export default defineComponent({
    name: "ValidationMessageComponent",
  });
</script>
  
<style scoped>
.err-msg {
  color: #de4b50;
  font-size: 12px;
  margin-top: 2px;
}
</style>  