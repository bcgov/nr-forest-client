import { describe, it, expect } from "vitest";
import { mount } from "@vue/test-utils";

import FormComponentOptions from "../../common/FormComponentOptions.vue";
import FormFieldTemplate from "../../common/FormFieldTemplate.vue";
import FormInput from "../../common/FormInput.vue";
import FormCheckbox from "../../common/FormCheckbox.vue";
import FormCheckboxGroup from "../../common/FormCheckboxGroup.vue";
import FormRadioGroup from "../../common/FormRadioGroup.vue";
import FormGroup from "../../common/FormGroup.vue";
import FormSelect from "../../common/FormSelect.vue";
import FormTable from "../../common/FormTable.vue";
import { testGroup, testTable } from "./TestConstant";

import type {
  FormComponentSchemaType,
  CommonObjectType,
} from "../../core/FormType";

describe("FormCoponentOptions", () => {
  it("component defined", () => {
    const wrapper = mount(FormComponentOptions, {
      props: {
        schema: {
          fieldProps: { id: "test" },
          type: "input",
        },
        data: "",
      },
    });
    expect(wrapper).toBeDefined();
  });

  it("renders input successfully", async () => {
    const schema: FormComponentSchemaType = {
      fieldProps: { label: "Title", id: "testInput" },
      type: "input",
    };
    const data: string = "";
    const wrapper = mount(FormComponentOptions, {
      props: { schema, data },
    });
    expect(wrapper.findComponent(FormFieldTemplate).exists()).toBe(true);
    expect(wrapper.findComponent(FormInput).exists()).toBe(true);
    expect(wrapper.text()).toContain("Title");

    const input = wrapper.find("input");
    expect(input.element.value).toBe("");
    await input.setValue("Test");
    expect(input.element.value).toBe("Test");
    // assert the emitted event has been performed
    expect(wrapper.emitted()).toHaveProperty("updateFormValue");

    let updateFormValueEvent = wrapper.emitted("updateFormValue");
    // updateValue has been called once when doing the input.setValue
    expect(updateFormValueEvent).toHaveLength(1);
    updateFormValueEvent = updateFormValueEvent || [];
    // test the given parameters
    expect(updateFormValueEvent[0]).toEqual(["Test", "testInput"]);
  });

  it("renders select successfully", async () => {
    const schema: FormComponentSchemaType = {
      fieldProps: { label: "Title", id: "testSelect" },
      type: "select",
      options: [
        { value: "1", text: "a" },
        { value: "2", text: "b" },
      ],
    };
    const data: string = "";
    const wrapper = mount(FormComponentOptions, {
      props: { schema, data },
    });
    expect(wrapper.findComponent(FormFieldTemplate).exists()).toBe(true);
    expect(wrapper.findComponent(FormSelect).exists()).toBe(true);
    expect(wrapper.text()).toContain("Title");

    expect(wrapper.findAll("option")).toHaveLength(2);
    expect(wrapper.findAll("option")[0].text()).toBe("a");
    expect(wrapper.findAll("option")[0].element.value).toBe("1");
    expect(wrapper.findAll("option")[1].text()).toBe("b");
    expect(wrapper.findAll("option")[1].element.value).toBe("2");

    const select = wrapper.find("select");
    expect(select.element.value).toBe("");
    await select.setValue("2");
    expect(select.element.value).toBe("2");
    // assert the emitted event has been performed
    expect(wrapper.emitted()).toHaveProperty("updateFormValue");

    let updateFormValueEvent = wrapper.emitted("updateFormValue");
    // updateValue has been called once when doing the input.setValue
    expect(updateFormValueEvent).toHaveLength(1);
    updateFormValueEvent = updateFormValueEvent || [];
    // test the given parameters
    expect(updateFormValueEvent[0]).toEqual(["2", "testSelect"]);
  });

  it("renders checkbox successfully", async () => {
    const schema: FormComponentSchemaType = {
      fieldProps: { label: "Title", id: "testCheckbox" },
      type: "checkbox",
    };
    const data: boolean = false;
    const wrapper = mount(FormComponentOptions, {
      props: { schema, data },
    });
    expect(wrapper.findComponent(FormFieldTemplate).exists()).toBe(true);
    expect(wrapper.findComponent(FormCheckbox).exists()).toBe(true);
    expect(wrapper.text()).toContain("Title");

    const label = wrapper.find("label");
    expect(label.classes("active")).toBe(false);

    // todo: check how to check/uncheck the checkbox
  });

  it("renders checkboxgroup successfully", async () => {
    const schema: FormComponentSchemaType = {
      fieldProps: { label: "Title", id: "testCheckBoxGroup" },
      type: "checkboxgroup",
      options: [
        { code: "1", text: "Option 1" },
        { code: "2", text: "Option 2" },
        { code: "3", text: "Option 3" },
      ],
    };
    const data: Array<string> = [];
    const wrapper = mount(FormComponentOptions, {
      props: { schema, data },
    });
    expect(wrapper.findComponent(FormFieldTemplate).exists()).toBe(true);
    expect(wrapper.findComponent(FormCheckboxGroup).exists()).toBe(true);
    expect(wrapper.text()).toContain("Title");

    expect(wrapper.findAll("label")).toHaveLength(3);
    expect(wrapper.findAll("input")).toHaveLength(3);
    expect(wrapper.findAll("label")[0].text()).toBe("Option 1");
    expect(wrapper.findAll("input")[0].element.value).toBe("1");
    expect(wrapper.findAll("label")[1].text()).toBe("Option 2");
    expect(wrapper.findAll("input")[1].element.value).toBe("2");
    expect(wrapper.findAll("label")[2].text()).toBe("Option 3");
    expect(wrapper.findAll("input")[2].element.value).toBe("3");

    // todo: check how to check/uncheck the checkbox and emit functions
  });

  it("renders radiogroup successfully", async () => {
    const schema: FormComponentSchemaType = {
      fieldProps: { label: "Title", id: "testRadioGroup" },
      type: "radiogroup",
      options: [
        { code: "1", text: "Option 1" },
        { code: "2", text: "Option 2" },
        { code: "3", text: "Option 3" },
      ],
    };
    const data: Array<string> = [];
    const wrapper = mount(FormComponentOptions, {
      props: { schema, data },
    });
    expect(wrapper.findComponent(FormFieldTemplate).exists()).toBe(true);
    expect(wrapper.findComponent(FormRadioGroup).exists()).toBe(true);
    expect(wrapper.text()).toContain("Title");

    expect(wrapper.findAll("label")).toHaveLength(3);
    expect(wrapper.findAll("input")).toHaveLength(3);
    expect(wrapper.findAll("label")[0].text()).toBe("Option 1");
    expect(wrapper.findAll("input")[0].element.value).toBe("1");
    expect(wrapper.findAll("label")[1].text()).toBe("Option 2");
    expect(wrapper.findAll("input")[1].element.value).toBe("2");
    expect(wrapper.findAll("label")[2].text()).toBe("Option 3");
    expect(wrapper.findAll("input")[2].element.value).toBe("3");

    const label = wrapper.findAll("label");
    expect(label[0].classes("active")).toBe(false);
    expect(label[1].classes("active")).toBe(false);
    expect(label[2].classes("active")).toBe(false);

    // todo: check how to check the radio and emit functions

    // await wrapper.findAll('input[type="radio"]')[0].trigger("click");
    // expect(label[0].classes("active")).toBe(true);
  });

  it("renders group successfully", async () => {
    const wrapper = mount(FormComponentOptions, {
      props: { schema: testGroup.schema, data: testGroup.data },
    });

    expect(wrapper.findComponent(FormFieldTemplate).exists()).toBe(true);
    expect(wrapper.findComponent(FormGroup).exists()).toBe(true);
    expect(wrapper.findComponent(FormComponentOptions).exists()).toBe(true);
    expect(wrapper.findComponent(FormInput).exists()).toBe(true);
    expect(wrapper.findComponent(FormCheckboxGroup).exists()).toBe(true);

    expect(wrapper.text()).toContain("Title");

    const cards = wrapper.findAll(".card");
    expect(cards).toHaveLength(2);
    const inputs = wrapper.findAll("input[type='text']");
    expect(inputs).toHaveLength(2);
    const checkboxes = wrapper.findAll("input[type='checkbox']");
    expect(checkboxes).toHaveLength(6);

    await wrapper.find("#groupAddButton").trigger("click");
    expect(wrapper.emitted()).toHaveProperty("addRow");

    let addRowEvent = wrapper.emitted("addRow");
    expect(addRowEvent).toHaveLength(1);
    // todo: test how to pass emit functions

    await wrapper.find("#groupDeleteButton").trigger("click");
    expect(wrapper.emitted()).toHaveProperty("deleteRow");
    // todo: test how to pass emit functions

    await inputs[0].setValue("Test");
    expect(inputs[0].element.value).toBe("Test");
    expect(wrapper.emitted()).toHaveProperty("updateFormArrayValue");

    let updateFormArrayValueEvent = wrapper.emitted("updateFormArrayValue");
    expect(updateFormArrayValueEvent).toHaveLength(1);
    updateFormArrayValueEvent = updateFormArrayValueEvent || [];
    expect(updateFormArrayValueEvent[0]).toEqual(["Test", "0.groupInput"]);
  });

  it("renders table successfully", async () => {
    const wrapper = mount(FormComponentOptions, {
      props: { schema: testTable.schema, data: testTable.data },
    });

    expect(wrapper.findComponent(FormFieldTemplate).exists()).toBe(true);
    expect(wrapper.findComponent(FormTable).exists()).toBe(true);
    expect(wrapper.findComponent(FormComponentOptions).exists()).toBe(true);
    expect(wrapper.findComponent(FormInput).exists()).toBe(true);
    expect(wrapper.findComponent(FormSelect).exists()).toBe(true);

    expect(wrapper.text()).toContain("Title");

    const tableRows = wrapper.findAll("tr[role='row']");
    expect(tableRows).toHaveLength(2);
    const inputs = wrapper.findAll("input[type='text']");
    expect(inputs).toHaveLength(2);
    const selects = wrapper.findAll("select");
    expect(selects).toHaveLength(2);

    await wrapper.find("#tableAddButton").trigger("click");
    expect(wrapper.emitted()).toHaveProperty("addRow");

    let addRowEvent = wrapper.emitted("addRow");
    expect(addRowEvent).toHaveLength(1);
    // todo: test how to pass emit functions

    await wrapper.find("#tableDeleteButton").trigger("click");
    expect(wrapper.emitted()).toHaveProperty("deleteRow");
    // todo: test how to pass emit functions

    await inputs[0].setValue("Test");
    expect(inputs[0].element.value).toBe("Test");
    expect(wrapper.emitted()).toHaveProperty("updateFormArrayValue");

    let updateFormArrayValueEvent = wrapper.emitted("updateFormArrayValue");
    expect(updateFormArrayValueEvent).toHaveLength(1);
    updateFormArrayValueEvent = updateFormArrayValueEvent || [];
    expect(updateFormArrayValueEvent[0]).toEqual(["Test", "0.tableInput"]);
  });

  it("renders disableAll of single field successfully", async () => {
    const schema: FormComponentSchemaType = {
      fieldProps: { label: "Title", id: "testInput" },
      type: "input",
    };
    const data: string = "";
    const wrapper = mount(FormComponentOptions, {
      props: { schema, data, disableAll: true },
    });
    expect(wrapper.findComponent(FormInput).exists()).toBe(true);

    const input = wrapper.find("input");
    expect(input.attributes().disabled).toBeDefined();
  });

  it("renders disabledFields of single field successfully", async () => {
    const schema: FormComponentSchemaType = {
      fieldProps: { label: "Title", id: "testInput" },
      type: "input",
    };
    const data: string = "";
    const wrapper = mount(FormComponentOptions, {
      props: { schema, data, disabledFields: ["testInput"] },
    });
    expect(wrapper.findComponent(FormInput).exists()).toBe(true);

    const input = wrapper.find("input");
    expect(input.attributes().disabled).toBeDefined();
  });

  it("renders disableAll of table fields successfully", async () => {
    const wrapper = mount(FormComponentOptions, {
      props: {
        schema: testTable.schema,
        data: testTable.data,
        disableAll: true,
      },
    });
    expect(wrapper.findComponent(FormTable).exists()).toBe(true);
    expect(wrapper.findComponent(FormComponentOptions).exists()).toBe(true);
    expect(wrapper.findComponent(FormInput).exists()).toBe(true);
    expect(wrapper.findComponent(FormSelect).exists()).toBe(true);

    const input = wrapper.find("input");
    expect(input.attributes().disabled).toBeDefined();
    const selects = wrapper.findAll("select");
    expect(selects[0].attributes().disabled).toBeDefined();
    expect(selects[1].attributes().disabled).toBeDefined();
  });

  it("renders disabledFields of group fields successfully", async () => {
    const wrapper = mount(FormComponentOptions, {
      props: {
        schema: testGroup.schema,
        data: testGroup.data,
        disabledFields: ["exampleGroup.0.groupInput"],
      },
    });
    expect(wrapper.findComponent(FormGroup).exists()).toBe(true);
    expect(wrapper.findComponent(FormComponentOptions).exists()).toBe(true);
    expect(wrapper.findComponent(FormInput).exists()).toBe(true);
    expect(wrapper.findComponent(FormCheckboxGroup).exists()).toBe(true);

    const input = wrapper.find("input[type='text']");
    expect(input.attributes().disabled).toBeDefined();

    const checkboxes = wrapper.findAll("input[type='checkbox']");
    expect(checkboxes[1].attributes().disabled).not.toBeDefined();
    expect(checkboxes[2].attributes().disabled).not.toBeDefined();

    await wrapper.setProps({
      disabledFields: [
        "exampleGroup.0.groupInput",
        "exampleGroup.0.groupCheckBoxGroup",
      ],
    });
    expect(checkboxes[1].attributes().disabled).toBeDefined();
    expect(checkboxes[2].attributes().disabled).toBeDefined();
  });

  it("renders error of single field successfully", async () => {
    const schema: FormComponentSchemaType = {
      fieldProps: { label: "Title", id: "testInput" },
      type: "input",
    };
    const data: string = "";
    const wrapper = mount(FormComponentOptions, {
      props: {
        schema,
        data,
        error: [{ path: "testInput", errorMsg: "input test error" }],
      },
    });
    expect(wrapper.findComponent(FormInput).exists()).toBe(true);
    expect(wrapper.text()).toContain("input test error");
  });

  it("renders error of group fields successfully", async () => {
    const wrapper = mount(FormComponentOptions, {
      props: {
        schema: testGroup.schema,
        data: testGroup.data,
        error: [
          {
            path: "exampleGroup.0.groupInput",
            errorMsg: "group input test error",
          },
          {
            path: "testpath",
            errorMsg: "test no show error",
          },
        ],
      },
    });
    expect(wrapper.findComponent(FormGroup).exists()).toBe(true);
    expect(wrapper.findComponent(FormComponentOptions).exists()).toBe(true);
    expect(wrapper.findComponent(FormInput).exists()).toBe(true);
    expect(wrapper.findComponent(FormCheckboxGroup).exists()).toBe(true);

    expect(wrapper.text()).toContain("group input test error");
    expect(wrapper.text()).not.toContain("test not show error");

    await wrapper.setProps({
      error: [
        {
          path: "exampleGroup.0.groupCheckBoxGroup",
          errorMsg: "group checkboxgroup test error",
        },
      ],
    });

    expect(wrapper.text()).not.toContain("group input test error");
    expect(wrapper.text()).toContain("group checkboxgroup test error");
  });
});
