import { reactive } from "vue";

// global app state
export const store = reactive({
  testEmail: "" as String,
  updateTestEmail(newValue: String) {
    this.testEmail = newValue;
  },
});
