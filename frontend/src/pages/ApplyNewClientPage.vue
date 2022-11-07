<template>
  <div style="margin: 24px">
    <SubmitSucessText v-if="success" confirmationId="A123456" />
    <SubmitFailText v-if="error" />
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
import SubmitFailText from "../containers/SubmitFailText.vue";
import SubmitSucessText from "../containers/SubmitSucessText.vue";
import { sendConfirmationEmail } from "../services/forestClient.service";
import type { FormFieldTemplateType } from "../core/AppType";
import { primary } from "../utils/color";

// composition api
const success = ref(false);
const error = ref(false);

const emailInputProps: FormFieldTemplateType = {
  label: "Email Address",
  id: "test-email",
};

const emailValue = ref("");

const updateEmailValue = (id: string, newValue: string) => {
  if (success) success.value = false;
  if (error) error.value = false;
  emailValue.value = newValue;
};

const sendEmail = () => {
  sendConfirmationEmail(emailValue.value, "Hello World!").then((response) => {
    if (response.status == 200) {
      if (error) error.value = false;
      success.value = true;
    } else {
      if (success) success.value = false;
      error.value = true;
    }
  });
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "ApplyNewClientPage",
});
</script>

<style scoped></style>
