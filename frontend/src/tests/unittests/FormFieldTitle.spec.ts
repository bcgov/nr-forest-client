import { describe, it, expect } from "vitest";
import { mount } from "@vue/test-utils";

import FormFieldTitle from "../../common/FormFieldTitle.vue";
import "bootstrap/dist/css/bootstrap.css";
import "bootstrap-vue-3/dist/bootstrap-vue-3.css";

describe("FormFieldTitle", () => {
  it("renders properly with no properties", () => {
    const wrapper = mount(FormFieldTitle);
    expect(wrapper.text()).toEqual("");
    expect(wrapper.find("svg").exists()).toBe(false);
  });

  it("renders props label properly", () => {
    const wrapper = mount(FormFieldTitle, {
      props: {
        label: "Test Form Field Title",
      },
    });
    expect(wrapper.text()).toContain("Test Form Field Title");
  });

  it("renders props required properly", () => {
    const wrapper = mount(FormFieldTitle, {
      props: {
        required: true,
      },
    });
    expect(wrapper.find("svg").exists()).toBe(true);
  });
});
