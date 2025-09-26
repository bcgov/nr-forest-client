import { describe, it, expect, afterEach, beforeEach } from "vitest";
import { DOMWrapper, mount, VueWrapper } from "@vue/test-utils";
import DateInputComponent from "@/components/forms/DateInputComponent/index.vue";
import { isDateInThePast, isMinimumYearsAgo } from "@/helpers/validators/GlobalValidators";

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

    const inputWrapper = wrapper.find<HTMLInputElement>(`#${id}Year`);
    await setInputValue(inputWrapper, "1");
    await inputWrapper.trigger("blur");

    expect(wrapper.emitted("error")).toBeTruthy();
    expect(wrapper.emitted("error")![0][0]).toBe("Year must have 4 digits");
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

  it("emits possibly-valid true when focused part is not empty and the two other parts are valid", async () => {
    const wrapper = mount(DateInputComponent, {
      props: {
        id,
        title: "My date",
        modelValue: "2000-12-", // missing the Day part
        validations: [],
        enabled: true,
      },
      directives: {
        masked: () => {},
      },
      attachTo: document.body,
    });
    globalWrapper = wrapper;

    const inputWrapper = wrapper.find<HTMLInputElement>(`#${id}Day`);
    await setInputValue(inputWrapper, "1");

    expect(wrapper.emitted("possibly-valid")).toBeTruthy();
    expect(wrapper.emitted("possibly-valid")![0][0]).toBe(true);
  });

  it("emits possibly-valid false when the focused part gets cleared", async () => {
    const wrapper = mount(DateInputComponent, {
      props: {
        id,
        title: "My date",
        modelValue: "2000-12-25",
        validations: [],
        enabled: true,
      },
      directives: {
        masked: () => {},
      },
      attachTo: document.body,
    });
    globalWrapper = wrapper;

    const inputWrapper = wrapper.find<HTMLInputElement>(`#${id}Day`);
    await setInputValue(inputWrapper, ""); // clears the Day part

    expect(wrapper.emitted("possibly-valid")).toBeTruthy();
    expect(wrapper.emitted("possibly-valid")![0][0]).toBe(false);
  });

  describe("when it's not a required field", () => {
    beforeEach(() => {
      const wrapper = mount(DateInputComponent, {
        props: {
          id,
          title: "My date",
          modelValue: "2000--",
          validations: [],
          enabled: true,
          required: false,
        },
        directives: {
          masked: () => {},
        },
        attachTo: document.body,
      });
      globalWrapper = wrapper;
    });

    it("emits possibly-valid true when it gets all emptied", async () => {
      const wrapper = globalWrapper;

      const inputWrapper = wrapper.find<HTMLInputElement>(`#${id}Year`);
      await setInputValue(inputWrapper, "");

      expect(wrapper.emitted("possibly-valid")).toBeTruthy();
      expect(wrapper.emitted("possibly-valid")![0][0]).toBe(true);
    });
  });

  describe("when it's a required field", () => {
    beforeEach(() => {
      const wrapper = mount(DateInputComponent, {
        props: {
          id,
          title: "My date",
          modelValue: "2000--",
          validations: [],
          enabled: true,
          required: true,
        },
        directives: {
          masked: () => {},
        },
        attachTo: document.body,
      });
      globalWrapper = wrapper;
    });

    it("emits possibly-valid false when it gets all emptied", async () => {
      const wrapper = globalWrapper;
      const inputWrapper = wrapper.find<HTMLInputElement>(`#${id}Year`);
      await setInputValue(inputWrapper, "");

      expect(wrapper.emitted("possibly-valid")).toBeTruthy();
      expect(wrapper.emitted("possibly-valid")![0][0]).toBe(false);
    });
  });

  describe.each([
    {
      partName: "Year",
      partIndex: 0,
      expectedLength: 4,
      validBeginning: "20019",
      invalidBeginning: "21019",
    },
    {
      partName: "Month",
      partIndex: 1,
      expectedLength: 2,
      validBeginning: "120",
      invalidBeginning: "130",
    },
    {
      partName: "Day",
      partIndex: 2,
      expectedLength: 2,
      validBeginning: "250",
      invalidBeginning: "400",
    },
  ])("when a value that doesn't fit the $partName part is pasted on it", (scenario) => {
    const baseValue = "2000-11-24";
    const test = async (valid: boolean) => {
      const inputWrapper = globalWrapper.find<HTMLInputElement>(`#${id}${scenario.partName}`);
      await inputWrapper.trigger("blur");

      const baseParts = baseValue.split("-");
      const { validBeginning, invalidBeginning } = scenario;
      const newPartValue = valid ? validBeginning : invalidBeginning;

      // first expected number of digits only
      baseParts[scenario.partIndex] = newPartValue.slice(0, scenario.expectedLength);

      const expectedModelValue = baseParts.join("-");

      expect(globalWrapper.emitted("update:model-value")).toBeTruthy();
      const lastUpdateEvent = globalWrapper.emitted("update:model-value").slice(-1)[0];

      expect(lastUpdateEvent[0]).toEqual(expectedModelValue);

      expect(globalWrapper.emitted("error")).toBeTruthy();
      const lastErrorEvent = globalWrapper.emitted("error").slice(-1)[0];

      if (valid) {
        expect(lastErrorEvent[0]).toBeFalsy();
      } else {
        expect(lastErrorEvent[0]).toBeTruthy();
        expect(lastErrorEvent[0]).toEqual(expect.any(String));
      }
    };
    beforeEach(() => {
      const baseParts = baseValue.split("-");
      baseParts[scenario.partIndex] = ""; // clear the part to be tested
      const modelValue = baseParts.join("-");
      const wrapper = mount(DateInputComponent, {
        props: {
          id,
          title: "My date",
          modelValue,
          validations: [isDateInThePast("sample error message")],
          enabled: true,
        },
        directives: {
          masked: () => {},
        },
        attachTo: document.body,
      });
      globalWrapper = wrapper;
    });
    describe(`but the first ${scenario.expectedLength} digits are valid`, () => {
      beforeEach(async () => {
        const inputWrapper = globalWrapper.find<HTMLInputElement>(`#${id}${scenario.partName}`);
        await setInputValue(inputWrapper, scenario.validBeginning);
      });
      it("updates the modelValue properly and emits error falsy (valid) after blurring", async () => {
        test(true);
      });
    });
    describe(`and the first ${scenario.expectedLength} digits are invalid`, () => {
      beforeEach(async () => {
        const inputWrapper = globalWrapper.find<HTMLInputElement>(`#${id}${scenario.partName}`);
        await setInputValue(inputWrapper, scenario.invalidBeginning);
      });
      it("updates the modelValue properly and emits error truthy (invalid) after blurring", async () => {
        test(false);
      });
    });
  });
});
