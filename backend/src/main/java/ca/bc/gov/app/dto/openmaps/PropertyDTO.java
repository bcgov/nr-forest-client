package ca.bc.gov.app.dto.openmaps;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PropertyDTO(
    @JsonProperty("FIRST_NATION_FEDERAL_NAME") String firstNationFederalName,
    @JsonProperty("FIRST_NATION_FEDERAL_ID") Integer firstNationFederalId,
    @JsonProperty("ADDRESS_LINE1") String addressLine1,
    @JsonProperty("ADDRESS_LINE2") String addressLine2,
    @JsonProperty("OFFICE_CITY") String officeCity,
    @JsonProperty("OFFICE_PROVINCE") String officeProvince,
    @JsonProperty("OFFICE_POSTAL_CODE") String officePostalCode
) {
}
