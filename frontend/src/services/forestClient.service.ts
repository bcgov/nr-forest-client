import { ref } from "vue";
import axios from "axios";
import type { Address, Contact } from "../dto/ApplyClientNumberDto";
import type { CodeDescrType } from "@/core/CommonTypes";

axios.defaults.headers.common['Access-Control-Allow-Origin'] = '*';
const forestClientBase = import.meta.env.VITE_BACKEND_URL;

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
        url,
        baseURL: forestClientBase,
        ...config
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

export const addNewAddress = (addresses: Address[]) => {
  const blankAddress: Address = {
    streetAddress: "",
    country: {value: "", text:""} as CodeDescrType,
    province: {value: "", text:""} as CodeDescrType,
    city: "",
    postalCode: "",
    contacts: [
      {
        contactType: { value: "", text: "" } as CodeDescrType,
        firstName: "",
        lastName: "",
        phoneNumber: "",
        emailAddress: "",
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
    emailAddress: "",
  };
  
  let newContacts = contacts.push(blankContact);
  return newContacts;
}

// import { backendUrl } from "../core/CoreConstants";

// export const sendConfirmationEmail = (emailTo: String, emailBody: String) => {
//   return axios
//     .post(backendUrl + "/app/m/ches/sendEmail", null, {
//       params: { emailTo, emailBody },
//     })
//     .then((response) => {
//       return response;
//     })
//     .catch((e) => {
//       console.log("Failed to send confirmation email", e);
//       return e;
//     });
// };

// export const foundDuplicateForBusiness = async (
//   incorporationNumber: String,
//   companyName: String
// ) => {
//   return axios
//     .get(
//       backendUrl + "/app/m/legacyclient/findClientByIncorporationNumberOrName",
//       {
//         params: { incorporationNumber, companyName },
//       }
//     )
//     .then((response) => {
//       if (response.data.length > 0) return true;
//       return false;
//     })
//     .catch((e) => {
//       console.log("Failed to check duplicate client for business user", e);
//       return e;
//     });
// };

// export const foundDuplicateForIndividual = async (
//   firstName: String,
//   lastName: String,
//   birthdate: String
// ) => {
//   return axios
//     .get(backendUrl + "/app/m/legacyclient/findClientByNameAndBirthdate", {
//       params: { firstName, lastName, birthdate },
//     })
//     .then((response) => {
//       if (response.data.length > 0) return true;
//       return false;
//     })
//     .catch((e) => {
//       console.log("Failed to check duplicate client for individual user", e);
//       return e;
//     });
// };
