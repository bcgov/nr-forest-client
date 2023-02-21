import type { FormSectionSchemaType } from "../../../core/FormType";

export const locationSectionSchema: FormSectionSchemaType = {
  container: {
    title: "Mailing address",
    id: "location",
  },
  content: [
    {
      fieldProps: {
        id: "addressId",
        modelName: "address",
        note: "This information is from BC Registries. If it's incorrect, go to BC Registries to update it before continuing"
      },
      type: "group",
      addButtonText: "+ Add another address or location",
      deleteButtonText: "- Remove this address or location",
      subfields: [
        {
          fieldProps: {
            label: "Country",
            id: "countryId",
            modelName: "country",
            required: true,
          },
          type: "select",
          options: [],
        },
        {
          fieldProps: {
            label: "Street address",
            id: "streetAddressId",
            modelName: "streetAddress",
            required: true,
          },
          type: "input",
        },
        {
          fieldProps: {
            label: "Province",
            id: "provinceId",
            modelName: "province",
            required: true,
          },
          type: "select",
          options: [],
        },
        {
          fieldProps: {
            label: "City",
            id: "cityId",
            modelName: "city",
            required: true,
          },
          type: "input",
        },
        {
          fieldProps: {
            label: "Postal code",
            id: "postalCodeId",
            modelName: "postalCode",
            required: true,
          },
          type: "input",
        },
        {
          fieldProps: {
            label: "Business Phone",
            id: "businessPhoneId",
            modelName: "businessPhone",
          },
          type: "input",
        },
        {
          fieldProps: {
            label: "Email",
            id: "emailId",
            modelName: "email",
          },
          type: "input",
        },
        {
          fieldProps: {
            label: "Business Contacts",
            id: "contactId",
            modelName: "contact",
          },
          type: "table",
          addButtonText: "+ Add another person for this address or location",
          deleteButtonText: "- Remove person for this address or location",
          subfields: [
            {
              fieldProps: {
                label: "Role",
                id: "contactTypeId",
                modelName: "contactType",
                required: true,
              },
              type: "select",
              options: [],
            },
            {
              fieldProps: {
                label: "Person or department name",
                id: "nameId",
                modelName: "name",
                required: true,
              },
              type: "input",
            },
            {
              fieldProps: {
                label: "Phone number",
                id: "businessPhoneId",
                modelName: "businessPhone",
                required: true,
              },
              type: "input",
            },
            {
              fieldProps: {
                label: "Email address",
                id: "emailId",
                modelName: "email",
                required: true,
              },
              type: "input",
            },
          ],
        },
      ],
    },
  ],
};
