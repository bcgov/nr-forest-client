export const testGroup = {
  schema: {
    fieldProps: {
      label: "Title",
      id: "exampleGroup",
      modelName: "exampleGroup",
    },
    type: "group",
    addButtonText: "+ Add another",
    deleteButtonText: "- Remove this",
    subfields: [
      {
        fieldProps: {
          label: "Link",
          id: "groupInput",
          modelName: "groupInput"
        },
        type: "input",
      },
      {
        fieldProps: { 
          label: "Hobby", 
          id: "groupCheckBoxGroup",
          modelName: "groupCheckBoxGroup"
        },
        type: "checkboxgroup",
        options: [
          { code: "swim", text: "Swim" },
          { code: "dance", text: "Dance" },
          { code: "sing", text: "Sing" },
        ],
      },
    ],
  },
  data: [
    {
      groupInput: "",
      groupCheckBoxGroup: [],
    },
    {
      groupInput: "",
      groupCheckBoxGroup: [],
    },
  ],
};

export const testTable = {
  schema: {
    fieldProps: {
      label: "Title",
      id: "exampleTable",
      modelName: "exampleTable",
    },
    type: "table",
    addButtonText: "+ Add another",
    subfields: [
      {
        fieldProps: {
          label: "Name",
          id: "tableInput",
          modelName: "tableInput",
        },
        type: "input",
      },
      {
        fieldProps: { 
          label: "Color", 
          id: "tableSelect",
          modelName: "tableSelect",
        },
        type: "select",
        options: [
          { value: "red", text: "Red" },
          { value: "green", text: "green" },
        ],
      },
    ],
  },
  data: [
    {
      tableInput: "",
      tableSelect: "",
    },
    {
      tableInput: "",
      tableSelect: "",
    },
  ],
};
