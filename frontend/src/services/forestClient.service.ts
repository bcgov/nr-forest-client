import axios from "axios";
import { backendUrl, apiKey } from "../core/CoreConstants";

export const searchInViewByClientNumber = (clientNumber: string) => {
  return axios
    .get(backendUrl + "/clientPublicView/findByNumber", {
      params: { clientNumber },
      headers: { "X-API-KEY": apiKey },
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
    .get(backendUrl + "/clientPublicView/findAllNonIndividuals", {
      params: { order, page, take },
      headers: { "X-API-KEY": apiKey },
    })
    .then((response) => {
      return response.data;
    })
    .catch((e) => {
      console.log("failed to get non individual client list", e);
      return e;
    });
};
