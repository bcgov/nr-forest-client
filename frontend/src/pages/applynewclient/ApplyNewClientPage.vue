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
import FormSections from "./formsections/FormSections.vue";
import SubmitFailText from "./SubmitFailText.vue";
import SubmitSucessText from "./SubmitSucessText.vue";
import ConfirmModal from "../../common/ConfirmModal.vue";
import PrimarySquareButton from "../../common/buttons/PrimarySquareButton.vue";
import { sendConfirmationEmail } from "../../services/forestClient.service";
import {
  newClientData,
  commonRequiredFields,
  businessRequiredFields,
  individualRequiredFields,
} from "./NewClient";

const data = ref(JSON.parse(JSON.stringify(newClientData)));

/* --------------- update form value functions --------------------- */
const updateFormValue = (containerId, fieldId, value) => {
  console.log("containerId", containerId, "fieldId", fieldId, "value", value);
  data.value[containerId][fieldId] = value;
  console.log("data", data.value);
  // todo: this is where to check if each field meets its validation rules
};
const updateFormArrayValue = (
  containerId,
  fieldId,
  columnId,
  value,
  rowIndex
) => {
  data.value[containerId][fieldId][rowIndex][columnId] = value;
};
const addRow = (containerId, fieldId) => {
  const defaultNew = JSON.parse(
    JSON.stringify(newClientData[containerId][fieldId][0])
  );
  data.value[containerId][fieldId].push({
    ...defaultNew,
    index: Math.floor(Math.random() * 10000000),
  });
};
const deleteRow = (containerId, fieldId, rowIndex) => {
  data.value[containerId][fieldId].splice(rowIndex, 1);
};

/* -------------- check when to enable submit button ------------------- */
const checkMissingRequireField = (requireList, formData) => {
  let missingRequire = false;
  for (let i = 0; i < requireList.length; i++) {
    const require = requireList[i];
    if (!require.columnId) {
      if (formData[require.containerId][require.fieldId] == "") {
        missingRequire = true;
        break;
      }
    } else {
      // check table and group, to see if each row got all required fields
      for (
        let j = 0;
        j < formData[require.containerId][require.fieldId].length;
        j++
      ) {
        const row = formData[require.containerId][require.fieldId][j];
        if (row[require.columnId] == "") {
          missingRequire = true;
          break;
        }
      }
    }
  }
  return missingRequire;
};
const computedButtonDisable = computed(() => {
  // check when to enable the submit button
  // enable the submit button when got all required fields
  // create required lists, and then check the data for those required ones
  if (
    data.value.begin["client_type"] == "individual" ||
    data.value.begin["client_type"] == "soleProprietorship"
  )
    return (
      checkMissingRequireField(commonRequiredFields, data.value) ||
      checkMissingRequireField(individualRequiredFields, data.value)
    );
  return (
    checkMissingRequireField(commonRequiredFields, data.value) ||
    checkMissingRequireField(businessRequiredFields, data.value)
  );
});

/* -------------------- data validation check --------------------------- */

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
  // todo: call the data validatio api and receive the result from backend and pass it to form sections
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
