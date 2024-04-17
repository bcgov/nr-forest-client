/* eslint-disable dot-notation */
import { isMaxSize, isOnlyNumbers, isNotEmpty, isNotEmptyArray } from "@/helpers/validators/GlobalValidators";

const reviewFieldValidations: Record<string, ((value: any) => string)[]> = {};

// This function will return all validators for the field
export const getValidations = (key: string): ((value: any) => string)[] =>
  reviewFieldValidations[key] || [];

const isMaxSizeMsg = (fieldName: string, maxSize: number) =>
  isMaxSize(`The ${fieldName} has a ${maxSize} character limit`)(maxSize);

reviewFieldValidations["reasons"] = [isNotEmptyArray("You must select at least one reason")];

reviewFieldValidations["message"] = [
  isNotEmpty("Matching client number cannot be empty"),
  isOnlyNumbers("Matching client number should be composed of only numbers"),
  isMaxSizeMsg("matching client number", 8),
];
