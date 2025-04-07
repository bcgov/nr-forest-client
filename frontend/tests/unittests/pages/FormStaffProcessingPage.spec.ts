import { describe, it, expect } from "vitest";

import { mount } from '@vue/test-utils';
import FormStaffProcessingPage from '@/pages/FormStaffProcessingPage.vue';

describe('FormStaffProcessingPage.vue', () => {
  const submissionId = 123;
  const clientEmail = 'test@example.com';
  const notifyClientInd = "Y";

  it('renders without crashing', () => {
    const wrapper = mount(FormStaffProcessingPage, {
      props: { submissionId, clientEmail, notifyClientInd }
    });
    expect(wrapper.exists()).toBeTruthy();
  });

  it('renders SVG correctly', () => {
    const wrapper = mount(FormStaffProcessingPage, {
      props: { submissionId, clientEmail, notifyClientInd }
    });
    expect(wrapper.find('.submission-badge').exists()).toBeTruthy();
  });

  it('displays static text content correctly', () => {
    const wrapper = mount(FormStaffProcessingPage, {
      props: { submissionId, clientEmail, notifyClientInd }
    });
    expect(wrapper.text()).toContain('This submission is being processed');
    expect(wrapper.text()).toContain('Create another client');
  });

  it('renders dynamic content correctly', async () => {
    const wrapper = mount(FormStaffProcessingPage, {
      props: { submissionId, clientEmail, notifyClientInd },
      global: {
        mocks: {
          clientEmail
        }
      }
    });
    await wrapper.vm.$nextTick();
    expect(wrapper.html()).toContain(clientEmail);    
    expect(wrapper.find('cds-button[href="/submissions/123"]').exists()).toBeTruthy();
  });

  it('button links are correct', () => {
    const wrapper = mount(FormStaffProcessingPage, {
      props: { submissionId, clientEmail, notifyClientInd }
    });
    const trackSubmissionButton = wrapper.find('cds-button[href="/submissions/123"]');
    const createClientButton = wrapper.find('cds-button[href="/new-client-staff"]');
    expect(trackSubmissionButton.exists()).toBeTruthy();
    expect(createClientButton.exists()).toBeTruthy();
  });
});
