import type {
  FormComponentSchemaType,
  FormFieldValidationResultType,
} from "../../core/AppType";

export const addErrMsgToSchema = (
  schemaContent: Array<FormComponentSchemaType>,
  errorInfo: FormFieldValidationResultType
) => {
  return schemaContent.map((fieldSchema: FormComponentSchemaType) =>
    fieldSchema.fieldProps.id == errorInfo.fieldId
      ? {
          ...fieldSchema,
          fieldProps: {
            ...fieldSchema.fieldProps,
            errorMsg: errorInfo.errorMsg,
          },
        }
      : fieldSchema
  );
};
