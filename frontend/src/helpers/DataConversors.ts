import { type Address, emptyAddress, locationName } from "@/dto/ApplyClientNumberDto";
import { toSentenceCase } from "@/services/ForestClientService";

export const retrieveClientType = (legalType: string): string => {
  if (legalType) {
    switch (legalType) {
      case "A":
      case "B":
      case "BC":
      case "C":
      case "CP":
      case "EPR":
      case "FOR":
      case "LIC":
      case "REG":
        return "C";
      case "S":
      case "XS":
        return "S";
      case "XCP":
        return "A";
      case "SP":
        return "RSP";
      case "GP":
        return "P";
      case "LP":
      case "XL":
      case "XP":
        return "L";
      default:
        throw new Error("Unknown Legal Type " + legalType);
    }
  } else {
    return "";
  }
};

export const exportAddress = (addresses: Address[]): Address[] => {
  if (addresses && addresses.length > 0) {
    return addresses.map(address => ({
      ...address,
      locationName: toSentenceCase(address.locationName),
    }));
  }
  return [{ ...emptyAddress(), locationName: locationName.text }];
};
