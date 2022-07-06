import axios from "axios";

const backendUrl = config.VITE_BACKEND_URL || import.meta.env.VITE_BACKEND_URL;

export const sendEmail = (
  emailBody: string,
  emailAttachments:
    | Array<{
        content: string;
        contentType: string;
        encoding: string;
        filename: string;
      }>
    | [],
  emailTo: Array<string>
) => {
  axios
    .post(`${backendUrl}/email`, { emailBody, emailAttachments, emailTo })
    .then((response) => {
      console.log("email", response.data);
    })
    .catch((e) => {
      console.log("failed to send email", e);
    });
};
