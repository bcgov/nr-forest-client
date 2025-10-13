import { describe, it, expect } from "vitest";
import { mount } from "@vue/test-utils";

import DropdownInputComponent from "@/components/forms/DropdownInputComponent.vue";

describe("ComboBoxInputComponent", () => {
  const validations = [
    (value: any) => (value === "A" ? "A is not supported" : ""),
  ];

  const objectError = {
    errorMsg: "A is not supported",
    custom: {
      foo: "bar",
    },
  };
  const objectErrorValidations = [(value: any) => (value === "A" ? objectError : "")];

  const eventContent = (value: string) => {
    return {
      detail: {
        item: { "data-value": value, getAttribute: (key: string) => value },
      },
    };
  };

  it("should render", () => {
    const wrapper = mount(DropdownInputComponent, {
      props: {
        id: "test",
        label: "test",
        tip: "",
        modelValue: [
          { code: "A", name: "Value A" },
          { code: "B", name: "Value B" },
        ],
        initialValue: "",
        validations: [],
      },
    });

    expect(
      wrapper.find('cds-dropdown-item[data-value="Value A"]').exists()
    ).toBe(true);
    expect(
      wrapper.find('cds-dropdown-item[data-value="Value B"]').exists()
    ).toBe(true);
    expect(
      wrapper.find('cds-dropdown-item[data-value="Value C"]').exists()
    ).toBe(false);
  });

  it("should emit event when changing selection", async () => {
    const wrapper = mount(DropdownInputComponent, {
      props: {
        id: "test",
        label: "test",
        tip: "",
        modelValue: [
          { code: "A", name: "Value A" },
          { code: "B", name: "Value B" },
        ],
        initialValue: "",
        validations: [],
      },
    });

    const dropdown = wrapper.find("cds-dropdown");

    await dropdown.trigger("cds-dropdown-selected", eventContent("Value A"));

    expect(wrapper.emitted("update:modelValue")).toBeTruthy();
    expect(wrapper.emitted("update:modelValue")![0][0]).toBe("A");

    expect(wrapper.emitted("update:selectedValue")).toBeTruthy();
    expect(wrapper.emitted("update:selectedValue")![0][0]).toStrictEqual({
      code: "A",
      name: "Value A",
    });
  });

  it("should emit empty then emit not empty", async () => {
    const wrapper = mount(DropdownInputComponent, {
      props: {
        id: "test",
        label: "test",
        tip: "",
        modelValue: [
          { code: "A", name: "Value A" },
          { code: "B", name: "Value B" },
        ],
        initialValue: "",
        validations: [],
      },
    });

    expect(wrapper.emitted("empty")).toBeTruthy();
    expect(wrapper.emitted("empty")![0][0]).toBe(true);

    const dropdown = wrapper.find("cds-dropdown");

    await dropdown.trigger("cds-dropdown-selected", eventContent("Value A"));

    expect(wrapper.emitted("empty")).toBeTruthy();
    expect(wrapper.emitted("empty")![1][0]).toBe(false);
  });

  it("should validate and emit error if required", async () => {
    const wrapper = mount(DropdownInputComponent, {
      props: {
        id: "test",
        label: "test",
        tip: "",
        modelValue: [
          { code: "A", name: "Value A" },
          { code: "B", name: "Value B" },
        ],
        initialValue: "",
        validations,
      },
    });

    const dropdown = wrapper.find("cds-dropdown");

    await dropdown.trigger("cds-dropdown-selected", eventContent("Value A"));

    await wrapper.trigger("click");

    expect(wrapper.emitted("update:modelValue")).toBeTruthy();
    expect(wrapper.emitted("update:modelValue")![0][0]).toBe("A");

    expect(wrapper.emitted("update:selectedValue")).toBeTruthy();
    expect(wrapper.emitted("update:selectedValue")![0][0]).toStrictEqual({
      code: "A",
      name: "Value A",
    });

    expect(wrapper.emitted("error")).toBeTruthy();
    expect(wrapper.emitted("error")![0][0]).toBe("A is not supported");
  });

  it("should not emit error if the change on selected value was not made by the user", async () => {
    const wrapper = mount(DropdownInputComponent, {
      props: {
        id: "test",
        label: "test",
        tip: "",
        modelValue: [
          { code: "A", name: "Value A" },
          { code: "B", name: "Value B" },
        ],
        initialValue: "",
        validations,
      },
    });

    await wrapper.setProps({ initialValue: "Value A" });

    expect(wrapper.emitted("update:modelValue")).toBeTruthy();
    expect(wrapper.emitted("update:modelValue")![0][0]).toBe("A");

    expect(wrapper.emitted("update:selectedValue")).toBeTruthy();
    expect(wrapper.emitted("update:selectedValue")![0][0]).toStrictEqual({
      code: "A",
      name: "Value A",
    });

    expect(wrapper.emitted("error")).toBeFalsy();
  });

  it("should emit error with empty payload (meaning it is valid) even if the change on selected value was not made by the user", async () => {
    const wrapper = mount(DropdownInputComponent, {
      props: {
        id: "test",
        label: "test",
        tip: "",
        modelValue: [
          { code: "A", name: "Value A" },
          { code: "B", name: "Value B" },
        ],
        initialValue: "",
        validations,
      },
    });

    await wrapper.setProps({ initialValue: "Value B" });

    expect(wrapper.emitted("update:modelValue")).toBeTruthy();
    expect(wrapper.emitted("update:modelValue")![0][0]).toBe("B");

    expect(wrapper.emitted("update:selectedValue")).toBeTruthy();
    expect(wrapper.emitted("update:selectedValue")![0][0]).toStrictEqual({
      code: "B",
      name: "Value B",
    });

    expect(wrapper.emitted("error")).toBeTruthy();

    // Error payload is empty - i.e. it is valid
    expect(wrapper.emitted("error")![0][0]).toBe(undefined);
  });

  it.each([[""], [undefined], [null]])(
    "should clear the selected value when initialValue changes to a falsy value (%s)",
    async (value) => {
      const wrapper = mount(DropdownInputComponent, {
        props: {
          id: "test",
          label: "test",
          tip: "",
          modelValue: [
            { code: "A", name: "Value A" },
            { code: "B", name: "Value B" },
          ],
          initialValue: "Value A",
          validations,
        },
      });

      await wrapper.setProps({ initialValue: value });

      expect(wrapper.get("#test").element.value).toEqual("");
    },
  );

  it("should validate and emit no error if required", async () => {
    const wrapper = mount(DropdownInputComponent, {
      props: {
        id: "test",
        label: "test",
        tip: "",
        modelValue: [
          { code: "A", name: "Value A" },
          { code: "B", name: "Value B" },
        ],
        initialValue: "",
        validations,
      },
    });

    const dropdown = wrapper.find("cds-dropdown");

    await dropdown.trigger("cds-dropdown-selected", eventContent("Value B"));

    expect(wrapper.emitted("update:modelValue")).toBeTruthy();
    expect(wrapper.emitted("update:modelValue")![0][0]).toBe("B");

    expect(wrapper.emitted("update:selectedValue")).toBeTruthy();
    expect(wrapper.emitted("update:selectedValue")![0][0]).toStrictEqual({
      code: "B",
      name: "Value B",
    });

    expect(wrapper.emitted("error")).toBeTruthy();
    expect(wrapper.emitted("error")![0][0]).toBe(undefined);
  });

  it("should reset selected to initial value when list change", async () => {
    const wrapper = mount(DropdownInputComponent, {
      props: {
        id: "test",
        label: "test",
        tip: "",
        modelValue: [
          { code: "A", name: "Value A" },
          { code: "B", name: "Value B" },
        ],
        initialValue: "",
        validations: [],
      },
    });

    const dropdown = wrapper.find("cds-dropdown");

    await dropdown.trigger("cds-dropdown-selected", eventContent("Value A"));

    expect(wrapper.emitted("update:modelValue")).toBeTruthy();
    expect(wrapper.emitted("update:modelValue")![0][0]).toBe("A");

    expect(wrapper.emitted("update:selectedValue")).toBeTruthy();
    expect(wrapper.emitted("update:selectedValue")![0][0]).toStrictEqual({
      code: "A",
      name: "Value A",
    });

    await wrapper.setProps({
      modelValue: [
        { code: "C", name: "Value C" },
        { code: "D", name: "Value D" },
      ],
    });

    expect(wrapper.emitted("update:modelValue")).toBeTruthy();
    expect(wrapper.emitted("update:modelValue")![1][0]).toBe("");

    expect(wrapper.emitted("update:selectedValue")).toBeTruthy();
    expect(wrapper.emitted("update:selectedValue")![1][0]).toStrictEqual({
      code: "",
      name: "",
    });
  });

  it("should emit the whole error object when the error returned is an object", async () => {
    const wrapper = mount(DropdownInputComponent, {
      props: {
        id: "test",
        label: "test",
        tip: "",
        modelValue: [
          { code: "A", name: "Value A" },
          { code: "B", name: "Value B" },
        ],
        initialValue: "",
        validations: objectErrorValidations,
      },
    });

    const dropdown = wrapper.find("cds-dropdown");

    await dropdown.trigger("cds-dropdown-selected", eventContent("Value A"));

    expect(wrapper.emitted("error")).toBeTruthy();
    expect(wrapper.emitted("error")![0][0]).toBe(objectError);
  });
});
