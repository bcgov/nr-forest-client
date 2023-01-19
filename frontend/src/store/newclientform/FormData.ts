import { reactive } from "vue";
import _ from "lodash";
import { validationResult } from "./FormValidation";
import { disabledFields } from "./FormDisable";
import type { CommonObjectType } from "../../core/FormType";

// global state
// the change of a global state can be detected by all the components are using the value

const initialFormData = {
  begin: {
    reason: [""],
    tenure_type: [""],
    client_type: "",
  },
  information: {
    first_name: "",
    last_name: "",
    birthdate: "",
    registration_number: "",
    doing_business_as_check: false,
    doing_business_as: "",
    business_name: "",
    worksafebc_number: "",
  },
  contact: {
    address: [
      {
        street_address: [{ address_line: "", index: 0 }],
        country: "",
        province: "",
        city: "",
        postal_code: "",
        index: 0, // any array data need to have this index, as an auto generated random number to be as unique identity
      },
    ],
  },
  authorized: {
    individuals: [
      {
        contact_type: "",
        firsname_or_company: "",
        last_name: "",
        phone: "",
        email: "",
        index: 0, // need use this index to be unique identity when display data in form tables
      },
    ],
  },
};

export const formData = {
  // Create a state with the reactive function, this will manage the reactivity for us
  state: reactive(
    // use json parse to get copy of value not reference
    JSON.parse(JSON.stringify(initialFormData)) as {
      [key: string]: CommonObjectType;
    }
  ),
  // This section will handle the getters
  getters: {
    getFormData() {
      return formData.state;
    },
    getFormDataByPath(path: string) {
      return _.get(formData.state, path);
    },
  },
  // This section will manage the changes into the state, can be only called by actions
  mutations: {
    updateFormValue(newValue: any, dataPath: string) {
      _.set(formData.state, dataPath, newValue);
    },
    addRow(newRow: CommonObjectType, dataPath: string) {
      const arrayValue = _.get(formData.state, dataPath);
      arrayValue.push({
        ...newRow,
        index: Math.floor(Math.random() * 10000000),
      });
      _.set(formData.state, dataPath, arrayValue);
    },
    deleteRow(rowIndex: number, dataPath: string) {
      const arrayValue = _.get(formData.state, dataPath);
      arrayValue.splice(rowIndex, 1);
      _.set(formData.state, dataPath, arrayValue);
    },
  },
  // This section will manage the actions needed for our store
  actions: {
    updateFormValue(newValue: any, dataPath: string) {
      formData.mutations.updateFormValue(newValue, dataPath);
      formData.actions.cleanErrorMsg();
      // an example to disable a field
      if (
        dataPath == "begin.client_type" &&
        formData.getters.getFormDataByPath(dataPath) == "individual"
      ) {
        disabledFields.actions.setDisabledFieldsForSection("information", [
          "information.last_name",
        ]);
      }
    },
    addRow(dataPath: string) {
      const defaultNew = JSON.parse(
        JSON.stringify(_.get(initialFormData, dataPath)[0])
      );
      formData.mutations.addRow(defaultNew, dataPath);
    },
    deleteRow(rowIndex: number, dataPath: string) {
      formData.mutations.deleteRow(rowIndex, dataPath);
    },
    cleanErrorMsg() {
      // todo: this is the function to check every feild if meet the validation rules, remove the error message if met
      // some hard code examples to remove validationError after user makes the correctness
      if (!_.isEmpty(validationResult)) {
        if (formData.state.begin.client_type == "individual")
          validationResult.actions.removeValidationError(
            "begin",
            "begin.client_type"
          );
        if (formData.state.information.first_name != "")
          validationResult.actions.removeValidationError(
            "information",
            "information.first_name"
          );
        // an example to check all array data for form group
        formData.state.contact.address.forEach(
          (e: CommonObjectType, index: number) => {
            if (e.country == "Canada") {
              validationResult.actions.removeValidationError(
                "contact",
                `contact.address.${index}.country`
              );
            }
            e.street_address.forEach(
              (addressLine: CommonObjectType, lineIndex: number) => {
                if (addressLine.address_line != "") {
                  validationResult.actions.removeValidationError(
                    "contact",
                    `contact.address.${index}.street_address.${lineIndex}.address_line`
                  );
                }
              }
            );
          }
        );

        if (formData.state.authorized.individuals[0].phone != "")
          validationResult.actions.removeValidationError(
            "authorized",
            "authorized.individuals.0.phone"
          );
      }
    },
  },
};
