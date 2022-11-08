import axios from "axios";
import { backendUrl } from "../core/CoreConstants";

export const sendConfirmationEmail = (emailTo: String, emailBody: String) => {
  return axios
    .post(backendUrl + "/app/m/ches/sendEmail", null, {
      params: { emailTo, emailBody },
    })
    .then((response) => {
      // console.log("response", response);
      return response;
    })
    .catch((e) => {
      console.log("Failed to send confirmation email", e);
      return e;
    });
};
