import { describe, it, expect } from "vitest";
import { DOMWrapper, mount } from "@vue/test-utils";
import TextareaInputComponent from "@/components/forms/TextareaInputComponent.vue";
import type { CDSTextarea } from "@carbon/web-components";
import { isMinSize } from "@/helpers/validators/GlobalValidators";
import { h } from "vue";

describe("Textarea Input Component", () => {
  const id = "my-input";
  const validations = [(value: any) => (value ? "" : "Field is required")];

  const setInputValue = async (inputWrapper: DOMWrapper<HTMLInputElement>, value: string) => {
    inputWrapper.element.value = value;
    await inputWrapper.trigger("input");
  };

  it("renders the input field with the provided id", () => {
    const wrapper = mount(TextareaInputComponent, {
      props: {
        id,
        label: "TestField",
        placeholder: "",
        modelValue: "",
        validations: [],
        enabled: true,
      },
    });

    expect(wrapper.find(`#${id}`).exists()).toBe(true);
  });

  it('emits the "update" event with the updated value', async () => {
    const wrapper = mount(TextareaInputComponent, {
      props: {
        id,
        label: "TestField",
        placeholder: "",
        modelValue: "",
        validations: [],
        enabled: true,
      },
    });

    const inputWrapper = wrapper.find<HTMLInputElement>(`#${id}`);
    await setInputValue(inputWrapper, "John Doe");
    await inputWrapper.trigger("blur");

    expect(wrapper.emitted("update:model-value")).toBeTruthy();
    expect(wrapper.emitted("update:model-value")![0][0]).toEqual("John Doe");
  });

  it('emits the "error" event when there is a validation error', async () => {
    const wrapper = mount(TextareaInputComponent, {
      props: {
        id,
        label: "TestField",
        placeholder: "",
        modelValue: "",
        validations,
        enabled: true,
      },
    });

    await wrapper.find(`#${id}`).trigger("input");
    await wrapper.find(`#${id}`).trigger("blur");

    expect(wrapper.emitted("error")).toBeTruthy();
    expect(wrapper.emitted("error")![0][0]).toBe("Field is required");
  });

  it("revalidates when changed from outside (i.e. not directly by the user)", async () => {
    const errorMessage = "sample error message";
    const wrapper = mount(TextareaInputComponent, {
      props: {
        id,
        label: "TestField",
        placeholder: "",
        modelValue: "",
        validations: [isMinSize(errorMessage)(5)],
        enabled: true,
      },
    });

    /*
    Note: we are not triggerring "input" nor "blur".
    Just like on a programmatic change, which was not performed by a user.
    */
    await wrapper.setProps({ modelValue: "1" });

    expect(wrapper.emitted("error")).toBeTruthy();
    expect(wrapper.emitted("error")).toHaveLength(1);
    expect(wrapper.emitted("error")![0][0]).toBe(errorMessage);
  });

  it('emits the "error" event even when the error message is the same as before', async () => {
    const errorMessage = "sample error message";
    const wrapper = mount(TextareaInputComponent, {
      props: {
        id,
        label: "TestField",
        placeholder: "",
        modelValue: "",
        validations: [isMinSize(errorMessage)(5)],
        enabled: true,
      },
    });

    const inputWrapper = wrapper.find<HTMLInputElement>(`#${id}`);
    await setInputValue(inputWrapper, "1");
    await inputWrapper.trigger("blur");

    expect(wrapper.emitted("error")).toBeTruthy();
    expect(wrapper.emitted("error")).toHaveLength(1);
    expect(wrapper.emitted("error")![0][0]).toBe(errorMessage);

    await setInputValue(inputWrapper, "12"); // adds another character
    await inputWrapper.trigger("blur");

    // Just a sanity check on the updated element value
    expect(inputWrapper.element.value).toEqual("12");

    expect(wrapper.emitted("error")).toHaveLength(2);
    expect(wrapper.emitted("error")![1][0]).toBe(errorMessage);
  });

  it('emits the "empty" event when the input field is empty', async () => {
    const wrapper = mount(TextareaInputComponent, {
      props: {
        id,
        label: "TestField",
        placeholder: "",
        modelValue: "",
        validations: [],
        enabled: true,
      },
    });

    await wrapper.find(`#${id}`).trigger("input");
    await wrapper.find(`#${id}`).trigger("blur");

    expect(wrapper.emitted("empty")).toBeTruthy();
    expect(wrapper.emitted("empty")![0][0]).toBe(true);
  });

  it("sets the properties enableCounter, maxCount and rows in the cds-textarea properly", () => {
    const enableCounter = true;
    const maxCount = 42;
    const rows = 9;

    const wrapper = mount(TextareaInputComponent, {
      props: {
        id,
        enableCounter,
        maxCount,
        rows,
        label: "TestField",
        placeholder: "",
        modelValue: "",
        validations: [],
        enabled: true,
      },
    });

    const cdsTextarea = wrapper.find<CDSTextarea>("cds-textarea");

    expect(cdsTextarea.element.enableCounter).toEqual(enableCounter);
    expect(cdsTextarea.element.maxCount).toEqual(maxCount);
    expect(cdsTextarea.element.rows).toEqual(rows);
  });

  it("renders the provided children elements within the cds-textarea", () => {
    const wrapper = mount(TextareaInputComponent, {
      props: {
        id,
        label: "TestField",
        placeholder: "",
        modelValue: "",
        validations: [],
        enabled: true,
      },
      slots: {
        default: h("div", { id: "cds-textarea-child" }), // renders a <div id="cds-textarea-child"></div>
      },
    });

    const cdsTextarea = wrapper.find<CDSTextarea>("cds-textarea");
    const testChild = cdsTextarea.find("div#cds-textarea-child");

    expect(testChild.exists()).toBe(true);
  });
});
