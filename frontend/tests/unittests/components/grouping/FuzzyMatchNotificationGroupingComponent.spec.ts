import { describe, it, expect, beforeEach } from "vitest";

import { VueWrapper, mount } from "@vue/test-utils";
import FuzzyMatchNotificationGroupingComponent from "@/components/grouping/FuzzyMatchNotificationGroupingComponent.vue";
import type { FuzzyMatcherEvent, ValidationMessageType } from "@/dto/CommonTypesDto";
import { useEventBus } from "@vueuse/core";
import { nextTick } from "vue";
import type { CDSActionableNotification } from "@carbon/web-components";

const defaultProps = {
  id: "global",
  businessName: "Awesome Corp.",
};

const fuzzyBus = useEventBus<FuzzyMatcherEvent>("fuzzy-error-notification");

const errorBus = useEventBus<ValidationMessageType[]>("submission-error-notification");

let errorEvent: ValidationMessageType[];
let errorPayload: any;
errorBus.on((_event: ValidationMessageType[], _payload) => {
  errorEvent = _event;
  errorPayload = _payload;
});

describe("Fuzzy Match Notification Grouping Component", () => {
  beforeEach(() => {
    errorEvent = undefined;
    errorPayload = undefined;
  });

  it("renders no error by default", () => {
    const wrapper = mount(FuzzyMatchNotificationGroupingComponent, {
      props: {
        ...defaultProps,
      },
    });
    expect(wrapper.find("cds-inline-notification").exists()).toBe(false);
  });
  it.each([
    {
      fuzzy: true,
      matchDescription: "a non-exact",
    },
    {
      fuzzy: false,
      matchDescription: "an exact",
    },
  ])(
    "renders a notification when $matchDescription match error message arrives",
    async ({ fuzzy }) => {
      const wrapper = mount(FuzzyMatchNotificationGroupingComponent, {
        props: {
          ...defaultProps,
        },
      });
      fuzzyBus.emit({
        id: "global",
        matches: [{ field: "businessInformation.foo", match: "00000001", fuzzy }],
      });

      await nextTick();

      const expectedTitle = fuzzy ? "Possible matching records found" : "Client already exists";
      const expectedPrefix = fuzzy ? "Partial matching on" : "Matching on";

      expect(wrapper.find("cds-actionable-notification").exists()).toBe(true);
      expect(
        wrapper.find<CDSActionableNotification>("cds-actionable-notification").element.title,
      ).toContain(expectedTitle);
      expect(wrapper.find("cds-actionable-notification").text()).toContain(expectedPrefix);
      expect(wrapper.find("cds-actionable-notification").text()).toContain("foo");
      expect(wrapper.find("cds-actionable-notification").text()).toContain("00000001");
    },
  );
  it("renders as many field items as the number of different fields in the error message", async () => {
    const wrapper = mount(FuzzyMatchNotificationGroupingComponent, {
      props: {
        ...defaultProps,
      },
    });
    fuzzyBus.emit({
      id: "global",
      matches: [
        { field: "businessInformation.foo", match: "00000001", fuzzy: true },
        { field: "businessInformation.bar", match: "00000002", fuzzy: true },
      ],
    });

    await nextTick();

    expect(wrapper.find("cds-actionable-notification").exists()).toBe(true);
    expect(
      wrapper.find<CDSActionableNotification>("cds-actionable-notification").element.title,
    ).toContain("Possible matching records found");
    const liList = wrapper.findAll<HTMLLIElement>("cds-actionable-notification li");
    expect(liList).toHaveLength(2);

    expect(liList[0].text()).toContain("foo");
    expect(liList[0].text()).toContain("00000001");

    expect(liList[1].text()).toContain("bar");
    expect(liList[1].text()).toContain("00000002");
  });
  describe("when error message includes both fuzzy and exact matching errors", () => {
    let wrapper: VueWrapper;
    beforeEach(async () => {
      wrapper = mount(FuzzyMatchNotificationGroupingComponent, {
        props: {
          ...defaultProps,
        },
      });
      fuzzyBus.emit({
        id: "global",
        matches: [
          { field: "businessInformation.foo", match: "00000001", fuzzy: true },
          { field: "businessInformation.bar", match: "00000002", fuzzy: false },
        ],
      });
      await nextTick();
    });
    it("renders the exact matching title and all the errors", () => {
      expect(
        wrapper.find<CDSActionableNotification>("cds-actionable-notification").element.title,
      ).toContain("Client already exists");
      const liList = wrapper.findAll<HTMLLIElement>("cds-actionable-notification li");
      expect(liList).toHaveLength(2);

      expect(liList[0].text()).toContain("foo");
      expect(liList[0].text()).toContain("Partial matching on");
      expect(liList[0].text()).toContain("00000001");

      expect(liList[1].text()).toContain("bar");
      expect(liList[1].text()).toContain("Matching on");
      expect(liList[1].text()).toContain("00000002");
    });
  });
  describe("when there is a rendered notification", () => {
    let wrapper: VueWrapper<any, any>;
    beforeEach(async () => {
      wrapper = mount(FuzzyMatchNotificationGroupingComponent, {
        props: {
          ...defaultProps,
        },
      });
      fuzzyBus.emit({
        id: "global",
        matches: [{ field: "businessInformation.foo", match: "00000001", fuzzy: true }],
      });

      await nextTick();

      const elements = wrapper.findAll("cds-actionable-notification");
      expect(elements).toHaveLength(1);
    });
    it("clears the notification when an empty event arrives", async () => {
      fuzzyBus.emit(undefined);

      await nextTick();

      const elements = wrapper.findAll("cds-actionable-notification");
      expect(elements).toHaveLength(0);
    });
  });
  describe("emiting validation messages for specific values of properties field and fuzzy", () => {
    const identificationTypeGroup = ["identificationType.text", "identificationProvince.text"];
    const clientIdentificationGroup = [
      "businessInformation.clientIdentification",
      "businessInformation.clientTypeOfId",
      "businessInformation.clientIdNumber",
    ];
    const scenarioList = [
      {
        field: "businessInformation.businessName",
        fuzzy: true,
        expectedFieldIdList: [
          "businessInformation.firstName",
          "businessInformation.lastName",
          "businessInformation.birthdate",
        ],
      },
      {
        field: "businessInformation.businessName",
        fuzzy: false,
        expectedFieldIdList: [
          "businessInformation.firstName",
          "businessInformation.lastName",
          "businessInformation.birthdate",
          ...clientIdentificationGroup,
        ],
      },
      {
        field: "businessInformation.identification",
        fuzzy: false,
        expectedFieldIdList: [...identificationTypeGroup, ...clientIdentificationGroup],
      },
    ];
    it.each(scenarioList)(
      "emits validation messages for field: $field and fuzzy: $fuzzy ",
      (scenario) => {
        mount(FuzzyMatchNotificationGroupingComponent, {
          props: {
            ...defaultProps,
          },
        });
        fuzzyBus.emit({
          id: "global",
          matches: [
            {
              ...scenario,
              match: "00000001",
            },
          ],
        });

        expect(errorPayload.skipNotification).toBe(true);

        expect(errorEvent).toHaveLength(scenario.expectedFieldIdList.length);

        expect(errorEvent).toMatchObject(
          scenario.expectedFieldIdList.map((fieldId) => ({
            fieldId,
          })),
        );

        // warning only when fuzzy is true
        expect(errorPayload.warning).toBe(scenario.fuzzy);
      },
    );
  });
  it("doesn't emit validation messages if not implemented for such field", () => {
    mount(FuzzyMatchNotificationGroupingComponent, {
      props: {
        ...defaultProps,
      },
    });
    fuzzyBus.emit({
      id: "global",
      matches: [
        {
          field: "randomName",
          fuzzy: true,
          match: "00000001",
        },
      ],
    });

    expect(errorEvent).toBeUndefined();
    expect(errorPayload).toBeUndefined();
  });
});