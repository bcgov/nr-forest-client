/**
 * This file will have control over fetching of everything
 */
import { ref, watch } from "vue";
import { useFetchTo } from "@/services/forestClient.service";
import EventBus, { EventBusEvent } from "@/services/EventBus";

export const conversionFn = (code: any) => { return { value: code.code, text: code.name } };

/**
 * Autocomplete function for the business name
 */
export const useBusinessNameIdAutoComplete = () => {
  //To control data update back to the list
  const data = ref([]);
  //We will listen to changes on the businessNameId text field and do the fetch
  EventBus.addEventListener("businessNameId", (ev: any) => {
    useFetchTo(`/api/orgbook/name/${ev.data}`, data, { method: "get" });
  });
  //We watch for changes on the data to emit back to the datalist
  watch(data, (dataFetched, _) => {
    EventBus.emit("businessNameListId", data.value.map((code: any) => {
      return { value: code.name, text: code.name }
    }));
  });
};

/**
 * Monitor the countryId change to load the provinces
 * @returns a list of provinces/states
 */
export const useCountryIdAutoComplete = () => {
  //To control data update back to the list
  const data = ref([]);
  //We will listen to changes on the country dropdown and do the fetch
  EventBus.addEventListener("countryId", (ev: any) => {
    //We then fetch the provinces for that country into the data ref
    useFetchTo(`/api/clients/activeCountryCodes/${ev.data}?page=0&size=250`, data, { method: 'get' });
  });

  //We return the data
  return { data };
};
