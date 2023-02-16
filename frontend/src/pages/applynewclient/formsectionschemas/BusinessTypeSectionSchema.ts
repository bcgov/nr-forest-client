import type {
  FormSectionSchemaType,
  FormRadioGroupOptionType,
} from "../../../core/FormType";

const formApplyReason: Array<FormRadioGroupOptionType> = [
  {
    code: "never_had",
    text: "I've never had a client number",
  },
  {
    code: "need_new",
    text: "I need a new client number because of an amalgamation",
  },
  {
    code: "joint_venture",
    text: "I'm entering into a joint venture",
  },
];

const formTenureType: Array<FormRadioGroupOptionType> = [
  {
    code: "forest",
    text: "Forest",
  },
  {
    code: "range",
    text: "Range",
  },
  {
    code: "oilgas",
    text: "Oil & gas",
  },
];

export const businessTypeSectionSchema: FormSectionSchemaType = {
  container: {
    title: "Business Type",
    id: "businessType",
  }, // property for CollapseCard when use it
  content: [
    // form content for each CollapseCard
    {
      fieldProps: {
        label: "What type of business are you?",
        required: true,
        id: "clientTypeId",
        modelName: "clientType",
      },
      type: "select",
      options: [],
    },
  ],
};
