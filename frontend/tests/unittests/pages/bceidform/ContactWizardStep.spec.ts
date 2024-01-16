import { mount } from "@vue/test-utils";
import type { DOMWrapper } from "@vue/test-utils";
import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import ContactWizardStep from "@/pages/bceidform/ContactWizardStep.vue";
import { emptyContact } from "@/dto/ApplyClientNumberDto";
import { useEventBus } from "@vueuse/core";
import * as fetcher from "@/composables/useFetch";
import type { ModalNotification } from "@/dto/CommonTypesDto";
import type { Contact, FormDataDto } from "@/dto/ApplyClientNumberDto";

const mockFetchTo = (data: any) => (url: string, received: any) => ({
  fetch: async () => {
    received.value = data;
  },
});

const setInputValue = async (inputWrapper: DOMWrapper<HTMLInputElement>, value: string) => {
  inputWrapper.element.value = value;
  await inputWrapper.trigger("input");
};

const comboBoxEventContent = (value: string) => {
  return {
    detail: {
      item: { "data-value": value, getAttribute: (key: string) => value },
    },
  };
};

const multiSelectEventContent = (value: string) => {
  return { data: value };
};

describe("ContactWizardStep.vue", () => {
  describe("when contact's firstName is null", () => {
    it("emits the confirmation event properly when Delete contact is clicked", async () => {
      const wrapper = mount(ContactWizardStep, {
        props: {
          data: {
            location: {
              addresses: [],
              contacts: [
                {
                  ...emptyContact,
                  firstName: "John",
                  lastName: "Doe",
                } as Contact,
                // And here is the contact to be deleted
                {
                  ...emptyContact,
                  firstName: null,
                  lastName: null,
                } as unknown as Contact,
              ],
            },
          } as unknown as FormDataDto,
          active: false,
        },
      });

      const bus = useEventBus<ModalNotification>("modal-notification");

      let payload: ModalNotification;
      bus.on((_payload) => {
        payload = _payload;
      });

      await wrapper.find("#deleteContact_1").trigger("click");

      expect(payload!).toMatchObject({
        name: "Contact #1",
        message: "“Contact #1” additional contact was deleted",
        kind: "contact",
      });
    });
  });

  describe("when the number of contacts is less than 5", () => {
    it("renders the 'Add another contact' button", () => {
      const wrapper = mount(ContactWizardStep, {
        props: {
          data: {
            location: {
              addresses: [],
              contacts: [
                {
                  ...emptyContact,
                  firstName: "John",
                  lastName: "Doe",
                } as Contact,
              ],
            },
          } as unknown as FormDataDto,
          active: false,
        },
      });
      
      const addButton = wrapper.find('cds-button');
      
      expect(addButton.text()).toContain("Add another contact");
      expect(addButton.exists()).toBe(true);
    });
  });

  describe("when a contact which is not the last one gets deleted", () => {
    const firstContactName = "John";
    const otherContactNames = ["Paul", "George", "Ringo"];

    const mountTest = () =>
      mount(ContactWizardStep, {
        props: {
          data: {
            location: {
              addresses: [{ locationName: "Mailing address" }],
              contacts: [firstContactName, ...otherContactNames].map(
                (firstName) =>
                  ({
                    ...emptyContact,
                    firstName,
                    lastName: "Doe",
                  }) as Contact,
              ),
            },
          } as unknown as FormDataDto,
          active: true,
        },
        attachTo: document.body,
      });
    let wrapper: ReturnType<typeof mountTest>;

    const findContact = (firstName: string) => {
      const inputArray = wrapper
        .findAll("cds-text-input[id^='firstName_']")
        .filter((domWrapper) => {
          return domWrapper.element.value === firstName;
        });
      return inputArray.length > 0 ? inputArray[0] : null;
    };

    let bus: ReturnType<typeof useEventBus<ModalNotification>>;

    beforeEach(async () => {
      vi.spyOn(fetcher, "useFetchTo").mockImplementation(
        mockFetchTo([{ code: "P", name: "Person" }]) as any,
      );
      wrapper = mountTest();

      bus = useEventBus<ModalNotification>("modal-notification");
      bus.on((payload) => {
        payload.handler(); // automatically proceed with the deletion
      });
    });

    afterEach(() => {
      bus.reset();
    });

    const fillContact = async (contactId: number) => {
      if (contactId > 0) {
        await setInputValue(wrapper.find(`cds-text-input#email_${contactId}`), "value@value.com");
      }

      await setInputValue(
        wrapper.find(`cds-text-input#phoneNumber_${contactId}`),
        "(123) 456-7890",
      );
      await wrapper
        .get(`#role_${contactId}`)
        .trigger("cds-combo-box-selected", comboBoxEventContent("Person"));

      // Sanity check
      expect(wrapper.get(`#role_${contactId}`).element.value).toEqual("Person");

      await wrapper
        .get(`#addressname_${contactId}`)
        .trigger("cds-multi-select-selected", multiSelectEventContent("Mailing address"));

      // Sanity check
      expect(wrapper.get(`#addressname_${contactId}`).element.value).toEqual("Mailing address");
    };

    it("removes the intended contact from the DOM", async () => {
      expect(findContact("George").exists()).toBe(true);

      await wrapper.get("cds-button#deleteContact_2").trigger("click");

      // properly removed from the DOM
      expect(findContact("George")).toBeFalsy();

      // others are kept in the DOM
      expect(wrapper.get("#fullName_0").text()).toMatch("John");
      expect(findContact("Paul").exists()).toBe(true);
      expect(findContact("Ringo").exists()).toBe(true);
    });

    it("can proceed to the next step (regardless of the deleted contact being invalid)", async () => {
      /*
      This test essentially makes sure the validation data structure is not messed up when a
      contact gets deleted.
      */

      // Just making sure the contact to be deleted has some empty, invalid information.
      expect(wrapper.get("cds-text-input[id^='email_2']").element.value).toBe("");

      await wrapper.get("cds-button#deleteContact_2").trigger("click");

      await fillContact(0);
      await fillContact(1);
      await fillContact(3);

      const lastValid = wrapper.emitted("valid")[wrapper.emitted("valid").length - 1];

      // The last valid event was emitted with true.
      expect(lastValid[0]).toEqual(true);
    });
  });
});
