import type {
  FormSectionSchemaType,
  FormComponentSchemaType,
} from "../../../core/FormType";

const informationSectionCommonSchema: Array<FormComponentSchemaType> = [
  {
    fieldProps: {
      label: "First name",
      required: true,
      id: "firstNameId",
      modelName: "firstName",
    },
    type: "input",
  },
  {
    fieldProps: {
      label: "Last name",
      id: "lastNameId",
      modelName: "lastName",
    },
    type: "input",
  },
  {
    fieldProps: {
      label: "Please select your birthdate",
      required: true,
      id: "birthdateId",
      modelName: "birthdate",
    },
    type: "input", // todo: change to date
  },
];

export const informationSectionSchema: {
  individual: FormSectionSchemaType;
  soleProprietorship: FormSectionSchemaType;
  company: FormSectionSchemaType;
} = {
  individual: {
    container: { title: "Individual information", id: "information" },
    content: informationSectionCommonSchema,
  },
  soleProprietorship: {
    container: { title: "Individual information", id: "information" },
    content: [
      ...informationSectionCommonSchema,
      {
        fieldProps: {
          label: "Registration number",
          id: "registrationNumberId",
          modelName: "registrationNumber",
        },
        type: "input",
      },
      {
        fieldProps: {
          label: "Are you 'doing business as'?",
          id: "doingBusinessAsIndId",
          modelName: "doingBusinessAsInd",
        },
        type: "checkbox",
      },
      {
        fieldProps: {
          label: "Doing business as",
          id: "doingBusinessAsId",
          modelName: "doingBusinessAsName",
        },
        type: "input",
        depend: {
          fieldModelName: "doingBusinessAsInd",
          value: true,
        },
      },
    ],
  },
  company: {
    container: {
      title: "Company information",
      id: "information",
    },
    content: [
      {
        fieldProps: {
          label: "Registered business name",
          id: "businessNameId",
          modelName: "businessName",
          required: true,
          note: "Start typing to search BC registered businesses database",
        },
        type: "input",
      },
      {
        fieldProps: {
          label: "Incorporation or registration number",
          id: "registrationNumberId",
          modelName: "registrationNumber",
          required: true,
        },
        type: "input",
      },
      {
        fieldProps: {
          label: "WorkSafe BC number",
          id: "workSafeBcNumberId",
          modelName: "workSafeBcNumber",
        },
        type: "input",
      },
      {
        fieldProps: {
          label: "Are you 'doing business as'?",
          id: "doingBusinessAsIndId",
          modelName: "doingBusinessAsInd",
          required: true,
        },
        type: "checkbox",
      },
      {
        fieldProps: {
          label: "Doing business as",
          id: "doingBusinessAsId",
          modelName: "doingBusinessAsName",
        },
        type: "input",
        depend: {
          fieldModelName: "doingBusinessAsInd",
          value: true,
        },
      },
    ],
  },
};
