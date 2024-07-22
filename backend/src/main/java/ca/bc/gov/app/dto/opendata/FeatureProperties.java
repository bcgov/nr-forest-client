package ca.bc.gov.app.dto.opendata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FeatureProperties(
    @JsonProperty("GmlID") String gmlId,
    @JsonProperty("OBJECTID") int objectId,
    @JsonProperty("Numéro_de_bande___Band_Number") int bandNumber,
    @JsonProperty("Nom_de_bande___Band_Name") String bandName,
    @JsonProperty("COMMUNITY_LOCATION_ID") int communityLocationId,
    @JsonProperty("FIRST_NATION_BC_NAME") String firstNationBCName,
    @JsonProperty("FIRST_NATION_FEDERAL_NAME") String firstNationFederalName,
    @JsonProperty("FIRST_NATION_FEDERAL_ID") int firstNationFederalId,
    @JsonProperty("URL_TO_BC_WEBSITE") String urlToBCWebsite,
    @JsonProperty("URL_TO_FEDERAL_WEBSITE") String urlToFederalWebsite,
    @JsonProperty("URL_TO_FIRST_NATION_WEBSITE") String urlToFirstNationWebsite,
    @JsonProperty("MEMBER_ORGANIZATION_NAMES") String memberOrganizationNames,
    @JsonProperty("LANGUAGE_GROUP") String languageGroup,
    @JsonProperty("BC_REGIONAL_OFFICE") String bcRegionalOffice,
    @JsonProperty("MAPSHEET_NUMBER") String mapsheetNumber,
    @JsonProperty("PREFERRED_NAME") String preferredName,
    @JsonProperty("ALTERNATIVE_NAME_1") String alternativeName1,
    @JsonProperty("ALTERNATIVE_NAME_2") String alternativeName2,
    @JsonProperty("ADDRESS_LINE1") String addressLine1,
    @JsonProperty("ADDRESS_LINE2") String addressLine2,
    @JsonProperty("OFFICE_CITY") String officeCity,
    @JsonProperty("OFFICE_PROVINCE") String officeProvince,
    @JsonProperty("OFFICE_POSTAL_CODE") String officePostalCode,
    @JsonProperty("LOCATION_DESCRIPTION") String locationDescription,
    @JsonProperty("SITE_NAME") String siteName,
    @JsonProperty("SITE_NUMBER") String siteNumber,
    @JsonProperty("COMMENTS") String comments,
    @JsonProperty("SE_ANNO_CAD_DATA") Object seAnnoCadData // Keeping this as Object since it's null in the provided JSON
) {

  public String getNationName() {
    return StringUtils.defaultIfBlank(firstNationBCName(), bandName());
  }

  public int getNationId() {
    return IntStream
        .of(firstNationFederalId(), bandNumber())
        .filter(id -> id > 0)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(0);
  }

}