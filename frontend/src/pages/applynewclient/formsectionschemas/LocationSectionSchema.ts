import type { FormSectionSchemaType } from "../../../core/FormType";

export const locationSectionSchema: FormSectionSchemaType = {
  container: {
    title: "Business Information",
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
            label: "Street address",
            id: "streetAddressId",
            modelName: "streetAddress",
            required: true,
          },
          type: "input",
        },
        {
          fieldProps: {
            label: "Country",
            id: "countryId",
            modelName: "country",
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
                label: "Contact Type",
                id: "contactTypeId",
                modelName: "contactType",
              },
              type: "select",
              options: [],
            },
            {
              fieldProps: {
                label: "Name",
                id: "nameId",
                modelName: "name",
              },
              type: "input",
            },
            {
              fieldProps: {
                label: "Cell Phone",
                id: "cellPhoneId",
                modelName: "cellPhone",
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
                label: "Fax Number",
                id: "faxNumberId",
                modelName: "faxNumber",
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
          ],
        },
      ],
    },
  ],
};
