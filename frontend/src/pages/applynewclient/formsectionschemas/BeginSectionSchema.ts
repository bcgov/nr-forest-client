import type {
  FormSectionSchemaType,
  FormRadioGroupOptionType,
} from "../../../core/FormType";

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

// each id is aligned with the object key in the newClientData
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
