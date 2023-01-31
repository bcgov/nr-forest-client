import _ from "lodash";
import type {
  FormValidationRequiredFieldType,
  CommonObjectType,
} from "../../core/FormType";

export const checkMissingRequireField = (
  requireList: Array<FormValidationRequiredFieldType>,
  formData: CommonObjectType
) => {
  // todo: so far it supports at most 2 level in-depth array, if the depth increase, need to redesign the structure

  let missingRequire = false;

  for (let i = 0; i < requireList.length; i++) {
    const require = requireList[i];
    if (!require.subFieldModelName) {
      if (_.get(formData, require.path) == "") {
        missingRequire = true;
        break;
      }
    } else if (_.get(formData, require.path)) {
      // check table and group, to see if each row got all required fields
      for (let j = 0; j < _.get(formData, require.path).length; j++) {
        if (!require.subPath) {
          if (
            _.get(formData, `${require.path}.${j}.${require.subFieldModelName}`) == ""
          ) {
            missingRequire = true;
            break;
          }
        } else {
          const subArray = _.get(
            formData,
            `${require.path}.${j}.${require.subPath}`
          );
          if (Array.isArray(subArray)) {
            for (let k = 0; k < subArray.length; k++) {
              if (
                _.get(
                  formData,
                  `${require.path}.${j}.${require.subPath}.${k}.${require.subFieldModelName}`
                ) == ""
              ) {
                missingRequire = true;
                break;
              }
            }
          }
        }
      }
    }
  }
  return missingRequire;
};
