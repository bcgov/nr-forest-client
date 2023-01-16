import type { FormSectionSchemaType } from "../../../core/AppType";

export const authorizedSectionSchema: FormSectionSchemaType = {
  container: { title: "Add authorized individuals", id: "authorized" },
  content: [
    {
      fieldProps: {
        id: "individuals",
      },
      type: "table",
      addButtonText: "+ Add another authorized person",
      subfields: [
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
        {
          fieldProps: {
            label: "Last name",
            id: "last_name",
          },
          type: "input",
        },
        {
          fieldProps: {
            label: "Phone number",
            id: "phone",
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
};
