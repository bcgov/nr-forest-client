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
  const data = ref(config.initialData || {});
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
  data: Ref<any>,
  config: any = {}
) => {
  const defaultResponse = {};
  const defaultError = {};

  const response = ref<any>(defaultResponse);
  const error = ref<AxiosError>(defaultError);
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

  let hasEverSucceeded = false;

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
        hasEverSucceeded = true;
        response.value = result;
        data.value = result.data;

        // No error - default value
        error.value = defaultError;
      })
      .catch((ex) => {
        // Reset response
        response.value = defaultResponse;

        // Reset data, unless it has never changed. In that case, keep the initial provided data.
        if (hasEverSucceeded) {
          data.value = undefined;
        }
        error.value = ex;
        if (config.skipDefaultErrorHandling) {
          return;
        }
        apiDataHandler.handleErrorDefault();
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

const useMethod = (method: string, url: string, body: any, config: any = {}) => {
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
        method,
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

/**
 * Sends a POST request to the backend
 * @param url resource URI
 * @param body the body of the request
 * @param config axios configuration
 * @returns a series of parameters containing the data fetched, the response, the error and the loading status
 */
export const usePost = (url: string, body: any, config: any = {}) =>
  useMethod("POST", url, body, config);

/**
 * Sends a PATCH request to the backend using the JSON-Patch standard
 * @param url resource URI
 * @param body the body of the request
 * @param config axios configuration
 * @returns a series of parameters containing the data fetched, the response, the error and the loading status
 */
export const useJsonPatch = (url: string, body: any, config: any = {}) =>
  useMethod("PATCH", url, body, {
    ...config,
    headers: {
      ...config.headers,
      "Content-Type": "application/json-patch+json",
    },
  });
