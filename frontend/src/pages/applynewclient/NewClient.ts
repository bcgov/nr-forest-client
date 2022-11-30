const formApplyReason = [
  { code: "never_had", text: "I've never had a client number" },
  {
    code: "need_new",
    text: "I need a new client number because of an amalgamation",
  },
  { code: "joint_venture", text: "I'm entering into a joint venture" },
];

const formTenureType = [
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
export const beginSectionSchema = {
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
      options: [],
    },
  ],
};

const informationSectionCommonSchema = [
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

export const informationSectionSchema = {
  individual: {
    container: { title: "Individual information", id: "information" },
    content: [informationSectionCommonSchema],
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
          label: "Are you 'doing business as'?",
          id: "doing_business_as",
        },
        type: "input",
        request: "doing_business_as_check",
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
        request: "doing_business_as_check",
      },
    ],
  },
};

export const contactSectionSchema = {};

export const authorizedSectionSchema = {
  container: { title: "Add authorized individuals", id: "authorized" },
  content: [
    {
      fieldProps: {
        id: "individuals",
      },
      type: "table",
      addButtonText: "+ Add another authorized person",
      columns: [
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
      ],
    },
  ],
};
