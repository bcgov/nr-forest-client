import { reactive } from "vue";
import type { FormValidationResultType } from "../core/AppType";

// global app states

// vue3 is hard to detect array or object change, so we make them as global state, so the change can be detected by the component is using the value
export const validationResult = reactive({
  value: [] as Array<FormValidationResultType>,
  setValue(result: Array<FormValidationResultType>) {
    this.value = result;
  },
});
