import { reactive, ref } from "vue";
import _ from "lodash";
import type { FormDisableType } from "../../core/FormType";

export const disabledFields = {
  state: reactive({
    businessType: [],
    information: [],
    location: [],
  } as FormDisableType), // has to initialize them, and update each key-value later, so it can detect the object change
  getters: {
    getDisabledFields() {
      return disabledFields.state;
    },
  },
  mutations: {
    setDisabledFields(fieldsToBeDisabled: FormDisableType) {
      // need to update for each one, otherwise won't detect whole object reassign
      disabledFields.state.businessType = fieldsToBeDisabled.businessType || [];
      disabledFields.state.information = fieldsToBeDisabled.information || [];
      disabledFields.state.location = fieldsToBeDisabled.location || [];
    },
    setDisabledFieldsForSection(
      containerId: string,
      fieldsToBeDisabled: Array<string>
    ) {
      disabledFields.state[containerId] = fieldsToBeDisabled;
    },
  },
  actions: {
    setDisabledFields(fieldsToBeDisabled: FormDisableType) {
      disabledFields.mutations.setDisabledFields(fieldsToBeDisabled);
    },
    setDisabledFieldsForSection(
      containerId: string,
      fieldsToBeDisabled: Array<string>
    ) {
      disabledFields.mutations.setDisabledFieldsForSection(
        containerId,
        fieldsToBeDisabled
      );
    },
    removeDisabledFileds(containerId: string, path: string) {
      if (
        disabledFields.state[containerId] &&
        disabledFields.state[containerId].length > 0
      ) {
        disabledFields.mutations.setDisabledFieldsForSection(
          containerId,
          disabledFields.state[containerId].filter(
            (eachRow) => eachRow !== path
          )
        );
      }
    },
  },
};

export const disableAllFields = {
  state: ref(false),
  mutations: {
    enableEdit() {
      disableAllFields.state.value = true;
    },
    disableEdit() {
      disableAllFields.state.value = false;
    },
  },
  actions: {
    enableEdit() {
      disableAllFields.mutations.enableEdit();
    },
    disableEdit() {
      disableAllFields.mutations.disableEdit();
    },
  },
};
