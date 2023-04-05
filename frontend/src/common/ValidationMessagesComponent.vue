<template>
  <div v-if="validationMessages != null" class="err-msg">
    <span v-if="errorMsg">
      <span v-html="errorMsg" />
    </span>
  </div>
</template>
  
<script setup lang="ts">
  import BiAsterisk from "~icons/bi/asterisk";
  import BiQuestionCircleFill from "~icons/bi/question-circle-fill";
  
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
    fieldValue: {
      type: Object, 
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

  watch(() => props.fieldValue, (currentState, prevState) => {
    if (currentState !== prevState) {
      errorMsg = computed(() => { return "" });
    }
  });
</script>

<script lang="ts">
  import { computed, defineComponent, ref, watch } from "vue";
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