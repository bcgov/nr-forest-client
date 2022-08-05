import axios from "axios";
import { backendUrl } from "../core/CoreConstants";

export const searchClient = (clientNumber?: string, clientName?: string) => {
  let params = {};
  if (clientNumber && clientNumber !== "") params = { clientNumber };
  if (clientName && clientName !== "") params = { ...params, clientName };

  console.log("backendUrl", backendUrl);
  return axios
    .get(backendUrl + "/clientView/findInViewBy", {
      params,
    })
    .then((response) => {
      console.log("client", response);
      return response;
    })
    .catch((e) => {
      console.log("failed to search client", e);
      return e;
    });
};
