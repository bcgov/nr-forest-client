import { ref, type Ref } from "vue";
import axios, { type AxiosError } from "axios";
import { backendUrl, frontendUrl } from "@/CoreConstants";
import { useEventBus } from "@vueuse/core";
import ForestClientUserSession from "@/helpers/ForestClientUserSession";
import type { ValidationMessageType } from "@/dto/CommonTypesDto";

const notificationBus = useEventBus<ValidationMessageType | undefined>(
  "error-notification"
);

/**
 * Fetch data from external resource
 * @param url resource URI
 * @param config axios configuration
 * @returns the data fetched
 */
export const useFetch = (url: string | Ref, config: any = {}) => {
  const data: any = ref(config.initialData || {});
  const info = useFetchTo(url, data, config);

  return info;
};

const handleErrorDefault = (error: any) => {
  if (
    error.code === "ERR_BAD_RESPONSE" ||
    error.code === "ERR_NETWORK"
  ) {
    notificationBus.emit({
      fieldId: "internal.server.error",
      errorMsg: "",
    });
  }
  else if (error.code === "ERR_BAD_REQUEST") {
    notificationBus.emit({
      fieldId: "bad.request.error",
      errorMsg: "",
    });
  }
};

/**
 * Fetch data from external resource and store it into the data parameter
 * @param url resource URI
 * @param data the store where to put the data
 * @param config axios configuration
 * @returns a series of parameters containing the data fetched, the response, the error and the loading status
 */
export const useFetchTo = (
  url: string | Ref<string>,
  data: any,
  config: any = {}
) => {
  const response = ref<any>({});
  const error = ref<AxiosError>({});
  const loading = ref<boolean>(false);

  const parameters = {
    ...config,
    headers: {
      "Content-Type": "application/json",
      "Access-Control-Allow-Origin": frontendUrl,
      ...config.headers,
      "Authorization": `Bearer ${ForestClientUserSession.token}`,
    },
  };

  const fetch = () => {
    loading.value = true;
    const actualURL = typeof url === "string" ? url : url.value;
    const controller = new AbortController();
    const asyncResponse = axios
      .request({
        ...parameters,
        url: actualURL,
        baseURL: backendUrl,
        signal: controller.signal,
      })
      .then((result) => {
        response.value = result;
        data.value = result.data;

        // Reset error
        error.value = {};
      })
      .catch((ex) => {
        error.value = ex;
        if (config.skipDefaultErrorHandling) {
          return;
        }
        apiDataHandler.handleErrorDefault();

        // Reset response
        response.value = {};
      })
      .finally(() => {
        loading.value = false;
      });

    return {
      asyncResponse,
      controller,
    };
  };

  !config.skip && fetch();

  const apiDataHandler = {
    response,
    error,
    data,
    loading,
    fetch,
    handleErrorDefault: () => handleErrorDefault(error.value),
  };

  return apiDataHandler;
};

/**
 * Sends a POST request to the backend
 * @param url resource URI
 * @param body the body of the request
 * @param config axios configuration
 * @returns a series of parameters containing the data fetched, the response, the error and the loading status
 */
export const usePost = (url: string, body: any, config: any = {}) => {
  const responseBody: any = ref({});
  const response = ref<any>({});
  const error = ref<AxiosError>({});
  const loading = ref<boolean>(false);

  const parameters = {
    ...config,
    headers: {
      "Content-Type": "application/json",
      "Access-Control-Allow-Origin": frontendUrl,
      ...config.headers,
      "Authorization": `Bearer ${ForestClientUserSession.token}`,
    },
  };

  const fetch = async () => {
    loading.value = true;
    try {
      const result = await axios.request({
        ...parameters,
        url,
        baseURL: backendUrl,
        method: "POST",
        data: body,
      });
      response.value = result;
      responseBody.value = result.data;
    } catch (ex: any) {
      error.value = ex;
      if (config.skipDefaultErrorHandling) {
        return;
      }
      apiDataHandler.handleErrorDefault();
    } finally {
      loading.value = false;
    }
  };
  !config.skip && fetch();

  const apiDataHandler = {
    response,
    error,
    responseBody,
    loading,
    fetch,
    handleErrorDefault: () => handleErrorDefault(error.value),
  };

  return apiDataHandler;
};
