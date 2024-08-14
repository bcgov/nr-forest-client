import { createRouter, createMemoryHistory } from 'vue-router';
import { describe, it, expect, vi } from "vitest";
import { mount } from '@vue/test-utils';
import FormStaffPage from '@/pages/FormStaffPage.vue';
import ForestClientUserSession from '@/helpers/ForestClientUserSession';

// Mocking the ForestClientUserSession
vi.mock('@/helpers/ForestClientUserSession', () => ({
  default: {
    authorities: []
  }
}));

describe('FormStaffPage', () => {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/', component: FormStaffPage }],
  });

  const mountComponent = () => {
    return mount(FormStaffPage, {
      global: {
        plugins: [router],
      },
    });
  };

  it('renders the clientType dropdown with the expected order of elements for a non-admin user', async () => {
    // Mock as non-admin user
    ForestClientUserSession.authorities = [];

    const wrapper = mountComponent();
    const clientTypeDropdown = wrapper.find('#clientType');

    // Extract the innerHTML of the dropdown
    const dropdownHTML = clientTypeDropdown.element.innerHTML;

    // Parse the innerHTML to extract the text content of each dropdown item
    const parser = new DOMParser();
    const doc = parser.parseFromString(`<cds-combo-box>${dropdownHTML}</cds-combo-box>`, 'text/html');
    const dropdownItems = doc.querySelectorAll('cds-combo-box-item');

    const dropdownValues = Array.from(dropdownItems).map(item => item.textContent.trim());

    const expectedDropdownValues = [
      'Individual',
      'BC registered business',
      'First Nation',
      'Government',
      'Ministry of Forests',
    ];

    expect(dropdownValues).toEqual(expectedDropdownValues);
  });

  it('renders the clientType dropdown with the expected order of elements for an admin user', async () => {
    // Mock as admin user
    ForestClientUserSession.authorities = ['CLIENT_ADMIN'];

    const wrapper = mountComponent();
    const clientTypeDropdown = wrapper.find('#clientType');

    // Extract the innerHTML of the dropdown
    const dropdownHTML = clientTypeDropdown.element.innerHTML;

    // Parse the innerHTML to extract the text content of each dropdown item
    const parser = new DOMParser();
    const doc = parser.parseFromString(`<cds-combo-box>${dropdownHTML}</cds-combo-box>`, 'text/html');
    const dropdownItems = doc.querySelectorAll('cds-combo-box-item');

    const dropdownValues = Array.from(dropdownItems).map(item => item.textContent.trim());

    const expectedDropdownValues = [
      'Individual',
      'BC registered business',
      'First Nation',
      'Government',
      'Ministry of Forests',
      'Unregistered company',
    ];

    expect(dropdownValues).toEqual(expectedDropdownValues);
  });
});
