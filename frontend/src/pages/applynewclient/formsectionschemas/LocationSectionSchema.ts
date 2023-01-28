import type { FormSectionSchemaType } from "../../../core/FormType";

export const locationSectionSchema: FormSectionSchemaType = {
  container: {
    title: "Contact Information",
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
            modelName: "street_address",
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
            modelName: "postal_code",
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
                modelName: "contact_type",
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
                modelName: "cell_phone",
              },
              type: "input",
            },
            {
              fieldProps: {
                label: "Business Phone",
                id: "businessPhoneId",
                modelName: "business_phone",
              },
              type: "input",
            },
            {
              fieldProps: {
                label: "Fax Number",
                id: "faxNumberId",
                modelName: "fax_number",
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
