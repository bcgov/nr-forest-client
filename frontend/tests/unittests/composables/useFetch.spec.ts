import { mount } from '@vue/test-utils';
import { ref, watch } from 'vue';
import axios from 'axios';
import { useFetch, usePost } from '@/composables/useFetch';
import { expect } from 'chai';
import sinon from 'sinon';

describe('useFetch', () => {
  let axiosStub;

  beforeEach(() => {
    axiosStub = sinon.stub(axios, 'request');
  });

  afterEach(() => {
    axiosStub.restore();
  });

  it('should make a GET request using Axios', async () => {
    const responseData = ref('');

    axiosStub.resolves({ data: 'Mock data' });

    const TestComponent = {
      template: '<div></div>',
      setup: () => {
        const { fetch, data } = useFetch('/api/data', { skip: true });
        watch(data, (value) => (responseData.value = value));
        fetch();
      },
    };

    watch(responseData, (value) => {
      expect(value).to.equal('Mock data');
    });

    const wrapper = mount(TestComponent);

    await wrapper.vm.$nextTick();

    expect(axiosStub.calledWithMatch({
      baseURL: 'http://localhost:8080',
      headers: {
        'Access-Control-Allow-Origin': 'http://localhost:3000',
        'Content-Type': 'application/json',
      },
      skip: true,
      url: '/api/data',
    })).to.be.equal(true);
  });

  it('should make a POST request using Axios', async () => {
    const responseData = ref('');

    axiosStub.resolves({ data: 'Mock data' });

    const TestComponent = {
      template: '<div></div>',
      setup: () => {
        const { fetch, responseBody } = usePost(
          '/api/data',
          { name: 'test' },
          { skip: true }
        );
        watch(responseBody, (value) => (responseData.value = value));
        fetch();
      },
    };

    watch(responseData, (value) => {
      expect(value).to.equal('Mock data');
    });

    const wrapper = mount(TestComponent);

    await wrapper.vm.$nextTick();

    expect(axiosStub.calledWithMatch({
      baseURL: 'http://localhost:8080',
      headers: {
        'Access-Control-Allow-Origin': 'http://localhost:3000',
        'Content-Type': 'application/json',
      },
      skip: true,
      url: '/api/data',
      method: 'POST',
      data: { name: 'test' },
    })).to.be.equal(true);
  });

  it('should make a GET request using Axios and get an error', async () => {
    const responseData = ref(null);

    axiosStub.rejects(
      new Error({ response: { status: 500, data: { message: 'Error' } } })
    );

    const TestComponent = {
      template: '<div></div>',
      setup: () => {
        const { fetch, error } = useFetch('/api/data', { skip: true });
        watch(error, (value) => (responseData.value = value));
        fetch();
      },
    };

    watch(responseData, (value) => {
      expect(value).to.deep.equal(
        new Error({ response: { status: 500, data: { message: 'Error' } } })
      );
    });

    const wrapper = mount(TestComponent);

    await wrapper.vm.$nextTick();

    expect(axiosStub.calledWithMatch({
      baseURL: 'http://localhost:8080',
      headers: {
        'Access-Control-Allow-Origin': 'http://localhost:3000',
        'Content-Type': 'application/json',
      },
      skip: true,
      url: '/api/data',
    })).to.be.equal(true);
  });

  it('should make a POST request using Axios and get an error', async () => {
    const responseData = ref(null);

    axiosStub.rejects(
      new Error({ response: { status: 500, data: { message: 'Error' } } })
    );

    const TestComponent = {
      template: '<div></div>',
      setup: () => {
        const { fetch, error } = usePost(
          '/api/data',
          { name: 'test' },
          { skip: true }
        );
        watch(error, (value) => (responseData.value = value));
        fetch();
      },
    };

    watch(responseData, (value) => {
      expect(value).to.deep.equal(
        new Error({ response: { status: 500, data: { message: 'Error' } } })
      );
    });

    const wrapper = mount(TestComponent);

    await wrapper.vm.$nextTick();

    expect(axiosStub.calledWithMatch({
      baseURL: 'http://localhost:8080',
      headers: {
        'Access-Control-Allow-Origin': 'http://localhost:3000',
        'Content-Type': 'application/json',
      },
      skip: true,
      url: '/api/data',
      method: 'POST',
      data: { name: 'test' },
    })).to.be.equal(true);
  });
});
