import { describe, it, expect, beforeEach } from 'vitest'

import { VueWrapper, mount } from '@vue/test-utils'
import ErrorNotificationGroupingComponent from '@/components/grouping/ErrorNotificationGroupingComponent.vue'
import type { ValidationMessageType } from '@/dto/CommonTypesDto'
import { useEventBus } from '@vueuse/core'
import { nextTick } from 'vue'

const formData = {
  location: {
    addresses: []
  }
}

describe('Error Notification Grouping Component', () => {
  it('renders no error by default', () => {
    const wrapper = mount(ErrorNotificationGroupingComponent, {
      props: {
        formData,
        scrollToNewContact: () => {}
      },
    })
    expect(wrapper.find('cds-inline-notification').exists()).toBe(false)
  })
  describe('location.addresses.*.locationName', () => {
    it('renders a notification when an error message arrives', async () => {
      const wrapper = mount(ErrorNotificationGroupingComponent, {
        props: {
          formData,
          scrollToNewContact: () => {}
        },
      })
      const notificationBus = useEventBus<ValidationMessageType|undefined>("error-notification");
      const locationName = 'My location'
      notificationBus.emit({
        fieldId: 'location.addresses[1].locationName',
        errorMsg: 'This value is invalid!'
      }, locationName)
  
      await nextTick()
  
      expect(wrapper.find('cds-inline-notification').exists()).toBe(true)
      expect(wrapper.find('cds-inline-notification').text()).toContain(locationName)
    })
    it('renders as many notifications as the number of different error messages arrived', async () => {
      const wrapper = mount(ErrorNotificationGroupingComponent, {
        props: {
          formData,
          scrollToNewContact: () => {}
        },
      })
      const notificationBus = useEventBus<ValidationMessageType|undefined>("error-notification");
      const locationNames = ['Mailing address', 'My location 1', 'My location 2', 'My location 3']
      notificationBus.emit({
        fieldId: 'location.addresses[1].locationName',
        errorMsg: 'This value is invalid!'
      }, locationNames[1])
      notificationBus.emit({
        fieldId: 'location.addresses[2].locationName',
        errorMsg: 'This value is invalid!'
      }, locationNames[2])
      notificationBus.emit({
        fieldId: 'location.addresses[3].locationName',
        errorMsg: 'This value is invalid!'
      }, locationNames[3])
  
      await nextTick()
  
      const elements = wrapper.findAll('cds-inline-notification');
      expect(elements).toHaveLength(3)
      for (let index = 0; index < elements.length; index++) {
        const element = elements[index];
        expect(element.text()).toContain(locationNames[index + 1])
      }
    })
    describe("when there are some rendered notifications initially", () => {
      let wrapper: VueWrapper<any, any>
      const locationNames = ['Mailing address', 'My location 1', 'My location 2', 'My location 3']
      const notificationBus = useEventBus<ValidationMessageType|undefined>("error-notification");
      beforeEach(async () => {
        wrapper = mount(ErrorNotificationGroupingComponent, {
          props: {
            formData: {
              location: {
                addresses: locationNames.map(locationName => ({ locationName }))
              }
            },
            scrollToNewContact: () => {}
          },
        })
        notificationBus.emit({
          fieldId: 'location.addresses[1].locationName',
          errorMsg: 'This value is invalid!'
        }, locationNames[1])
        notificationBus.emit({
          fieldId: 'location.addresses[2].locationName',
          errorMsg: 'This value is invalid!'
        }, locationNames[2])
        notificationBus.emit({
          fieldId: 'location.addresses[3].locationName',
          errorMsg: 'This value is invalid!'
        }, locationNames[3])
    
        await nextTick()
    
        const elements = wrapper.findAll('cds-inline-notification');
        expect(elements).toHaveLength(3)
      })
      it("clears the notification when an event with no error message for the corresponding location arrives", async () => {
        notificationBus.emit({
          fieldId: 'location.addresses[2].locationName',
          errorMsg: '' // empty means valid
        }, locationNames[2])
    
        await nextTick()
    
        const elements = wrapper.findAll('cds-inline-notification');
        expect(elements).toHaveLength(2)
        for (let index = 0; index < elements.length; index++) {
          const element = elements[index];
          // #2 was removed
          expect(element.text()).not.toContain(locationNames[2])
        }

        // #1 and #3 were kept
        expect(elements[0].text()).toContain(locationNames[1])
        expect(elements[1].text()).toContain(locationNames[3])
      })
      it("clears the notification when the corresponding location has been removed", async () => {
        const newLocationNames = [...locationNames];
        newLocationNames.splice(2, 1); // removing #2
        await wrapper.setProps({
          formData: {
            location: {
              addresses: newLocationNames.map(locationName => ({ locationName }))
            }
          },
          scrollToNewContact: () => {}
        })
    
        const elements = wrapper.findAll('cds-inline-notification');
        expect(elements).toHaveLength(2)
        for (let index = 0; index < elements.length; index++) {
          const element = elements[index];
          // #2 was removed
          expect(element.text()).not.toContain(locationNames[2])
        }

        // #1 and #3 were kept
        expect(elements[0].text()).toContain(locationNames[1])
        expect(elements[1].text()).toContain(locationNames[3])
      })
      it("does not duplicate notifications", async () => {
        notificationBus.emit({
          fieldId: 'location.addresses[2].locationName',
          errorMsg: 'This value is invalid!'
        }, locationNames[2])
    
        await nextTick()
    
        const elements = wrapper.findAll('cds-inline-notification');
        expect(elements).toHaveLength(3) // still 3 notifications just like before
        for (let index = 0; index < elements.length; index++) {
          const element = elements[index];
          expect(element.text()).toContain(locationNames[index + 1])
        }
      })
      it("ignores a valid event when the corresponding location was already valid", async () => {
        notificationBus.emit({
          fieldId: 'location.addresses[0].locationName',
          errorMsg: ''
        }, locationNames[0])
    
        await nextTick()
    
        const elements = wrapper.findAll('cds-inline-notification');
        expect(elements).toHaveLength(3) // still 3 notifications just like before
        for (let index = 0; index < elements.length; index++) {
          const element = elements[index];
          expect(element.text()).toContain(locationNames[index + 1])
        }
      })
    })
  })
})
