import type { FormSectionSchemaType } from "../../../core/FormType";

export const locationSectionSchema: FormSectionSchemaType = {
  container: {
    title: "Location Information",
    id: "location",
  },
  content: [
    {
      fieldProps: {
        id: "addressId",
        modelName: "address",
      },
      type: "group",
      addButtonText: "+ Add another address",
      deleteButtonText: "- Remove this address",
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
          type: "input",
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
            label: "Contact",
            id: "contactId",
            modelName: "contact",
          },
          type: "group",
          addButtonText: "+ Add another contact",
          deleteButtonText: "- Remove this contact",
          subfields: [
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
                label: "Contact type",
                id: "contactTypeId",
                modelName: "contactType",
                required: true,
              },
              type: "select",
              options: [],
            },            
            {
              fieldProps: {
                label: "Daytime phone number",
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
