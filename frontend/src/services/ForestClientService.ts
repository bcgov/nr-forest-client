import { ref } from "vue";
import axios from "axios";
import type { Address, Contact } from "../dto/ApplyClientNumberDto";
import type { CodeDescrType } from "@/core/CommonTypes";
import { backendUrl, frontendUrl } from "@/core/CoreConstants";

axios.defaults.headers.common['Access-Control-Allow-Origin'] = frontendUrl;

/**
 * Fetch data from external resource
 * @param url resource URI
 * @param config axios configuration
 * @returns the data fetched
 */
export const useFetch = (url: string, config: any = {}) => {

  const data: any = ref(config.initialData || {});
  const info = useFetchTo(url, data, config);

  return { ...info, data }
}

export const useFetchTo = (url: string, data: any, config: any = {}) => {

  const response: any = ref({});
  const error: any = ref({});
  const loading: any = ref(false);

  const fetch = async () => {
    loading.value = true;
    try {
      const result = await axios.request({
        ...config,
        url,
        baseURL: backendUrl
      });
      Object.assign(response, result);
      data.value = result.data;
    } catch (ex) {
      Object.assign(error, ex);
    } finally {
      loading.value = false;
    }
  };

  !config.skip && fetch();

  return { response, error, data, loading, fetch }
}

export const usePost = (url: string, body: any, config: any = {}) => {
  const response: any = ref({});
  const responseBody: any = ref({});
  const error: any = ref({});
  const loading: any = ref(false);

  const fetch = async () => {
    loading.value = true;
    try {
      const result = await axios.request({
        ...config,
        url,
        baseURL: backendUrl,
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        data: body
      });
      response.value = result;
      responseBody.value = result.data;
    } catch (ex: any) {
      error.value = ex.response;
    } finally {
      loading.value = false;
    }
  };

  !config.skip && fetch();

  return { response, error, responseBody, loading, fetch }
}

export const addNewAddress = (addresses: Address[]) => {
  const blankAddress: Address = {
    streetAddress: "",
    country: { value: "", text: "" } as CodeDescrType,
    province: { value: "", text: "" } as CodeDescrType,
    city: "",
    postalCode: "",
    contacts: [
      {
        contactType: { value: "", text: "" } as CodeDescrType,
        firstName: "",
        lastName: "",
        phoneNumber: "",
        email: "",
      }
    ] as Contact[],
  };

  let newAddresses = addresses.push(blankAddress);
  return newAddresses;
}

export const addNewContact = (contacts: Contact[]) => {
  const blankContact: Contact = {
    contactType: { value: "", text: "" } as CodeDescrType,
    firstName: "",
    lastName: "",
    phoneNumber: "",
    email: "",
  };

  let newContacts = contacts.push(blankContact);
  return newContacts;
}
