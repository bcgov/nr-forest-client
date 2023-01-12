import type {
  FormSectionSchemaType,
  FormComponentSchemaType,
  FormRadioGroupOptionType,
  FormValidationRequiredField,
} from "../../core/AppType";

const formApplyReason: Array<FormRadioGroupOptionType> = [
  { code: "never_had", text: "I've never had a client number" },
  {
    code: "need_new",
    text: "I need a new client number because of an amalgamation",
  },
  { code: "joint_venture", text: "I'm entering into a joint venture" },
];

const formTenureType: Array<FormRadioGroupOptionType> = [
  { code: "forest", text: "Forest" },
  { code: "range", text: "Range" },
  { code: "oilgas", text: "Oil & gas" },
];

export const newClientData = {
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

// each id is aligned with the newClientData
export const beginSectionSchema: FormSectionSchemaType = {
  container: { title: "Let's begin", id: "begin" }, // property for CollapseCard when use it
  content: [
    // form content for each CollapseCard
    {
      fieldProps: { label: "Why are you applying?", id: "reason" },
      type: "radiogroup",
      options: formApplyReason,
    },
    {
      fieldProps: { label: "Select your tenure type", id: "tenure_type" },
      type: "radiogroup",
      options: formTenureType,
    },
    {
      fieldProps: {
        label: "What type of business are you?",
        required: true,
        id: "client_type",
      },
      type: "select",
      options: [
        { value: "individual", text: "Individual" },
        { value: "company", text: "Company" },
      ],
    },
  ],
};

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

export const contactSectionSchema: FormSectionSchemaType = {
  container: { title: "Contact Information", id: "contact" },
  content: [
    {
      fieldProps: {
        id: "address",
      },
      type: "group",
      addButtonText: "+ Add another address",
      deleteButtonText: "- Remove this address",
      subfields: [
        {
          fieldProps: {
            label: "Street address",
            id: "stree_address",
            required: true,
          },
          type: "input",
        },
        {
          fieldProps: {
            label: "Country",
            id: "country",
            required: true,
          },
          type: "input",
        },
        {
          fieldProps: {
            label: "Province",
            id: "province",
            required: true,
          },
          type: "input",
        },
        {
          fieldProps: {
            label: "City",
            id: "city",
            required: true,
          },
          type: "input",
        },
        {
          fieldProps: {
            label: "Postal code",
            id: "postal_code",
            required: true,
          },
          type: "input",
        },
      ],
    },
  ],
};

export const authorizedSectionSchema: FormSectionSchemaType = {
  container: { title: "Add authorized individuals", id: "authorized" },
  content: [
    {
      fieldProps: {
        id: "individuals",
      },
      type: "table",
      addButtonText: "+ Add another authorized person",
      subfields: [
        {
          fieldProps: { label: "Contact Type", id: "contact_type" },
          type: "select",
          options: [],
        },
        {
          fieldProps: {
            label: "First name or company",
            id: "firsname_or_company",
          },
          type: "input",
        },
        {
          fieldProps: {
            label: "Last name",
            id: "last_name",
          },
          type: "input",
        },
        {
          fieldProps: {
            label: "Phone number",
            id: "phone",
          },
          type: "input",
        },
        {
          fieldProps: {
            label: "Email",
            id: "email",
          },
          type: "input",
        },
      ],
    },
  ],
};

/* --------------- Required Fields --------------------- */
export const commonRequiredFields: Array<FormValidationRequiredField> = [
  { containerId: "begin", fieldId: "client_type" },
  { containerId: "contact", fieldId: "address", subFieldId: "stree_address" },
  { containerId: "contact", fieldId: "address", subFieldId: "country" },
  { containerId: "contact", fieldId: "address", subFieldId: "province" },
  { containerId: "contact", fieldId: "address", subFieldId: "city" },
  { containerId: "contact", fieldId: "address", subFieldId: "postal_code" },
];

export const businessRequiredFields: Array<FormValidationRequiredField> = [
  { containerId: "information", fieldId: "business_name" },
  { containerId: "information", fieldId: "registration_number" },
];

export const individualRequiredFields: Array<FormValidationRequiredField> = [
  { containerId: "information", fieldId: "first_name" },
  { containerId: "information", fieldId: "birthdate" },
];
