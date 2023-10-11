import type { SessionProperties, Submitter } from "@/dto/CommonTypesDto";
import { backendUrl } from "@/CoreConstants";
import { toTitleCase } from "@/services/ForestClientService";

class ForestClientUserSession implements SessionProperties {
  public user: Submitter | undefined;

  public constructor() {
    this.loadUser();
  }

  logIn = (provider: string): void => {
    window.location.href = `${backendUrl}/login?code=${provider}`;
  };

  logOut = (): void => {
    this.user = undefined;
    window.location.href = `${backendUrl}/logout`;
  };

  isLoggedIn = (): boolean => {
    return this.loadDetails() !== undefined;
  };

  loadDetails = (): Submitter | undefined => {
    if (this.user === undefined) {
      this.loadUser();
    }
    return this.user;
  };

  private processName = (
    payload: any,
    provider: string
  ): { firstName: string; lastName: string; businessName: string } => {
    const additionalInfo = { firstName: "", lastName: "", businessName: "" };
    if (payload.given_name) {
      additionalInfo.firstName = payload.given_name;
    }

    if (payload.family_name) {
      additionalInfo.lastName = payload.family_name;
    }

    if (
      provider === "bceidbusiness" ||
      (additionalInfo.firstName === "" && additionalInfo.lastName === "")
    ) {
      const name = payload["custom:idp_display_name"];
      const spaceIndex = name.indexOf(" ");
      if (spaceIndex > 0) {
        additionalInfo.lastName = this.splitAtSpace(
          payload["custom:idp_display_name"]
        )[0].replace(/,/g, "");
        additionalInfo.firstName = this.splitAtSpace(
          payload["custom:idp_display_name"]
        )[1].replace(/,/g, "");
        additionalInfo.businessName = payload["custom:idp_business_name"];
      }
    }

    return additionalInfo;
  };

  private splitAtSpace = (name: string): string[] => {
    const nameArray = name.split(" ");
    const nameArrayWithoutSpaces = nameArray.filter((name) => name !== "");
    return nameArrayWithoutSpaces;
  };

  private loadUser = (): void => {
    const accessToken = this.getCookie("idToken");
    if (accessToken) {
      const parsedUser = this.parseJwt(accessToken);
      const address = parsedUser["address"];
      const streetAddress = JSON.parse(address.formatted);

      this.user = {
        name: parsedUser["custom:idp_display_name"],
        provider: parsedUser["custom:idp_name"],
        userId: parsedUser["custom:idp_user_id"],
        birthDate: parsedUser["birthdate"],
        address: {
          locationName: "",
          streetAddress: toTitleCase(streetAddress.street_address),
          city: toTitleCase(streetAddress.locality),
          country: {
            code: streetAddress.country,
            text: ""
          },
          province: {
            code: streetAddress.region,
            text: ""
          },
          postalCode: streetAddress.postal_code
        },
        email: parsedUser.email,
        ...this.processName(parsedUser, parsedUser["custom:idp_name"]),
      };
      this.user.provider = this.user.provider.startsWith("ca.bc.gov.flnr.fam.")
        ? "bcsc"
        : this.user.provider;
    }
  };

  private getCookie = (name: string): string | null => {
    const cookieString = document.cookie;
    if (cookieString !== "") {
      const cookies = cookieString.split(";");
      for (const cookie of cookies) {
        const [cookieName, cookieValue] = cookie.trim().split("=");
        if (cookieName === name) {
          return decodeURIComponent(cookieValue);
        }
      }
    }
    return null;
  };

  private parseJwt = (token: string): any => {
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const decodedPayload = JSON.parse(atob(base64));
    return decodedPayload;
  };
}

export default new ForestClientUserSession();
