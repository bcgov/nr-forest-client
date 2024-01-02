import { describe, it, expect, afterEach } from "vitest";
import { DOMWrapper, mount, VueWrapper } from "@vue/test-utils";
import DateInputComponent from "@/components/forms/DateInputComponent/index.vue";
import { isMinimumYearsAgo } from "@/helpers/validators/GlobalValidators";

describe("Date Input Component", () => {
  let globalWrapper: VueWrapper;

  afterEach(() => {
    if (globalWrapper) {
      globalWrapper.unmount();
    }
  });

  const id = "my-date";

  const setInputValue = async (
    inputWrapper: DOMWrapper<HTMLInputElement>,
    value: string
  ) => {
    inputWrapper.element.value = value;
    await inputWrapper.trigger("input");
  };

  it("renders the input field with the provided id", () => {
    const wrapper = mount(DateInputComponent, {
      props: {
        id,
        title: "My date",
        modelValue: "",
        validations: [],
        enabled: true,
      },
      directives: {
        masked: () => {},
      },
    });
    globalWrapper = wrapper;

    expect(wrapper.find(`#${id}`).exists()).toBe(true);
  });

  it('emits the "update" event with the updated value', async () => {
    const wrapper = mount(DateInputComponent, {
      props: {
        id,
        title: "My date",
        modelValue: "",
        validations: [],
        enabled: true,
      },
      directives: {
        masked: () => {},
      },
      attachTo: document.body,
    });
    globalWrapper = wrapper;

    const yearWrapper = wrapper.find<HTMLInputElement>(`#${id}Year`);
    await setInputValue(yearWrapper, "2001");
    await yearWrapper.trigger("blur");

    expect(wrapper.emitted("update:model-value")).toBeTruthy();
    expect(wrapper.emitted("update:model-value")![0][0]).toEqual("2001--");
  });

  it('emits the "error" event when there is a validation error', async () => {
    const wrapper = mount(DateInputComponent, {
      props: {
        id,
        title: "My date",
        modelValue: "",
        validations: [],
        enabled: true,
      },
      directives: {
        masked: () => {},
      },
      attachTo: document.body,
    });
    globalWrapper = wrapper;

    await wrapper.find(`#${id}Year`).trigger("input");
    await wrapper.find(`#${id}Year`).trigger("blur");

    expect(wrapper.emitted("error")).toBeTruthy();
    expect(wrapper.emitted("error")![0][0]).toBe("My date must include a year");
  });

  it("revalidates when changed from outside (i.e. not directly by the user)", async () => {
    const errorMessage = "sample error message";

    const wrapper = mount(DateInputComponent, {
      props: {
        id,
        title: "My date",
        modelValue: "",
        validations: [isMinimumYearsAgo(10, errorMessage)],
        enabled: true,
      },
      directives: {
        masked: () => {},
      },
      attachTo: document.body,
    });
    globalWrapper = wrapper;

    const currentYear = new Date().getFullYear();
    const currentYearMinus5 = currentYear - 5;

    /*
    Note: we are not triggerring "input" nor "blur".
    Just like on a programmatic change, which was not performed by a user.
    */
    await wrapper.setProps({ modelValue: `${currentYearMinus5}-03-25` });

    expect(wrapper.emitted("error")).toBeTruthy();
    expect(wrapper.emitted("error")).toHaveLength(1);
    expect(wrapper.emitted("error")![0][0]).toBe(errorMessage);
  });

  it('emits the "error" event even when the error message is the same as before', async () => {
    const errorMessage = "Year must have 4 digits";
    const wrapper = mount(DateInputComponent, {
      props: {
        id,
        title: "My date",
        modelValue: "",
        validations: [],
        enabled: true,
      },
      directives: {
        masked: () => {},
      },
      attachTo: document.body,
    });
    globalWrapper = wrapper;

    const inputWrapper = wrapper.find<HTMLInputElement>(`#${id}Year`);
    await setInputValue(inputWrapper, "1");
    await inputWrapper.trigger("blur");

    expect(wrapper.emitted("error")).toBeTruthy();
    expect(wrapper.emitted("error")).toHaveLength(1);
    expect(wrapper.emitted("error")![0][0]).toBe(errorMessage);

    await setInputValue(inputWrapper, "19"); // adds another character
    await inputWrapper.trigger("blur");

    // Just a sanity check on the updated element value
    expect(inputWrapper.element.value).toEqual("19");

    expect(wrapper.emitted("error")).toHaveLength(2);
    expect(wrapper.emitted("error")![1][0]).toBe(errorMessage);
  });

  it('emits the "empty" event when the input field is empty', async () => {
    const wrapper = mount(DateInputComponent, {
      props: {
        id,
        title: "My date",
        modelValue: "",
        validations: [],
        enabled: true,
      },
      directives: {
        masked: () => {},
      },
      attachTo: document.body,
    });
    globalWrapper = wrapper;

    const inputWrapper = wrapper.find<HTMLInputElement>(`#${id}Year`);
    await inputWrapper.trigger("input");
    await inputWrapper.trigger("blur");

    expect(wrapper.emitted("empty")).toBeTruthy();
    expect(wrapper.emitted("empty")![0][0]).toBe(true);
  });
});
