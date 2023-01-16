<template>
  <div style="margin: 24px">
    <b-spinner
      :style="'color:' + primary + '; margin-bottom: 12px'"
      v-if="loading"
    ></b-spinner>
    <SubmitSucessText v-if="success" confirmationId="A123456" />
    <SubmitFailText v-if="error" />

    <FormSections />

    <PrimarySquareButton
      @click="openModal()"
      text="Submit"
      :disabled="computedButtonDisable"
    />

    <ConfirmModal
      :show="modalShow"
      okayText="Submit"
      @onOkay="onModalOkay()"
      @onCancel="onModalCancel()"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import ConfirmModal from "../../common/ConfirmModal.vue";
import PrimarySquareButton from "../../common/buttons/PrimarySquareButton.vue";
import FormSections from "./formsections/FormSections.vue";
import SubmitFailText from "./SubmitFailText.vue";
import SubmitSucessText from "./SubmitSucessText.vue";
import { checkMissingRequireField } from "../../helpers/formvalidation/MissingFieldCheck";
import {
  commonRequiredFields,
  businessRequiredFields,
  individualRequiredFields,
} from "./formvalidation/RequiredFields";
import { validationResult } from "../../helpers/AppState";
import { formData } from "../../helpers/FormState";
import { primary } from "../../utils/color";

/* -------------- check when to enable submit button ------------------- */
const computedButtonDisable = computed(() => {
  // check when to enable the submit button
  // enable the submit button when got all required fields
  // create required lists, and then check the data for those required ones
  if (
    formData.data.begin["client_type"] == "individual" ||
    formData.data.begin["client_type"] == "soleProprietorship"
  )
    return (
      checkMissingRequireField(commonRequiredFields, formData.data) ||
      checkMissingRequireField(individualRequiredFields, formData.data)
    );
  return (
    checkMissingRequireField(commonRequiredFields, formData.data) ||
    checkMissingRequireField(businessRequiredFields, formData.data)
  );
});

/* ---------- modal placehoder to confirm submit ----------- */
const success = ref(false);
const error = ref(false);
const loading = ref(false);

const modalShow = ref(false);
const openModal = () => {
  modalShow.value = true;
  console.log("formdata", formData.data);
};
const onModalOkay = () => {
  modalShow.value = false;
  // todo: call the data validatio api and receive the result from backend and pass it to form sections
  /* -------------------- data validation check --------------------------- */

  // a hardcode example to pass validationResult
  validationResult.setValue([
    { containerId: "begin", fieldId: "client_type", errorMsg: "WrongType" },
  ]);
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
