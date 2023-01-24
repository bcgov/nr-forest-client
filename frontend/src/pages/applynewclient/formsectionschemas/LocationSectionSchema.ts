import type { FormSectionSchemaType } from "../../../core/FormType";

export const locationSectionSchema: FormSectionSchemaType = {
  container: { title: "Contact Information", id: "location" },
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
            id: "street_address",
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
        {
          fieldProps: {
            label: "Contact",
            id: "contact",
          },
          type: "group",
          addButtonText: "+ Add another contact",
          deleteButtonText: "- Remove this contact",
          subfields: [
            {
              fieldProps: { label: "Contact Type", id: "contact_type" },
              type: "select",
              options: [],
            },
            {
              fieldProps: {
                label: "Name",
                id: "name",
              },
              type: "input",
            },
            {
              fieldProps: {
                label: "Cell Phone",
                id: "cell_phone",
              },
              type: "input",
            },
            {
              fieldProps: {
                label: "Business Phone",
                id: "business_phone",
              },
              type: "input",
            },
            {
              fieldProps: {
                label: "Fax Number",
                id: "fax_number",
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
    },
  ],
};
