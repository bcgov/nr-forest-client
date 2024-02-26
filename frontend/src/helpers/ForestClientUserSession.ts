import type { SessionProperties, Submitter } from "@/dto/CommonTypesDto";
import { toTitleCase } from "@/services/ForestClientService";
import { fetchAuthSession, signInWithRedirect, signOut } from "aws-amplify/auth";
import { cognitoEnvironment} from "@/CoreConstants";

class ForestClientUserSession implements SessionProperties {
  public user: Submitter | undefined;

  logIn = (provider: string): void => {
    console.log("Logging in with provider: ", provider);
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
    const { idToken } = (await fetchAuthSession()).tokens ?? {};
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

}

export default new ForestClientUserSession();
