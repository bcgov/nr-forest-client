import { createRouter, createMemoryHistory } from 'vue-router';
import { describe, it, expect } from "vitest";
import { mount } from '@vue/test-utils';
import FormStaffPage from '@/pages/FormStaffPage.vue';

describe('FormStaffPage', () => {

  it('renders the clientType dropdown with the expected order of elements', async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/', component: FormStaffPage },],
    });

    const wrapper = mount(FormStaffPage, {
      global: {
        plugins: [router],
      },
    });
    const clientTypeDropdown = wrapper.find('#clientType');
    
    // Extract the innerHTML of the dropdown
    const dropdownHTML = clientTypeDropdown.element.innerHTML;
    
    // Parse the innerHTML to extract the text content of each dropdown item
    const parser = new DOMParser();
    const doc = parser.parseFromString(`<cds-combo-box>${dropdownHTML}</cds-combo-box>`, 'text/html');
    const dropdownItems = doc.querySelectorAll('cds-combo-box-item');

    // Extract the text content from each dropdown item
    const dropdownValues = Array.from(dropdownItems).map(item => item.textContent.trim());
    
    expect(dropdownValues).toEqual(['Individual','BC registered business','First Nation','Government','Ministry of Forests','Unregistered company']);
  });
});