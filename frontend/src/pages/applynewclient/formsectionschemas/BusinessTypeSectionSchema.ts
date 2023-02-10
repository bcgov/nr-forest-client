import type {
  FormSectionSchemaType,
  FormRadioGroupOptionType,
} from "../../../core/FormType";

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
