import { reactive } from "vue";
import _ from "lodash";
import { validationResult } from "./FormValidation";
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
        stree_address: [{ address_line: "1", index: 0 }],
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
  },
  // This section will manage the changes into the state, can be only called by actions
  mutations: {
    updateFormValue(newValue: any, dataPath: Array<string>) {
      _.set(formData.state, dataPath, newValue);
    },
    addRow(newRow: CommonObjectType, dataPath: Array<string>) {
      const arrayValue = _.get(formData.state, dataPath);
      arrayValue.push({
        ...newRow,
        index: Math.floor(Math.random() * 10000000),
      });
      _.set(formData.state, dataPath, arrayValue);
    },
    deleteRow(rowIndex: number, dataPath: Array<string>) {
      const arrayValue = _.get(formData.state, dataPath);
      arrayValue.splice(rowIndex, 1);
      _.set(formData.state, dataPath, arrayValue);
    },
  },
  // This section will manage the actions needed for our store
  actions: {
    updateFormValue(newValue: any, dataPath: Array<string>) {
      formData.mutations.updateFormValue(newValue, dataPath);
      formData.actions.cleanErrorMsg();
    },
    addRow(dataPath: Array<string>) {
      const defaultNew = JSON.parse(
        JSON.stringify(_.get(initialFormData, dataPath)[0])
      );
      formData.mutations.addRow(defaultNew, dataPath);
    },
    deleteRow(rowIndex: number, dataPath: Array<string>) {
      formData.mutations.deleteRow(rowIndex, dataPath);
    },
    cleanErrorMsg() {
      // todo: this is the function to check every feild if meet the validation rules, remove the error message if met
      // some hard code examples to remove validationError after user makes the correctness
      if (!_.isEmpty(validationResult)) {
        if (formData.state.begin.client_type == "individual")
          validationResult.actions.removeValidationError(
            "begin",
            "client_type"
          );
        if (formData.state.information.first_name == "test")
          validationResult.actions.removeValidationError(
            "information",
            "first_name"
          );
        formData.state.contact.address.forEach(
          (e: CommonObjectType, index: number) => {
            if (e.country == "Canada") {
              validationResult.actions.removeValidationError(
                "contact",
                "address",
                "country",
                index
              );
            }
          }
        );
        formData.state.authorized.individuals.forEach(
          (e: CommonObjectType, index: number) => {
            if (e.phone != "") {
              validationResult.actions.removeValidationError(
                "authorized",
                "individuals",
                "phone",
                index
              );
            }
          }
        );
      }
    },
  },
};
