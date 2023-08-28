import {
  isNotEmpty,
  isEmail,
  isPhoneNumber,
  isCanadianPostalCode,
  isUsZipCode,
  isMaxSize,
  isMinSize,
  isOnlyNumbers,
  isNoSpecialCharacters
} from '@/helpers/validators/GlobalValidators'

import type { FormDataDto } from '@/dto/ApplyClientNumberDto'

// We declare here a collection of all validations for every field in the form
const globalValidations : Record<string, Function[]> = {}

// Step 1: Business Information
globalValidations['businessInformation.businessName'] = [isNotEmpty]

// Step 2: Addresses
globalValidations['location.addresses.*.locationName'] = [
  isNotEmpty,
  isMinSize(3),
  isMaxSize(50),
  isNoSpecialCharacters
]
globalValidations['location.addresses.*.country.text'] = [isNotEmpty]
globalValidations['location.addresses.*.province.text'] = [isNotEmpty]
globalValidations['location.addresses.*.city'] = [
  isNotEmpty,
  isMinSize(3),
  isMaxSize(50),
  isNoSpecialCharacters
]
globalValidations['location.addresses.*.streetAddress'] = [isNotEmpty, isMinSize(5), isMaxSize(50)]
globalValidations['location.addresses.*.postalCode(location.addresses.*.country.text === "CA")'] = [isCanadianPostalCode]
globalValidations['location.addresses.*.postalCode(location.addresses.*.country.text === "US")'] = [isUsZipCode]
globalValidations['location.addresses.*.postalCode(location.addresses.*.country.text !== "CA" && country.text !== "US")'] = [isOnlyNumbers, isMinSize(5), isMaxSize(10)]

// Step 3: Contacts

globalValidations['location.contacts.*.locationNames.*.text'] = [isNotEmpty]
globalValidations['location.contacts.*.contactType.text'] = [isNotEmpty]
globalValidations['location.contacts.*.firstName'] = [
  isMinSize(1),
  isMaxSize(25),
  isNotEmpty,
  isNoSpecialCharacters
]
globalValidations['location.contacts.*.lastName'] = [
  isMinSize(1),
  isMaxSize(25),
  isNotEmpty,
  isNoSpecialCharacters
]
globalValidations['location.contacts.*.email'] = [
  isNotEmpty,
  isEmail,
  isMinSize(6),
  isMaxSize(50)
]
globalValidations['location.contacts.*.phoneNumber'] = [
  isNotEmpty,
  isPhoneNumber,
  isMaxSize(15),
  isMinSize(10)
]

// Associate the field with the validators here

// This function will extract the field value from the object
export const getField = (path:string, value: FormDataDto):any => {
  // First we set is in a temporary variable
  let temporaryValue: any = value
  // We split the path by dots and iterate over it
  path.split('.').forEach((key:string) => {
    // If the temporary value is not undefined we dig in
    if (temporaryValue[key] !== undefined) {
      // If the temporary value is an array we iterate over it
      if (Array.isArray(temporaryValue[key])) {
        // Array fields are split by star to indicate that we want to iterate over the array
        // Due to that, we split the path by the star and get the second element containing the rest of the path
        temporaryValue = temporaryValue[key].map((item:any) => getField(path.split('.*.').slice(1).join('.*.'), item))
        // if is not an array we just set the value
      } else { temporaryValue = temporaryValue[key] }
    }
  })

  // At the end we return the value, keep in mind that the value can be an array if the field is an array
  return temporaryValue
}

// This function will return all validators for the field
export const getValidations = (key: string) : Function[] => (globalValidations[key] || [])


// This function will run the validators and return the errors
export const validate = (keys: string[], target: FormDataDto): boolean => {
// For every received key we get the validations and run them
return keys.every((key) => {
  // First we get all validators for that field
  const validations: Function[] = getValidations(key);
  //If there is no corresponding key in the validations we return false
  if(!validations) return false
  // For every validator we run it and check if the result is empty
  return validations.every((validation: Function) => {
    // We split the field key and the condition if it has one
    const [fieldKey, fieldCondition] = key.includes('(') ? key.replace(')','').split('(') : [key, 'true'];
    // We then load the field value
    const fieldValue = getField(fieldKey, target);
    // We define a function that will run the validation if the condition is true
    const buildEval = (condition: string) => condition === 'true' ? 'true' : `target.${condition}`;
    const runValidation = (item: any,condition:string) => eval(condition) ? validation(item) === '' : true;
    // If the field value is an array we run the validation for every item in the array
    if (Array.isArray(fieldValue)) {
      return fieldValue.every((item: any,index:number) => {
        // And sometimes we can end up with another array inside, that's life
        if (Array.isArray(item)) {
          if(item.length === 0) item.push('')
          return item.every((subItem:any) => runValidation(subItem,buildEval(fieldCondition.replace('.*.',`[${index}].`))));
        }
        // If it is not an array here, just validate it
        return runValidation(item,buildEval(fieldCondition.replace('.*.',`[${index}].`)));
      });
    }
    // If the field value is not an array we run the validation for the field
    return runValidation(fieldValue,fieldCondition);
  });
});
}

export const addValidation = (key: string, validation: Function) => {
  if (!globalValidations[key]) globalValidations[key] = [];
  globalValidations[key].push(validation);
}