import { describe, it, expect } from "vitest";
import { mount } from "@vue/test-utils";

import FormInput from "../../common/FormInput.vue";
import FormFieldTemplate from "../../common/FormFieldTemplate.vue";

describe("FormInput", () => {
  it("component defined", () => {
    const wrapper = mount(FormInput);
    expect(wrapper).toBeDefined();
    expect(wrapper.find("input").exists()).toBe(true);
  });

  it("renders props fieldProps successfully", () => {
    const wrapper = mount(FormInput, {
      props: { fieldProps: { label: "Test Form Input Title" } },
    });
    expect(wrapper.findComponent(FormFieldTemplate).exists()).toBe(true);
    expect(wrapper.text()).toContain("Test Form Input Title");
  });

  it("renders props value, emit function successfully", async () => {
    const wrapper = mount(FormInput, {
      props: { fieldProps: { id: "test-form-input-id" }, value: "hello" },
    });

    const input = wrapper.find("input");
    expect(input.element.value).toBe("hello");
    await input.setValue("Test");
    expect(input.element.value).toBe("Test");
    // assert the emitted event has been performed
    expect(wrapper.emitted()).toHaveProperty("updateValue");

    const updateValueEvent = wrapper.emitted("updateValue");
    // updateValue has been called once when doing the input.setValue
    expect(updateValueEvent).toHaveLength(1);
    // test the given parameters
    expect(updateValueEvent[0]).toEqual(["test-form-input-id", "Test"]);
  });

  it("renders props disabled successfully", async () => {
    const wrapper = mount(FormInput, {
      props: { disabled: true },
    });

    const input = wrapper.find("input");
    expect(input.attributes().disabled).toBeDefined();
  });
});
