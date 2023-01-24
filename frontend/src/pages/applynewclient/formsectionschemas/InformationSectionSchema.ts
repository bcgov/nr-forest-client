import type {
  FormSectionSchemaType,
  FormComponentSchemaType,
} from "../../../core/FormType";

const informationSectionCommonSchema: Array<FormComponentSchemaType> = [
  {
    fieldProps: { label: "First name", required: true, id: "first_name" },
    type: "input",
  },
  {
    fieldProps: { label: "Last name", id: "last_name" },
    type: "input",
  },
  {
    fieldProps: {
      label: "Please select your birthdate",
      required: true,
      id: "birthdate",
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
        fieldProps: { label: "Registration number", id: "registration_number" },
        type: "input",
      },
      {
        fieldProps: {
          label: "Are you 'doing business as'?",
          id: "doing_business_as_check",
        },
        type: "checkbox",
      },
      {
        fieldProps: {
          label: "Doing business as",
          id: "doing_business_as",
        },
        type: "input",
        depend: {
          fieldId: "doing_business_as_check",
          value: true,
        },
      },
    ],
  },
  company: {
    container: { title: "Company information", id: "information" },
    content: [
      {
        fieldProps: {
          label: "Registered business name",
          id: "business_name",
          required: true,
          note: "Start typing to search BC registered businesses database",
        },
        type: "input",
      },
      {
        fieldProps: {
          label: "Incorporation or registration number",
          id: "registration_number",
          required: true,
        },
        type: "input",
      },
      {
        fieldProps: {
          label: "WorkSafe BC number",
          id: "worksafebc_number",
        },
        type: "input",
      },
      {
        fieldProps: {
          label: "Are you 'doing business as'?",
          id: "doing_business_as_check",
          required: true,
        },
        type: "checkbox",
      },
      {
        fieldProps: {
          label: "Doing business as",
          id: "doing_business_as",
        },
        type: "input",
        depend: {
          fieldId: "doing_business_as_check",
          value: true,
        },
      },
    ],
  },
};
