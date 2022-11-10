<template>
  <div style="margin: 24px">
    <b-spinner
      :style="'color:' + primary + '; margin-bottom: 12px'"
      v-if="loading"
    ></b-spinner>
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
      @click="openModal()"
    >
      Submit
    </b-button>

    <ConfirmModal
      :value="modalShow"
      okayText="Submit"
      @onOkay="onModalOkay()"
      @onCancel="onModalCancel()"
    />
  </div>
</template>

<script setup lang="ts">
// composition api
import { ref } from "vue";
import FormInput from "../../common/FormInput.vue";
import SubmitFailText from "./SubmitFailText.vue";
import SubmitSucessText from "./SubmitSucessText.vue";
import ConfirmModal from "../../common/ConfirmModal.vue";
import { sendConfirmationEmail } from "../../services/forestClient.service";
import type { FormFieldTemplateType } from "../../core/AppType";
import { primary } from "../../utils/color";

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

const modalShow = ref(false);
const loading = ref(false);
const openModal = () => {
  modalShow.value = true;
};
const onModalOkay = () => {
  modalShow.value = false;
  loading.value = true;
  sendConfirmationEmail(emailValue.value, "Hello World!").then((response) => {
    if (response.status == 200) {
      if (error) error.value = false;
      success.value = true;
      loading.value = false;
    } else {
      if (success) success.value = false;
      error.value = true;
      loading.value = false;
    }
  });
};
const onModalCancel = () => {
  modalShow.value = false;
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "ApplyNewClientPage",
});
</script>

<style scoped></style>
