<template>
  <div style="margin: 24px">
    <b-spinner
      :style="'color:' + primary + '; margin-bottom: 12px'"
      v-if="loading"
    ></b-spinner>
    <SubmitSucessText v-if="success" confirmationId="A123456" />
    <SubmitFailText v-if="error" />

    <FormSections
      :data="data"
      @updateFormValue="updateFormValue"
      @updateFormArrayValue="updateFormArrayValue"
      @addRow="addRow"
      @deleteRow="deleteRow"
    />

    <PrimarySquareButton @click="openModal()" text="Submit" />

    <ConfirmModal
      :show="modalShow"
      okayText="Submit"
      @onOkay="onModalOkay()"
      @onCancel="onModalCancel()"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import FormSections from "./formsections/FormSections.vue";
import SubmitFailText from "./SubmitFailText.vue";
import SubmitSucessText from "./SubmitSucessText.vue";
import ConfirmModal from "../../common/ConfirmModal.vue";
import PrimarySquareButton from "../../common/buttons/PrimarySquareButton.vue";
import { sendConfirmationEmail } from "../../services/forestClient.service";
import { newClientData } from "./NewClient";

const data = ref(JSON.parse(JSON.stringify(newClientData)));

const updateFormValue = (containerId, fieldId, value) => {
  console.log("containerId", containerId, "fieldId", fieldId, "value", value);
  data.value[containerId][fieldId] = value;
  console.log("data", data.value);
};

const countIndex = ref({
  authorized: 0,
  contact: 0,
}); // must use this to generate unique index
const updateFormArrayValue = (
  containerId,
  fieldId,
  columnId,
  value,
  rowIndex
) => {
  data.value[containerId][fieldId][rowIndex][columnId] = value;
  console.log("data array", data.value);
};
const addRow = (containerId, fieldId) => {
  countIndex.value[containerId] += 1;
  const defaultNew = JSON.parse(
    JSON.stringify(newClientData[containerId][fieldId][0])
  );
  data.value[containerId][fieldId].push({
    ...defaultNew,
    index: countIndex.value[containerId],
  });
};
const deleteRow = (containerId, fieldId, rowIndex) => {
  data.value[containerId][fieldId].splice(rowIndex, 1);
};

/* ---------- modal placehoder to confirm submit ----------- */
const success = ref(false);
const error = ref(false);
const loading = ref(false);

const modalShow = ref(false);
const openModal = () => {
  modalShow.value = true;
};
const onModalOkay = () => {
  modalShow.value = false;
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
