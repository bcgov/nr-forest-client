import { describe, it, expect } from "vitest";
import { mount } from "@vue/test-utils";

import FormFieldTitle from "../common/FormFieldTitle.vue";

describe("FormFieldTitle", () => {
  it("renders props label properly", () => {
    const wrapper = mount(FormFieldTitle, {
      props: {
        label: "Text Form Field Title",
      },
    });
    expect(wrapper.text()).toContain("Text Form Field Title");
  });
});
