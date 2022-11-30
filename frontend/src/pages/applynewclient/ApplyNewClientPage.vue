<template>
  <div style="margin: 24px">
    <b-spinner
      :style="'color:' + primary + '; margin-bottom: 12px'"
      v-if="loading"
    ></b-spinner>
    <SubmitSucessText v-if="success" confirmationId="A123456" />
    <SubmitFailText v-if="error" />

    <BeginSection
      :data="data.begin"
      @updateFormValue="
        (id, newValue) => updateFormValue('begin', id, newValue)
      "
    />
    <AddAuthorizedSection
      :data="data.authorized"
      @updateFormValue="
        (id, newValue) => updateFormValue('authorized', id, newValue)
      "
      @updateFormTable="
        (id, newValue, row) =>
          updateFormTable('authorized', 'individuals', id, newValue, row)
      "
      @addTableRow="addTableRow('authorized', 'individuals')"
      @deleteTableRow="
        (row) => deleteTableRow('authorized', 'individuals', row)
      "
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
import AddAuthorizedSection from "./AddAuthorizedSection.vue";
import BeginSection from "./BeginSection.vue";
import SubmitFailText from "./SubmitFailText.vue";
import SubmitSucessText from "./SubmitSucessText.vue";
import ConfirmModal from "../../common/ConfirmModal.vue";
import PrimarySquareButton from "../../common/buttons/PrimarySquareButton.vue";
import { sendConfirmationEmail } from "../../services/forestClient.service";
import { newClientData } from "./NewClient";
import type { FormFieldTemplateType } from "../../core/AppType";
import { primary } from "../../utils/color";

const data = ref(JSON.parse(JSON.stringify(newClientData)));

const updateFormValue = (containerId, fieldId, value) => {
  console.log("containerId", containerId, "fieldId", fieldId, "value", value);
  data.value[containerId][fieldId] = value;
};

const countIndex = ref(0); // must use this to generate unique index
const updateFormTable = (containerId, fieldId, columnId, value, row) => {
  data.value[containerId][fieldId][row][columnId] = value;
};
const addTableRow = (containerId, fieldId) => {
  countIndex.value += 1;
  const defaultNew = JSON.parse(
    JSON.stringify(newClientData[containerId][fieldId][0])
  );
  data.value[containerId][fieldId].push({
    ...defaultNew,
    index: countIndex.value,
  });
};
const deleteTableRow = (containerId, fieldId, row) => {
  data.value[containerId][fieldId].splice(row, 1);
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
