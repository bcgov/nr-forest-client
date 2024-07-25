import { describe, it, expect } from "vitest";

import { mount } from '@vue/test-utils';
import LoadingOverlayComponent from '@/components/LoadingOverlayComponent.vue';
import { nextTick } from 'vue';

describe('LoadingOverlayComponent.vue', () => {
  it('shows overlay when isVisible is true', async () => {
    const wrapper = mount(LoadingOverlayComponent, {
      props: { isVisible: true, message: 'Loading...', showLoading: true }
    });
    await nextTick();
    expect(wrapper.find('.overlay').exists()).toBe(true);
  });

  it('hides overlay when isVisible is false', async () => {
    const wrapper = mount(LoadingOverlayComponent, {
      props: { isVisible: false, message: 'Loading...', showLoading: true }
    });
    await nextTick();
    expect(wrapper.find('.overlay').exists()).toBe(false);
  });

  it('shows loading indicator when showLoading is true', async () => {
    const wrapper = mount(LoadingOverlayComponent, {
      props: { isVisible: true, message: 'Loading...', showLoading: true }
    });
    await nextTick();
    expect(wrapper.find('cds-loading').exists()).toBe(true);
  });

  it('hides loading indicator when showLoading is false', async () => {
    const wrapper = mount(LoadingOverlayComponent, {
      props: { isVisible: true, message: 'Loading...', showLoading: false }
    });
    await nextTick();
    expect(wrapper.find('cds-loading').exists()).toBe(false);
  });

  it('displays the correct message', async () => {
    const testMessage = 'Test Message';
    const wrapper = mount(LoadingOverlayComponent, {
      props: { isVisible: true, message: testMessage, showLoading: true }
    });
    await nextTick();
    expect(wrapper.find('.message').text()).toBe(testMessage);
  });

});
