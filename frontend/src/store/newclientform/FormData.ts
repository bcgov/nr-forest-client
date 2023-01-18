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
        stree_address: "",
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
    updateFormValue(containerId: string, fieldId: string, newValue: any) {
      formData.state[containerId][fieldId] = newValue;
    },
    updateFormArrayValue(
      containerId: string,
      fieldId: string,
      subFieldId: string,
      newValue: any,
      rowIndex: number
    ) {
      formData.state[containerId][fieldId][rowIndex][subFieldId] = newValue;

      // // if the formdata depth increase, for example has repeatble session inside another repeatable session
      // // could update to use the following, so it will automatically detect the path to the key, and update the value based on the path

      // const objectKeys = [] as Array<string>;
      // function getObjectKeys(obj: CommonObjectType, previousPath = "") {
      //   Object.keys(obj).forEach((key) => {
      //     let currentPath = "";
      //     if (key.match(/^-?\d+$/)) currentPath = previousPath || "";
      //     else currentPath = previousPath ? `${previousPath}.${key}` : key;

      //     if (typeof obj[key] !== "object") {
      //       objectKeys.push(currentPath);
      //     } else {
      //       objectKeys.push(currentPath);
      //       getObjectKeys(obj[key], currentPath);
      //     }
      //   });
      // }
      // getObjectKeys(formData.state);
      // const fieldFullPath = objectKeys.filter((keyPath) =>
      //   keyPath.includes(subFieldId)
      // )[0];
      // const fieldParentPath = fieldFullPath.split(".").slice(0, -1).join(".");
      // const arrayValue = _.get(formData.state, fieldParentPath);
      // arrayValue[rowIndex][subFieldId] = newValue;
      // _.set(formData.state, fieldParentPath, arrayValue);
    },
    addRow(containerId: string, fieldId: string, newRow: CommonObjectType) {
      formData.state[containerId][fieldId].push({
        ...newRow,
        index: Math.floor(Math.random() * 10000000),
      });
    },
    deleteRow(containerId: string, fieldId: string, rowIndex: number) {
      formData.state[containerId][fieldId].splice(rowIndex, 1);
    },
  },
  // This section will manage the actions needed for our store
  actions: {
    updateFormValue(containerId: string, fieldId: string, newValue: any) {
      formData.mutations.updateFormValue(containerId, fieldId, newValue);
      formData.actions.cleanErrorMsg();
    },
    updateFormArrayValue(
      containerId: string,
      fieldId: string,
      subFieldId: string,
      newValue: any,
      rowIndex: number
    ) {
      formData.mutations.updateFormArrayValue(
        containerId,
        fieldId,
        subFieldId,
        newValue,
        rowIndex
      );
      formData.actions.cleanErrorMsg();
    },
    addRow(containerId: string, fieldId: string) {
      const newContainer =
        initialFormData[containerId as keyof typeof initialFormData];
      const defaultNew = JSON.parse(
        JSON.stringify(newContainer[fieldId as keyof typeof newContainer][0])
      );
      formData.mutations.addRow(containerId, fieldId, defaultNew);
    },
    deleteRow(containerId: string, fieldId: string, rowIndex: number) {
      formData.mutations.deleteRow(containerId, fieldId, rowIndex);
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
