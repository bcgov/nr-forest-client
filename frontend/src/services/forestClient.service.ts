import axios from "axios";
import { backendUrl } from "../core/CoreConstants";

export const sendConfirmationEmail = (emailTo: String, emailBody: String) => {
  return axios
    .post(backendUrl + "/app/m/ches/sendEmail", null, {
      params: { emailTo, emailBody },
    })
    .then((response) => {
      return response;
    })
    .catch((e) => {
      console.log("Failed to send confirmation email", e);
      return e;
    });
};

export const foundDuplicateForBusiness = async (
  incorporationNumber: String,
  companyName: String
) => {
  return axios
    .get(
      backendUrl + "/app/m/legacyclient/findClientByIncorporationNumberOrName",
      {
        params: { incorporationNumber, companyName },
      }
    )
    .then((response) => {
      if (response.data.length > 0) return true;
      return false;
    })
    .catch((e) => {
      console.log("Failed to check duplicate client for business user", e);
      return e;
    });
};

export const foundDuplicateForIndividual = async (
  firstName: String,
  lastName: String,
  birthdate: String
) => {
  return axios
    .get(backendUrl + "/app/m/legacyclient/findClientByNameAndBirthdate", {
      params: { firstName, lastName, birthdate },
    })
    .then((response) => {
      if (response.data.length > 0) return true;
      return false;
    })
    .catch((e) => {
      console.log("Failed to check duplicate client for individual user", e);
      return e;
    });
};
