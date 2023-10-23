import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import ContactWizardStep from '@/pages/bceidform/ContactWizardStep.vue'
import type { Contact, FormDataDto } from '@/dto/ApplyClientNumberDto'
import { emptyContact } from '@/dto/ApplyClientNumberDto'
import { useEventBus } from '@vueuse/core'
import type { ModalNotification } from '@/dto/CommonTypesDto'

describe('ContactWizardStep.vue', () => {
  describe("when contact's firstName is null", () => {
    it('emits the confirmation event properly when Delete contact is clicked', async () => {
      const wrapper = mount(ContactWizardStep, {
        props: {
          data: {
            location: {
              addresses: [],
              contacts: [
                {
                  ...emptyContact,
                  firstName: 'John',
                  lastName: 'Doe',
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
      })

      const bus = useEventBus<ModalNotification>('modal-notification')

      let payload: ModalNotification
      bus.on((_payload) => {
        payload = _payload
      })

      await wrapper.find('#deleteContact_1').trigger('click')

      expect(payload!).toMatchObject({
        message: 'Contact #1',
        kind: 'Contact deleted',
      })
    })
  })
})
