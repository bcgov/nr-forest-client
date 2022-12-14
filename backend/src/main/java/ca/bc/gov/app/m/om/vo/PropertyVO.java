package ca.bc.gov.app.m.om.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PropertyVO(
    @JsonProperty("FIRST_NATION_FEDERAL_NAME") String firstNationFederalName,
    @JsonProperty("FIRST_NATION_FEDERAL_ID") String firstNationFederalId,
    @JsonProperty("ADDRESS_LINE1") String addressLine1,
    @JsonProperty("ADDRESS_LINE2") String addressLine2,
    @JsonProperty("OFFICE_CITY") String officeCity,
    @JsonProperty("OFFICE_PROVINCE") String officeProvince,
    @JsonProperty("OFFICE_POSTAL_CODE") String officePostalCode
) {
}
