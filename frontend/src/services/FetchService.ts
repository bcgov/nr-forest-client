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
  //This is the input provided by the text
  const dataInput = ref("");
  //This is to have the name and ID for usage in other places
  const dataSelection = ref({});
  //We will listen to changes on the businessNameId text field and do the fetch
  EventBus.addEventListener("businessNameId", (ev: any) => {
    dataInput.value = ev.data;
    useFetchTo(`/api/orgbook/name/${ev.data}`, data, { method: "get" });
  });
  //We watch for changes on the data to emit back to the datalist
  watch(data, (dataFetched, _) => {
    dataSelection.value = dataFetched;
    EventBus.emit("businessNameListId", dataFetched.map((code: any) => {
      return { value: code.name, text: code.name }
    }));
  });

  //We watch for changes on both input and data
  //This is to be able to extract the selected data for now
  watch([data, dataInput], (_, __) => {
    if (data.value.length > 0) {
      dataSelection.value = data.value
        .filter((entry: any) => entry.name == dataInput.value)[0];
    }
  });

  return { dataSelection };
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
