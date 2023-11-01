import { type Address, emptyAddress, locationName } from "@/dto/ApplyClientNumberDto";

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
        return "I";
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
    return addresses;
  }
  return [{ ...emptyAddress(), locationName: locationName.text }];
};
