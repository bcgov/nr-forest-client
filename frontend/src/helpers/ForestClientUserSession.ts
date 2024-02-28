import type { SessionProperties, Submitter } from "@/dto/CommonTypesDto";
import { toTitleCase } from "@/services/ForestClientService";
import { fetchAuthSession, signInWithRedirect, signOut } from "aws-amplify/auth";
import { cognitoEnvironment, nodeEnv, cognitoClientId } from "@/CoreConstants";

class ForestClientUserSession implements SessionProperties {
  public user: Submitter | undefined;
  public token: string | undefined;

  logIn = (provider: string): void => {
    signInWithRedirect({ provider: { custom: `${cognitoEnvironment}-${provider}`.toUpperCase() } });
  };

  logOut = (): void => {
    this.user = undefined;
    signOut();
  };

  isLoggedIn = (): boolean => {
    return this.loadDetails() !== undefined;
  };

  loadDetails = (): Submitter | undefined => {    
    return this.user;
  };

  loadUser = async (): Promise<void> => {
    const { idToken } = await this.loadUserToken();
    if (idToken) {
      const parsedUser: any = idToken.payload;  
      const address = parsedUser["address"];
      const streetAddress = address !== undefined ? JSON.parse(address.formatted) : {};

      const provider =parsedUser["custom:idp_name"].startsWith("ca.bc.gov.flnr.fam.")
        ? "bcsc"
        : parsedUser["custom:idp_name"];

      this.user = {
        name: toTitleCase(parsedUser["custom:idp_display_name"]),
        provider: provider,
        userId: `${provider}\\${parsedUser["custom:idp_username"] ?? parsedUser["custom:idp_user_id"]}`,
        businessId: parsedUser["custom:idp_business_id"] ?? "",
        birthdate: parsedUser["birthdate"],
        address: {
          locationName: "",
          streetAddress: toTitleCase(streetAddress.street_address),
          city: toTitleCase(streetAddress.locality),
          country: {
            value: streetAddress.country, //TODO: double check this
            text: ""
          },
          province: {
            value: streetAddress.region, //TODO: double check this
            text: ""
          },
          postalCode: streetAddress.postal_code
        },
        email: parsedUser.email,
        ...this.processName(parsedUser, parsedUser["custom:idp_name"]),
      };
    }else{
      this.user = undefined;
    }
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

  private loadUserToken = async (): Promise<any> => {

    if(nodeEnv !== "test"){
      const cognitoTokens = (await fetchAuthSession()).tokens ?? {};
      this.setUserTokenFromCookie();
      return Promise.resolve(cognitoTokens);
    }else {
      // This is for test only
      this.setUserTokenFromCookie();
      if(this.token){       
        const jwtBody = this.token ? JSON.parse(atob(this.token.split(".")[1])) : null;
        return Promise.resolve({ idToken: { payload:jwtBody } });
      }else{
        return Promise.resolve({ idToken: null });
      }
    }
  }

  private getCookie = (name: string): string => {
    const cookie = document.cookie
      .split(";")
      .find((cookie) => cookie.trim().startsWith(name));
    return cookie ? cookie.split("=")[1] : "";
  };

  private setUserTokenFromCookie = (): void => {
    const baseCookieName = `CognitoIdentityServiceProvider.${cognitoClientId}`;
    const userId = encodeURIComponent(this.getCookie(`${baseCookieName}.LastAuthUser`));
    if(userId){
      const idTokenCookieName = `${baseCookieName}.${userId}.idToken`;
      const idToken = this.getCookie(idTokenCookieName);
      this.token = idToken;        
    }else{
      this.token = undefined;
    }
  }


}

export default new ForestClientUserSession();
