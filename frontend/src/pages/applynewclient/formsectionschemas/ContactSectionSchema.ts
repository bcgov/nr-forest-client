import type { FormSectionSchemaType } from "../../../core/AppType";

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
