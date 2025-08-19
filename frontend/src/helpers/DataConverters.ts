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
      case "ULC":
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
      case "LL":
      case "LP":
      case "XL":
      case "XP":
        return "L";
      default:
        return null;
    }
  } else {
    return "";
  }
};

export const retrieveLegalTypeDesc = (legalType: string): string => {
  switch (legalType) {
    case "A":
    case "B":
    case "EPR":
    case "REG":
      return "Extraprovincial Company ";
    case "BC":
      return "BC Company";
    case "C":
      return "Continued In Corporation";
    case "CP":
      return "Cooperative";
    case "FOR":
      return "Foreign Registration";
    case "GP":
      return "General Partnership";
    case "LIC":
      return "Licensed (Extra-Pro)";
    case "LL":
      return "Limited Liability Partnership";
    case "LP":
      return "Limited Partnership";
    case "S":
      return "Society";
    case "SP":
      return "Sole Proprietorship";
    case "XS":
      return "Extraprovincial Society";
    case "XCP":
      return "Extraprovincial Cooperative";
    case "XL":
      return "Extraprovincial Limited Liability Partnership";
    case "XP":
      return "Extraprovincial Limited Partnership";
    case "ULC":
      return "Unlimited Liability Company";
    default:
      return legalType + " (Unknown)";
  }
};
