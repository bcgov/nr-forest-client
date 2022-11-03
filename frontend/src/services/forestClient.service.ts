import axios from "axios";
import { backendUrl } from "../core/EnvConstants";

export const searchInViewByClientNumber = (clientNumber: string) => {
  return axios
    .get(backendUrl + "/client/findByNumber", {
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

export const searchInViewAllNonIndividuals = (
  order: string,
  page: number,
  take: number
) => {
  return axios
    .get(backendUrl + "/client/findAllNonIndividuals", {
      params: { order, page, take },
    })
    .then((response) => {
      return response.data;
    })
    .catch((e) => {
      console.log("failed to get non individual client list", e);
      return e;
    });
};
