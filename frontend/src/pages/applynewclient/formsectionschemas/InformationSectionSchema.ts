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
      modelName: "first_name",
    },
    type: "input",
  },
  {
    fieldProps: {
      label: "Last name",
      id: "lastNameId",
      modelName: "last_name",
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
          modelName: "registration_number",
        },
        type: "input",
      },
      {
        fieldProps: {
          label: "Are you 'doing business as'?",
          id: "doingBusinessAsCheckId",
          modelName: "doing_business_as_check",
        },
        type: "checkbox",
      },
      {
        fieldProps: {
          label: "Doing business as",
          id: "doingBusinessAsId",
          modelName: "doing_business_as",
        },
        type: "input",
        depend: {
          fieldModelName: "doing_business_as_check",
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
          modelName: "business_name",
          required: true,
          note: "Start typing to search BC registered businesses database",
        },
        type: "input",
      },
      {
        fieldProps: {
          label: "Incorporation or registration number",
          id: "registrationNumberId",
          modelName: "registration_number",
          required: true,
        },
        type: "input",
      },
      {
        fieldProps: {
          label: "WorkSafe BC number",
          id: "workSafeBcNumberId",
          modelName: "work_safe_bc_number",
        },
        type: "input",
      },
      {
        fieldProps: {
          label: "Are you 'doing business as'?",
          id: "doingBusinessAsCheckId",
          modelName: "doing_business_as_check",
          required: true,
        },
        type: "checkbox",
      },
      {
        fieldProps: {
          label: "Doing business as",
          id: "doingBusinessAsId",
          modelName: "doing_business_as",
        },
        type: "input",
        depend: {
          fieldModelName: "doing_business_as_check",
          value: true,
        },
      },
    ],
  },
};
