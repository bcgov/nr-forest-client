<template>
  <div style="margin: 24px">
    <FormInput
      :fieldProps="emailInputProps"
      :value="emailValue"
      @updateValue="updateEmailValue"
    />
    <b-button
      variant="primary"
      :style="'background-color:' + primary + ';margin-top: 24px'"
      @click="sendEmail()"
    >
      Submit
    </b-button>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import FormInput from "../common/FormInput.vue";
import { sendConfirmationEmail } from "../services/forestClient.service";
import type { FormFieldTemplateType } from "../core/AppType";
import { primary } from "../utils/color";

// composition api
const emailInputProps: FormFieldTemplateType = {
  label: "Email Address",
  id: "test-email",
};

const emailValue = ref("");

const updateEmailValue = (id: string, newValue: string) => {
  emailValue.value = newValue;
};

const sendEmail = () => {
  sendConfirmationEmail(emailValue.value, "Hello World!");
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "ApplyNewClientPage",
});
</script>

<style scoped></style>
