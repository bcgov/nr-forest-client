import { reactive } from "vue";
import _ from "lodash";
import type {
  FormValidationResultType,
  FormFieldValidationResultType,
} from "../../core/FormType";

export const validationResult = {
  state: reactive({
    begin: [],
    information: [],
    location: [],
  } as FormValidationResultType), // has to initialize them, and update each key-value later, so it can detect the object change
  getters: {
    getValidationResult() {
      return validationResult.state;
    },
  },
  mutations: {
    setValidationResult(result: FormValidationResultType) {
      // need to update for each one, otherwise won't detect whole object reassign
      validationResult.state.begin = result.begin || [];
      validationResult.state.information = result.information || [];
      validationResult.state.location = result.location || [];
    },
    setValidationResultForSection(
      containerId: string,
      sectionResult: Array<FormFieldValidationResultType>
    ) {
      validationResult.state[containerId] = sectionResult;
    },
  },
  actions: {
    setValidationResult(result: FormValidationResultType) {
      validationResult.mutations.setValidationResult(result);
    },
    removeValidationError(containerId: string, path: string) {
      if (
        validationResult.state[containerId] &&
        validationResult.state[containerId].length > 0
      ) {
        validationResult.mutations.setValidationResultForSection(
          containerId,
          validationResult.state[containerId].filter(
            (eachRow) => eachRow.path !== path
          )
        );
      }
    },
  },
};
