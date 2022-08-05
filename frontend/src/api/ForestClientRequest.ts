import axios from "axios";
import { backendUrl } from "../core/CoreConstants";

export const searchClientApi = (clientNumber?: string, clientName?: string) => {
  let params = {};
  if (clientNumber && clientNumber !== "") params = { clientNumber };
  if (clientName && clientName !== "") params = { ...params, clientName };

  return axios
    .get(backendUrl + "/clientView/findInViewBy", {
      params,
    })
    .then((response) => {
      return response.data;
    })
    .catch((e) => {
      console.log("failed to search client", e);
      return e;
    });
};
