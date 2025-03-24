package ca.bc.gov.app.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.With;

@With
public record ForestClientContactDetailsDto(
    String clientNumber,
    Long contactId,
    @JsonIgnore
    String locationCodesCsv,
    String contactName,
    String contactTypeCode,
    String contactTypeDesc,
    String businessPhone,
    String secondaryPhone,
    String faxNumber,
    String emailAddress,
    List<String> locationCodes
) {
  public ForestClientContactDetailsDto {
    locationCodes = locationCodesCsv != null && !locationCodesCsv.isBlank()
        ? List.of(locationCodesCsv.split(","))
        : List.of();
  }
}
