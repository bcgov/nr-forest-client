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
    container: { title: "Sole proprietor name", id: "information" },
    content: [
      {
        fieldProps: {
          label: "Business Name",
          id: "businessNameId",
          modelName: "businessName",
          required: true,
        },
        type: "input",
      },
      {
        fieldProps: {
          label: "Doing business as",
          id: "doingBusinessAsId",
          modelName: "doingBusinessAsName",
        },
        type: "input"
      },
    ],
  },
  company: {
    container: {
      title: "Registered business name",
      id: "information",
    },
    content: [
      {
        fieldProps: {
          label: "Start typing to search for your B.C. registered business",
          id: "businessNameId",
          dataListId: "businessNameListId",
          modelName: "businessName",
          required: true,
          note: "The name must be the same as it is in BC Registries",
        },
        type: "autocomplete",
      },
      {
        fieldProps: {
          label: "Doing business as",
          id: "doingBusinessAsId",
          modelName: "doingBusinessAsName",
        },
        type: "input"
      },
    ],
  },
};
