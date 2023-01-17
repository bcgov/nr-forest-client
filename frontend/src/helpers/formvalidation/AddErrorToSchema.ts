import _ from "lodash";
import { formData, validationResult } from "../../helpers/FormState";
import type {
  FormComponentSchemaType,
  FormSectionSchemaType,
} from "../../core/FormType";

export const addErrMsgToSchema = (
  sectionSchema:
    | FormSectionSchemaType
    | {
        individual: FormSectionSchemaType;
        soleProprietorship: FormSectionSchemaType;
        company: FormSectionSchemaType;
      },
  containerId: string
) => {
  let sectionSchemaCopy = JSON.parse(JSON.stringify(sectionSchema));
  if (containerId != "information") {
    if (
      _.has(validationResult, ["state", containerId]) &&
      validationResult.state[containerId].length > 0
    ) {
      validationResult.state[containerId].forEach((eachRow) => {
        const newSectionSchemaContent = sectionSchemaCopy.content.map(
          (fieldSchema: FormComponentSchemaType) =>
            fieldSchema.fieldProps.id == eachRow.fieldId
              ? {
                  ...fieldSchema,
                  fieldProps: {
                    ...fieldSchema.fieldProps,
                    errorMsg: eachRow.errorMsg,
                  },
                }
              : fieldSchema
        );
        sectionSchemaCopy = {
          ...sectionSchemaCopy,
          content: newSectionSchemaContent,
        };
      });
    }
  } else {
    if (
      _.has(validationResult, ["state", "information"]) &&
      validationResult.state.information.length > 0 &&
      formData.state.begin.client_type != ""
    ) {
      let type = formData.state.begin.client_type;
      if (
        formData.state.begin.client_type != "individual" &&
        formData.state.begin.client_type != "soleProprietorship"
      ) {
        type = "company";
      }
      validationResult.state.information.forEach((eachRow) => {
        const newSectionSchemaContent = sectionSchemaCopy[type].content.map(
          (fieldSchema: FormComponentSchemaType) =>
            fieldSchema.fieldProps.id == eachRow.fieldId
              ? {
                  ...fieldSchema,
                  fieldProps: {
                    ...fieldSchema.fieldProps,
                    errorMsg: eachRow.errorMsg,
                  },
                }
              : fieldSchema
        );
        sectionSchemaCopy[type] = {
          ...sectionSchemaCopy[type],
          content: newSectionSchemaContent,
        };
      });
    }
  }

  return sectionSchemaCopy;
};
