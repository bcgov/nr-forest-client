import { reactive } from "vue";
import type {
  CommonObjectType,
  FormValidationResultType,
} from "../core/AppType";

// global state
// the change of a global state can be detected by all the components are using the value
export const formData = reactive({
  data: {
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
  } as {
    [key: string]: CommonObjectType;
  },
  updateFormValue(containerId: string, fieldId: string, newValue: any) {
    this.data[containerId][fieldId] = newValue;
    this.cleanErrorMsg();
  },
  updateFormArrayValue(
    containerId: string,
    fieldId: string,
    subFieldId: string,
    newValue: any,
    rowIndex: number
  ) {
    this.data[containerId][fieldId][rowIndex][subFieldId] = newValue;
    this.cleanErrorMsg();
  },
  addRow(containerId: string, fieldId: string) {
    const newContainer = this.data[containerId as keyof typeof this.data];
    const defaultNew = JSON.parse(
      JSON.stringify(newContainer[fieldId as keyof typeof newContainer][0])
    );
    this.data[containerId][fieldId].push({
      ...defaultNew,
      index: Math.floor(Math.random() * 10000000),
    });
  },
  deleteRow(containerId: string, fieldId: string, rowIndex: number) {
    this.data[containerId][fieldId].splice(rowIndex, 1);
  },
  cleanErrorMsg() {
    // todo: this is the function to check every feild if meet the validation rules, remove the error message if met
    // a hard code example
    if (this.data.begin.client_type == "individual")
      validationResult.setValue([]);
  },
});

export const validationResult = reactive({
  value: {} as FormValidationResultType,
  setValue(result: FormValidationResultType) {
    this.value = result;
  },
});
