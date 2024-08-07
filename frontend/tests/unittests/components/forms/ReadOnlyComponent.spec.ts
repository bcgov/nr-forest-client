// Import necessary modules
import { describe, it, expect } from "vitest";
import { mount } from '@vue/test-utils';
import ReadOnlyComponent from '@/components/forms/ReadOnlyComponent.vue';

describe('ReadOnlyComponent.vue', () => {
  it('renders the component correctly', () => {
	const wrapper = mount(ReadOnlyComponent, {
	  props: { label: 'Test Label' }
	});
	expect(wrapper.exists()).toBe(true);
  });

  it('displays the label prop correctly', () => {
	const wrapper = mount(ReadOnlyComponent, {
	  props: { label: 'Test Label' }
	});
	expect(wrapper.find('.label-01').text()).toBe('Test Label');
  });

  it('renders default slot content', () => {
	const wrapper = mount(ReadOnlyComponent, {
	  props: { label: 'Test Label' },
	  slots: {
		default: '<div class="default-slot-content">Default Slot Content</div>'
	  }
	});
	expect(wrapper.find('.default-slot-content').exists()).toBe(true);
	expect(wrapper.find('.default-slot-content').text()).toBe('Default Slot Content');
  });

  it('renders named slot "icon"', () => {
	const wrapper = mount(ReadOnlyComponent, {
	  props: { label: 'Test Label' },
	  slots: {
		icon: '<span class="icon-slot-content">Icon Slot Content</span>'
	  }
	});
	expect(wrapper.find('.icon-slot-content').exists()).toBe(true);
	expect(wrapper.find('.icon-slot-content').text()).toBe('Icon Slot Content');
  });
});