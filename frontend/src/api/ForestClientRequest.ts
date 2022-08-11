import axios from "axios";
import { backendUrl } from "../core/CoreConstants";

export const searchInViewByClientNumber = (clientNumber: string) => {
  return axios
    .get(backendUrl + "/clientPublicView/findById", {
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
    .get(backendUrl + "/clientPublicView/findAllNonIndividuals", {
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
