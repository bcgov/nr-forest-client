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
    fieldId: String,
    validationMessages: { type: Array, default: null },
  });

  const getErrorMessage = (fieldId: string) => {
    console.log(fieldId);
    let errorMsg = props.validationMessages
                                .filter(p => p.fieldId == fieldId)
                                .map(p => p.errorMsg);
    return errorMsg.toString();
  };
</script>

<script lang="ts">
  import { defineComponent } from "vue";
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