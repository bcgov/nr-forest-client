import { describe, it, expect } from "vitest";
import { retrieveClientType, retrieveLegalTypeDesc } from "@/helpers/DataConverters";

describe("DataConverters.ts", () => {
  it("retrieveClientType should return C when legalType is A", () => {
    const result = retrieveClientType("A");
    expect(result).toEqual("C");
  });
  it("retrieveClientType should return C when legalType is LLC", () => {
    const result = retrieveClientType("LLC");
    expect(result).toEqual("C");
  });
  it("retrieveLegalTypeDesc should return Limited Liability Company when legalType is LLC", () => {
    const result = retrieveLegalTypeDesc("LLC");
    expect(result).toEqual("Limited Liability Company");
  });
  it("retrieveClientType should return I when legalType is SP", () => {
    const result = retrieveClientType("SP");
    expect(result).toEqual("RSP");
  });
  it("retrieveClientType should return P when legalType is GP", () => {
    const result = retrieveClientType("GP");
    expect(result).toEqual("P");
  });
  it("retrieveClientType should return L when legalType is LP", () => {
    const result = retrieveClientType("LP");
    expect(result).toEqual("L");
  });
  it("retrieveClientType should return S when legalType is S", () => {
    const result = retrieveClientType("S");
    expect(result).toEqual("S");
  });
  it("retrieveClientType should return A when legalType is XCP", () => {
    const result = retrieveClientType("XCP");
    expect(result).toEqual("A");
  });
  it("retrieveClientType should return A when legalType is unknown", () => {
    const result = retrieveClientType("Z");
    expect(result).toEqual(null);
  });
});
