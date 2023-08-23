export const retrieveClientType = (legalType: string): string => {
  switch (legalType) {
    case 'A':
    case 'B':
    case 'BC':
    case 'C':
    case 'CP':
    case 'EPR':
    case 'FOR':
    case 'LIC':
    case 'REG':
      return 'C'
    case 'S':
    case 'XS':
      return 'S'
    case 'XCP':
      return 'A'
    case 'SP':
      return 'I'
    case 'GP':
      return 'P'
    case 'LP':
    case 'XL':
    case 'XP':
      return 'L'
    default:
      throw new Error('Unknown Legal Type.')
  }
}

export const toMixedCase = (inputString: string) =>
  inputString
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')
