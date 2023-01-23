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
  location: {
    address: [
      {
        street_address: "",
        country: "",
        province: "",
        city: "",
        postal_code: "",
        index: 0, // any array data need to have this index, as an auto generated random number to be as unique identity
        contact: [
          {
            contact_type: "",
            name: "",
            cell_phone: "",
            business_phone: "",
            fax_number: "",
            email: "",
            index: 0, // need use this index to be unique identity when display data in form tables
          },
        ],
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
    setPrepopulateFieldsForIndividual(
      firstName: string,
      lastName: string,
      birthdate: string
    ) {
      _.set(formData.state, "information.first_name", firstName);
      _.set(formData.state, "information.last_name", lastName);
      _.set(formData.state, "information.birthdate", birthdate);
    },
    setPrepopulateFieldsForBusiness(
      businessName: string,
      registryNumber: string
    ) {
      _.set(formData.state, "information.business_name", businessName);
      _.set(formData.state, "information.registration_number", registryNumber);
    },
  },

  // This section will manage the actions needed for our store
  actions: {
    updateFormValue(newValue: any, dataPath: string) {
      formData.mutations.updateFormValue(newValue, dataPath);
      formData.actions.cleanErrorMsg();
      // set value for prepopulate fields, and disabled prepoplated fields
      if (
        dataPath == "begin.client_type" &&
        formData.getters.getFormDataByPath(dataPath) == "individual"
      ) {
        disabledFields.actions.setDisabledFieldsForSection("information", [
          "information.first_name",
          "information.last_name",
          "information.birthdate",
        ]);
        // todo: put hardcode data here, should be the information get from login
        formData.mutations.setPrepopulateFieldsForIndividual(
          "prepopulated firstname",
          "prepolulated lastname",
          "1990-01-01"
        );
      } else if (
        dataPath == "begin.client_type" &&
        formData.getters.getFormDataByPath(dataPath) != "soleProprietorship"
      ) {
        disabledFields.actions.setDisabledFieldsForSection("information", [
          "information.business_name",
          "information.registration_number",
        ]);
        // todo: put hardcode data here, should be the information get from login and bc registry
        formData.mutations.setPrepopulateFieldsForBusiness(
          "prepopulated business name",
          "prepolulated number"
        );
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

        // an example to check all array data for form group
        formData.state.location.address.forEach(
          (e: CommonObjectType, index: number) => {
            if (e.country == "Canada") {
              validationResult.actions.removeValidationError(
                "location",
                `location.address.${index}.country`
              );
            }
            // an example to check the array data inside an array, could remove this once get the idea
            e.contact.forEach(
              (contact: CommonObjectType, contactIndex: number) => {
                if (contact.cell_phone != "") {
                  validationResult.actions.removeValidationError(
                    "location",
                    `location.address.${index}.contact.${contactIndex}.cell_phone`
                  );
                }
              }
            );
          }
        );
      }
    },
  },
};
