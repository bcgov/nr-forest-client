import { describe, it, expect, beforeEach } from "vitest";

import { VueWrapper, mount as baseMount, type DOMWrapper } from "@vue/test-utils";
import AutoCompleteInputComponent from "@/components/forms/AutoCompleteInputComponent.vue";
import { isMinSize } from "@/helpers/validators/GlobalValidators";

describe("Auto Complete Input Component", () => {
  const id = "my-input";
  const validations = [(value: any) => (value ? "" : "Field is required")];
  const contents = [
    { code: "TA", name: "TANGO" },
    { code: "TB", name: "TAMBOR" },
    { code: "TC", name: "TAMCADA" },
    { code: "TD", name: "TADANARA" },
  ];

  const eventSelectContent = (value: string) => {
    return {
      detail: {
        item: { "data-id": value, getAttribute: (key: string) => value },
      },
    };
  };

  const eventClearContent = () => {
    return {
      detail: {
        item: undefined,
      },
    };
  };

  const setInputValue = async (inputWrapper: DOMWrapper<HTMLInputElement>, value: string) => {
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    inputWrapper.element._filterInputValue = value;

    await inputWrapper.trigger("input");
  };

  const selectItemWithKeyboard = async (
    inputWrapper: DOMWrapper<HTMLInputElement>,
    value: string,
  ) => {
    /*
    This reproduces artificially what happens when an item is selected using the keyboard.
    A cds-combo-box-beingselected happens before the corresponding keypress event.
    */
    await inputWrapper.trigger("cds-combo-box-beingselected", eventSelectContent(value));

    await inputWrapper.trigger("keypress.enter");
  };

  const selectItemWithMouse = async (inputWrapper: DOMWrapper<HTMLInputElement>, value: string) => {
    /*
    This reproduces artificially what happens when an item is selected using the mouse.
    The click event on the item happens before the cds-combo-box-beingselected.
    */
    await inputWrapper.find(`cds-combo-box-item[data-id="${value}"]`).trigger("click");

    await inputWrapper.trigger("cds-combo-box-beingselected", eventSelectContent(value));
  };

  const mount: typeof baseMount = function (inputComponent, options) {
    const wrapper = baseMount(inputComponent, options);
    const comboBox = wrapper.find(`#${id}`);
    comboBox.element.addEventListener("cds-combo-box-beingselected", async (event: any) => {
      if (!event.defaultPrevented) {
        // propagates the event as it would happen normally
        await comboBox.trigger("cds-combo-box-selected", { detail: event?.detail });
      }
    });
    return wrapper;
  };

  it("renders the input field with the provided id", () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        contents,
        validations: [],
        label: id,
      },
    });

    expect(wrapper.find(`#${id}`).exists()).toBe(true);
  });

  it('emits the "update" event with the updated value', async () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        contents,
        validations: [],
        label: id,
        tip: "",
      },
    });

    await wrapper.setProps({ modelValue: "TANGO" });
    await wrapper.find(`#${id}`).trigger("blur");
    await wrapper.find(`#${id}`).trigger("input");

    expect(wrapper.emitted("update:model-value")).toBeTruthy();
    expect(wrapper.emitted("update:model-value")![0][0]).toEqual("TANGO");
  });

  it('emits the "error" event when there is a validation error', async () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        contents,
        validations,
        label: id,
      },
    });

    await wrapper.find(`#${id}`).trigger("input");
    await wrapper.find(`#${id}`).trigger("blur");

    expect(wrapper.emitted("error")).toBeTruthy();
    expect(wrapper.emitted("error")![0][0]).toBe("Field is required");
  });

  it('emits the "error" event even when the error message is the same as before', async () => {
    const errorMessage = "sample error message";
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        contents,
        validations: [isMinSize(errorMessage)(5)],
        label: id,
      },
    });

    await setInputValue(wrapper.find(`#${id}`), "a");
    await wrapper.find(`#${id}`).trigger("blur");

    expect(wrapper.emitted("error")).toBeTruthy();
    expect(wrapper.emitted("error")).toHaveLength(1);
    expect(wrapper.emitted("error")![0][0]).toBe(errorMessage);

    await setInputValue(wrapper.find(`#${id}`), "ab"); // adds another character
    await wrapper.find(`#${id}`).trigger("blur");

    expect(wrapper.emitted("error")).toHaveLength(2);
    expect(wrapper.emitted("error")![1][0]).toBe(errorMessage);
  });

  describe("when validationsOnChange is falsy", () => {
    let wrapper: VueWrapper;
    const errorMessage = "sample error message";
    beforeEach(() => {
      wrapper = mount(AutoCompleteInputComponent, {
        props: {
          id,
          modelValue: "",
          contents,
          validations: [isMinSize(errorMessage)(5)],
          label: id,
        },
      });
    });
    it("doesn't validate while the field has focus", async () => {
      await setInputValue(wrapper.find(`#${id}`), "a");

      expect(wrapper.emitted("error")).toBeFalsy();
    });
    it("validates when the field loses focus", async () => {
      await setInputValue(wrapper.find(`#${id}`), "a");
      await wrapper.find(`#${id}`).trigger("blur");

      expect(wrapper.emitted("error")).toBeTruthy();
      expect(wrapper.emitted("error")![0][0]).toBe(errorMessage);
    });
  });
  describe("when validationsOnChange is true (boolean)", () => {
    let wrapper: VueWrapper;
    const errorMessage = "sample error message";
    beforeEach(() => {
      wrapper = mount(AutoCompleteInputComponent, {
        props: {
          id,
          modelValue: "",
          contents,
          validations: [isMinSize(errorMessage)(5)],
          validationsOnChange: true,
          label: id,
        },
      });
    });
    it("uses the same validations provided on prop.validations on every change", async () => {
      await setInputValue(wrapper.find(`#${id}`), "a");

      expect(wrapper.emitted("error")).toBeTruthy();
      expect(wrapper.emitted("error")![0][0]).toBe(errorMessage);
    });
    it("uses the provided validations on blur", async () => {
      await setInputValue(wrapper.find(`#${id}`), "a");
      await wrapper.find(`#${id}`).trigger("blur");

      expect(wrapper.emitted("error")).toBeTruthy();
      expect(wrapper.emitted("error")).toHaveLength(2);
      expect(wrapper.emitted("error")![0][0]).toBe(errorMessage);
      expect(wrapper.emitted("error")![1][0]).toBe(errorMessage);
    });
  });
  describe("when validationsOnChange is an array", () => {
    let wrapper: VueWrapper;
    const validationsMessage = "validations message";
    const validationsOnChangeMessage = "validationsOnChange message";
    beforeEach(() => {
      wrapper = mount(AutoCompleteInputComponent, {
        props: {
          id,
          modelValue: "",
          contents,
          validations: [isMinSize(validationsMessage)(5)],
          validationsOnChange: [isMinSize(validationsOnChangeMessage)(5)],
          label: id,
        },
      });
    });
    it("uses the provided validationsOnChange on every change", async () => {
      await setInputValue(wrapper.find(`#${id}`), "a");

      expect(wrapper.emitted("error")).toBeTruthy();
      expect(wrapper.emitted("error")![0][0]).toBe(validationsOnChangeMessage);
    });
    it("uses the provided validations on blur", async () => {
      await setInputValue(wrapper.find(`#${id}`), "a");
      await wrapper.find(`#${id}`).trigger("blur");

      expect(wrapper.emitted("error")).toBeTruthy();
      expect(wrapper.emitted("error")).toHaveLength(2);
      expect(wrapper.emitted("error")![0][0]).toBe(validationsOnChangeMessage);
      expect(wrapper.emitted("error")![1][0]).toBe(validationsMessage);
    });
  });

  it('emits the "empty" event when the input field is empty', async () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        contents,
        validations: [],
        label: id,
      },
    });

    await wrapper.find(`#${id}`).trigger("input");
    await wrapper.find(`#${id}`).trigger("blur");

    expect(wrapper.emitted("empty")).toBeTruthy();
    expect(wrapper.emitted("empty")![0][0]).toBe(true);
  });

  it('emits the "empty" event with false when the input field is not empty', async () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        contents,
        validations: [],
        label: id,
      },
    });

    await setInputValue(wrapper.find(`#${id}`), "a");
    await wrapper.find(`#${id}`).trigger("blur");

    expect(wrapper.emitted("empty")).toHaveLength(2);
    expect(wrapper.emitted("empty")![1][0]).toBe(false);
  });

  describe("when the contents prop is empty", () => {
    const emptyContents: any[] = [];
    it('emits "empty" when the input becomes empty', async () => {
      const wrapper = mount(AutoCompleteInputComponent, {
        props: {
          id,
          modelValue: "a",
          contents: emptyContents,
          validations: [],
          label: id,
        },
      });

      await setInputValue(wrapper.find(`#${id}`), "");

      expect(wrapper.emitted("empty")).toBeTruthy();

      // the first event is from the initialization
      expect(wrapper.emitted("empty")![0][0]).toBe(false);

      // the second event is the result from changing the input value
      // Note: before FSADT1-912, it would emit a "not empty" (false) one.
      expect(wrapper.emitted("empty")![1][0]).toBe(true);

      expect(wrapper.emitted("empty")).toHaveLength(2);
    });
  });

  it('emits the "select:item" event with the code of the clicked option and selects it', async () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        contents,
        validations: [],
        label: id,
        tip: "",
      },
    });

    await wrapper.setProps({ modelValue: "T" });
    await wrapper.find(`#${id}`).trigger("input");

    const code = "TB";
    await wrapper.find(`#${id}`).trigger("cds-combo-box-beingselected", eventSelectContent(code));

    expect(wrapper.emitted("select:item")).toBeTruthy();
    expect(wrapper.emitted("select:item")![0][0]).toEqual(code);

    expect(wrapper.emitted("update:selected-value")).toBeTruthy();

    const selectedContent = contents.find((value) => value.code === code);
    expect(wrapper.emitted("update:selected-value")![0][0]).toEqual(selectedContent);
  });

  describe("when preventSelection is true", () => {
    let wrapper: VueWrapper;
    beforeEach(() => {
      wrapper = mount(AutoCompleteInputComponent, {
        props: {
          id,
          modelValue: "",
          contents,
          validations: [],
          label: id,
          tip: "",
          preventSelection: true,
        },
      });
    });

    it('emits the "select:item" event with the code of the clicked option and prevents selecting it', async () => {
      await wrapper.setProps({ modelValue: "T" });
      await wrapper.find(`#${id}`).trigger("input");

      const code = "TB";
      await wrapper.find(`#${id}`).trigger("cds-combo-box-beingselected", eventSelectContent(code));

      expect(wrapper.emitted("select:item")).toBeTruthy();
      expect(wrapper.emitted("select:item")![0][0]).toEqual(code);

      expect(wrapper.emitted("update:selected-value")).toBeFalsy();
    });

    it("doesn't prevent the clearing action", async () => {
      await wrapper.setProps({ modelValue: "T" });
      await wrapper.find(`#${id}`).trigger("input");

      // User initiated event to clear the value
      await wrapper.find(`#${id}`).trigger("cds-combo-box-beingselected", eventClearContent());

      // Clears the value
      expect(wrapper.emitted("update:selected-value")).toBeTruthy();
      expect(wrapper.emitted("update:selected-value")![0][0]).toEqual(undefined);
    });
  });

  it("sets the model value to an empty string, not undefined", async () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        contents,
        validations: [],
        label: id,
        tip: "",
      },
    });

    await wrapper.setProps({ modelValue: "T" });
    await wrapper.find(`#${id}`).trigger("input");

    // User initiated event to clear the value
    await wrapper.find(`#${id}`).trigger("cds-combo-box-beingselected", eventClearContent());

    expect(wrapper.emitted("update:model-value")).toBeTruthy();
    expect(wrapper.emitted("update:model-value")).toHaveLength(2);
    expect(wrapper.emitted("update:model-value")![1][0]).toEqual("");
  });

  it('emits the "update:selected-value" event when an option from the list is clicked', async () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        contents,
        validations: [],
        label: id,
      },
    });

    await wrapper
      .find(`#${id}`)
      .trigger("cds-combo-box-selected", eventSelectContent("TA"));
    expect(wrapper.emitted("update:selected-value")).toBeTruthy();
    expect(wrapper.emitted("update:selected-value")![0][0]).toEqual(
      contents[0]
    );
  });

  describe("when an option was previously selected", () => {
    let wrapper: VueWrapper<any, any>;

    beforeEach(async () => {
      wrapper = mount(AutoCompleteInputComponent, {
        props: {
          id,
          modelValue: "",
          contents,
          validations: [],
          label: id,
        },
      });

      await wrapper
        .find(`#${id}`)
        .trigger("cds-combo-box-selected", eventSelectContent(contents[0].code));

      // Now an option is effectively selected
      expect(wrapper.emitted("update:selected-value")).toBeTruthy();
      expect(wrapper.emitted("update:selected-value")![0][0]).toEqual(
        contents[0]
      );
    });

    it('emits the "update:selected-value" with undefined when user types something in the input field', async () => {
      // adding a 'Z' character to the end of the original value so to trigger an update:model-value
      await setInputValue(wrapper.find(`#${id}`), "TANGOZ");

      expect(wrapper.emitted("update:selected-value")).toHaveLength(2);
      expect(wrapper.emitted("update:selected-value")![1][0]).toEqual(
        undefined
      );
    });

    it('doesn\'t emit the "update:selected-value" when the value has been changed from outside', async () => {
      const expectedCount = 1;

      // Expected count before the update
      expect(wrapper.emitted("update:selected-value")).toHaveLength(expectedCount);

      // adding a 'Z' character to the end of the original value so to trigger an update:model-value
      await wrapper.setProps({ modelValue: "TANGOZ" });

      // Expected count after the update
      expect(wrapper.emitted("update:selected-value")).toHaveLength(expectedCount);
    });

    it('emits the "update:selected-value" with the newly selected value', async () => {
      await wrapper
        .find(`#${id}`)
        .trigger("cds-combo-box-selected", eventSelectContent(contents[1].code));

      expect(wrapper.emitted("update:selected-value")).toHaveLength(2);
      expect(wrapper.emitted("update:selected-value")![1][0]).toEqual(
        contents[1]
      );
    });
  });

  it('emits the "press:enter" event', async () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        contents,
        validations: [],
        label: id,
      },
    });

    await wrapper.find(`#${id}`).trigger("keypress.enter");

    expect(wrapper.emitted("press:enter")).toHaveLength(1);
  });

  it("doesn't emit the \"press:enter\" event when it's a selection with the keyboard", async () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        contents,
        validations: [],
        label: id,
      },
    });

    selectItemWithKeyboard(wrapper.find(`#${id}`), "TB");

    expect(wrapper.emitted("press:enter")).toBeFalsy();
  });

  describe("when user selects an item using the enter key on the keyboard", () => {
    let wrapper: VueWrapper<any, any>;

    beforeEach(async () => {
      wrapper = mount(AutoCompleteInputComponent, {
        props: {
          id,
          modelValue: "",
          contents,
          validations: [],
          label: id,
        },
      });

      selectItemWithKeyboard(wrapper.find(`#${id}`), "TB");

      // sanity check
      expect(wrapper.emitted("press:enter")).toBeFalsy();
    });

    describe("and later hits enter on the input field", () => {
      beforeEach(async () => {
        await wrapper.find(`#${id}`).trigger("keypress.enter");
      });

      it('emits the "press:enter" event', async () => {
        expect(wrapper.emitted("press:enter")).toHaveLength(1);
      });
    });
  });

  describe("when user selects an item with a click", () => {
    let wrapper: VueWrapper<any, any>;

    beforeEach(async () => {
      wrapper = mount(AutoCompleteInputComponent, {
        props: {
          id,
          modelValue: "",
          contents,
          validations: [],
          label: id,
        },
      });

      selectItemWithMouse(wrapper.find(`#${id}`), "TA");
    });

    describe("and later hits enter on the input field", () => {
      beforeEach(async () => {
        await wrapper.find(`#${id}`).trigger("keypress.enter");
      });

      it('emits the "press:enter" event', async () => {
        expect(wrapper.emitted("press:enter")).toHaveLength(1);
      });
    });
  });

  it("renders the helper-text as supplied in the tip slot", async () => {
    const wrapper = mount(AutoCompleteInputComponent, {
      props: {
        id,
        modelValue: "",
        contents,
        validations: [],
        label: id,
      },
      slots: {
        tip: "Custom tip text",
      },
    });

    expect(wrapper.find("[slot='helper-text']").text()).toContain("Custom tip text");
  });
});
