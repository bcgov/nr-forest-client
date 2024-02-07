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

const globalDefault = {
  config: {
    globalProperties: {
      $features: {
        BCEID_MULTI_ADDRESS: false,
      },
    },
  },
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
        global: globalDefault,
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
        global: globalDefault,
      });

      const addButton = wrapper.find("cds-button");

      expect(addButton.text()).toContain("Add another contact");
      expect(addButton.exists()).toBe(true);
    });
  });

  describe.each([
    { BCEID_MULTI_ADDRESS: true },
    { BCEID_MULTI_ADDRESS: false },
  ])("when feature BCEID_MULTI_ADDRESS is: $BCEID_MULTI_ADDRESS", ({ BCEID_MULTI_ADDRESS }) => {
    const global = {
      config: {
        globalProperties: {
          $features: {
            BCEID_MULTI_ADDRESS,
          },
        },
      },
    };

    const firstContactName = "John";
    const otherContactNames = ["Paul", "George", "Ringo"];

    const mountTest = (includeOtherContacts: boolean) =>
        mount(ContactWizardStep, {
          props: {
            data: {
              location: {
                addresses: [{ locationName: "MAILING ADDRESS" }],
                contacts: [
                  firstContactName,
                  ...(includeOtherContacts ? otherContactNames : []),
                ].map(
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
          global,
          attachTo: document.body,
        });

    let wrapper: ReturnType<typeof mountTest>;

    describe("when a new contact is added", () => {
      let newContact: Contact;
      beforeEach(async () => {
        wrapper = mountTest(false);
        const addButton = wrapper.findAll("cds-button").filter((domWrapper) => {
          return domWrapper.text() === "Add another contact";
        })[0];

        await addButton.trigger("click");

        const updatedData = wrapper.emitted("update:data").slice(-1)[0][0];
        newContact = updatedData.location.contacts[1];
      })
      if (BCEID_MULTI_ADDRESS) {
        it("should not associate the contact to any address", () => {
          expect(newContact.locationNames).toHaveLength(0);
        });
      } else {
        it("should automatically associate the contact to the default address", () => {
          expect(newContact.locationNames).toHaveLength(1);
            expect(newContact.locationNames[0]).toMatchObject({
              value: "0",
              text: "MAILING ADDRESS",
            });
        });
      }
    })

    describe("when a contact which is not the last one gets deleted", () => {
      const findContact = (firstName: string) => {
        const inputArray = wrapper
          .findAll("cds-text-input[id^='firstName_']")
          .filter((domWrapper) => {
            return domWrapper.element.value === firstName;
          });
        return inputArray.length > 0 ? inputArray[0] : null;
      };
  
      let bus: ReturnType<typeof useEventBus<ModalNotification>>;
  
      const fillContactName = async (contactId: number, firstName?: string) => {
        let domWrapper: DOMWrapper<HTMLInputElement>;
        if (contactId > 0) {
          domWrapper = wrapper.find(`cds-text-input#firstName_${contactId}`);
          if (!domWrapper.element.value) {
            await setInputValue(domWrapper, firstName || "First" + contactId);
          }
          domWrapper = wrapper.find(`cds-text-input#lastName_${contactId}`);
          if (!domWrapper.element.value) {
            await setInputValue(domWrapper, "Doe");
          }
        }
      };
  
      const fillContactRemaining = async (contactId: number) => {
        let domWrapper: DOMWrapper<HTMLInputElement>;
        if (contactId > 0) {
          domWrapper = wrapper.find(`cds-text-input#firstName_${contactId}`);
          if (!domWrapper.element.value) {
            await setInputValue(domWrapper, otherContactNames[contactId] || "First" + contactId);
          }
          domWrapper = wrapper.find(`cds-text-input#lastName_${contactId}`);
          if (!domWrapper.element.value) {
            await setInputValue(domWrapper, "Last" + contactId);
          }
          domWrapper = wrapper.find(`cds-text-input#email_${contactId}`);
          if (!domWrapper.element.value) {
            await setInputValue(domWrapper, "value@value.com");
          }
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
  
        // Note: If BCEID_MULTI_ADDRESS is disabled, the default address is automatically selected.
        if (BCEID_MULTI_ADDRESS) {
          await wrapper
            .get(`#addressname_${contactId}`)
            .trigger("cds-multi-select-selected", multiSelectEventContent("MAILING ADDRESS"));

            // Sanity check
            expect(wrapper.get(`#addressname_${contactId}`).element.value).toEqual("MAILING ADDRESS");
        }
      };
  
      describe.each([
        { includeOtherContactsInProps: true, otherContactsVerb: "are" },
        { includeOtherContactsInProps: false, otherContactsVerb: "are not" },
      ])(
        "when other contacts $otherContactsVerb provided in the props",
        ({ includeOtherContactsInProps }) => {
          beforeEach(async () => {
            vi.spyOn(fetcher, "useFetchTo").mockImplementation(
              mockFetchTo([{ code: "P", name: "Person" }]) as any,
            );
            wrapper = mountTest(includeOtherContactsInProps);
  
            bus = useEventBus<ModalNotification>("modal-notification");
            bus.on((payload) => {
              payload.handler(); // automatically proceed with the deletion
            });
  
            if (!includeOtherContactsInProps) {
              const addButton = wrapper.findAll("cds-button").filter((domWrapper) => {
                return domWrapper.text() === "Add another contact";
              })[0];
  
              // add contacts manually
              for (let i = 0; i < otherContactNames.length; i++) {
                const firstName = otherContactNames[i];
                await addButton.trigger("click");
                await fillContactName(i + 1, firstName);
              }
            }
          });
  
          afterEach(() => {
            bus.reset();
          });
  
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
  
            await fillContactRemaining(0);
            await fillContactRemaining(1);
            await fillContactRemaining(3);
  
            const lastValid = wrapper.emitted("valid")[wrapper.emitted("valid").length - 1];
  
            // The last valid event was emitted with true.
            expect(lastValid[0]).toEqual(true);
          });
        },
      );
    });
  });
});
