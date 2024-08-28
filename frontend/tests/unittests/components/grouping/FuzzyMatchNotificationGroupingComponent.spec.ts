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

const errorBus = useEventBus<ValidationMessageType[]>(
  "submission-error-notification"
);

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
      partialMatch: true,
      matchDescription: "a non-exact, non-blocking",
    },
    {
      fuzzy: true,
      partialMatch: false,
      matchDescription: "an exact but non-blocking",
    },
    {
      fuzzy: false,
      partialMatch: false,
      matchDescription: "an exact, blocking",
    },
  ])(
    "renders a notification when $matchDescription match error message arrives",
    async ({ fuzzy, partialMatch }) => {
      const wrapper = mount(FuzzyMatchNotificationGroupingComponent, {
        props: {
          ...defaultProps,
        },
      });
      fuzzyBus.emit({
        id: "global",
        matches: [
          {
            field: "businessInformation.businessName",
            match: "00000001",
            fuzzy,
            partialMatch,
          },
        ],
      });

      await nextTick();

      const expectedTitle = fuzzy
        ? "Possible matching records found"
        : "Client already exists";

      const expectedPrefix = fuzzy ? "Client number " : "It looks like";

      expect(wrapper.find("cds-actionable-notification").exists()).toBe(true);
      expect(
        wrapper.find<CDSActionableNotification>("cds-actionable-notification")
          .element.title
      ).toContain(expectedTitle);
      expect(wrapper.find("cds-actionable-notification").text()).toContain(
        expectedPrefix
      );
      if (fuzzy)
        expect(wrapper.find("cds-actionable-notification").text()).toContain(
          "client name"
        );
      expect(wrapper.find("cds-actionable-notification").text()).toContain(
        "00000001"
      );
    }
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
        {
          field: "businessInformation.businessName",
          match: "00000001",
          fuzzy: true,
          partialMatch: true,
        },
        {
          field: "businessInformation.federalId",
          match: "00000002",
          fuzzy: true,
          partialMatch: true,
        },
      ],
    });

    await nextTick();

    expect(wrapper.find("cds-actionable-notification").exists()).toBe(true);
    expect(
      wrapper.find<CDSActionableNotification>("cds-actionable-notification")
        .element.title
    ).toContain("Possible matching records found");
    const liList = wrapper.findAll<HTMLLIElement>(
      "cds-actionable-notification span"
    );
    expect(liList).toHaveLength(3);

    expect(liList[1].text()).toContain("client name");
    expect(liList[1].text()).toContain("00000001");

    expect(liList[0].text()).toContain("federal identification number");
    expect(liList[0].text()).toContain("00000002");
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
          {
            field: "businessInformation.foo",
            match: "00000001",
            fuzzy: true,
            partialMatch: true,
          },
          {
            field: "businessInformation.bar",
            match: "00000002",
            fuzzy: false,
            partialMatch: false,
          },
        ],
      });
      await nextTick();
    });

    it("renders the exact matching title and all the errors", () => {
      expect(
        wrapper.find<CDSActionableNotification>("cds-actionable-notification")
          .element.title
      ).toContain("Client already exists");

      const liList = wrapper.findAll<HTMLLIElement>(
        "cds-actionable-notification span a"
      );

      expect(liList).toHaveLength(2);
      expect(liList[1].text()).toContain("00000002");
      expect(liList[0].text()).toContain("00000001");
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
        matches: [
          {
            field: "businessInformation.foo",
            match: "00000001",
            fuzzy: true,
            partialMatch: true,
          },
        ],
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
    const scenarioList = [
      {
        field: "businessInformation.individual",
        fuzzy: true,
        partialMatch: true,
        expectedFieldIdList: [
          "businessInformation.firstName",
          "businessInformation.lastName",
          "businessInformation.birthdate",
          "businessInformation.businessName",
        ],
      },
      {
        field: "businessInformation.individualAndDocument",
        fuzzy: false,
        partialMatch: true,
        expectedFieldIdList: [
          "businessInformation.firstName",
          "businessInformation.lastName",
          "businessInformation.birthdate",
          "businessInformation.clientIdentification",
        ],
      },
      {
        field: "businessInformation.clientIdentification",
        fuzzy: false,
        partialMatch: false,
        expectedFieldIdList: [
          "businessInformation.identificationType",
          "identificationProvince.text",
          "businessInformation.clientIdentification",
        ],
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
          }))
        );

        // warning only when fuzzy is true
        expect(errorPayload.warning).toBe(scenario.fuzzy);
      }
    );
  });

  it("emit validation messages even if field not mapped", () => {
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
          partialMatch: true,
        },
      ],
    });

    expect(errorPayload.skipNotification).toBe(true);
    expect(errorEvent).toHaveLength(1);
    expect(errorEvent).toMatchObject([
      {
        fieldId: "randomName",
        errorMsg: "There's already a client with this random name",
      },
    ]);

    // warning only when fuzzy is true
    expect(errorPayload.warning).toBe(true);
  });
});
