import type { FormValidationRequiredFieldType } from "../../core/FormType";

export const checkMissingRequireField = (
  requireList: Array<FormValidationRequiredFieldType>,
  formData: any
) => {
  let missingRequire = false;
  for (let i = 0; i < requireList.length; i++) {
    const require = requireList[i];
    if (!require.subFieldId) {
      if (formData[require.containerId][require.fieldId] == "") {
        missingRequire = true;
        break;
      }
    } else {
      // check table and group, to see if each row got all required fields
      for (
        let j = 0;
        j < formData[require.containerId][require.fieldId].length;
        j++
      ) {
        const row = formData[require.containerId][require.fieldId][j];
        if (row[require.subFieldId] == "") {
          missingRequire = true;
          break;
        }
      }
    }
  }
  return missingRequire;
};
