import { reactive } from "vue";
import type { FormValidationResultType } from "../core/AppType";

// global app states

// the change of a global state can be detected by all the components are using the value
export const validationResult = reactive({
  value: [] as Array<FormValidationResultType>,
  setValue(result: Array<FormValidationResultType>) {
    this.value = result;
  },
});
