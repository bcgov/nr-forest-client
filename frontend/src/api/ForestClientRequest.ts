import axios from "axios";
import { backendUrl } from "../core/CoreConstants";

export const searchByClientNumberApi = (clientNumber: string) => {
  return axios
    .get(backendUrl + "/clientView/findInViewByNumber", {
      params: { clientNumber },
    })
    .then((response) => {
      return response.data;
    })
    .catch((e) => {
      console.log("failed to search client by client number", e);
      return e;
    });
};
