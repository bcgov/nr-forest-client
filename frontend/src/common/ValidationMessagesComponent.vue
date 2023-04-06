<template>
  <div v-if="validationMessages != null" class="err-msg">
    <span v-if="getErrorMessage(fieldId).length > 0">
      {{ getErrorMessage(fieldId) }}
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
  });

  const getErrorMessage = (fieldId: string) => {
    let errorMsg = (Array.isArray(props.validationMessages)) ? 
                      props.validationMessages
                                .filter(p => p.fieldId == fieldId)
                                .map(p => p.errorMsg)
                                .toString() :
                      "";
    return errorMsg;
  };
</script>

<script lang="ts">
  import { defineComponent } from "vue";
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