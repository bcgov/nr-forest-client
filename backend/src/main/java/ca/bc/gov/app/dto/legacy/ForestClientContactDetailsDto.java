package ca.bc.gov.app.dto.legacy;

import java.util.List;
import lombok.With;

@With
public record ForestClientContactDetailsDto(
    String clientNumber,
    Long contactId,
    String contactName,
    String contactTypeCode,
    String contactTypeDesc,
    String businessPhone,
    String secondaryPhone,
    String faxNumber,
    String emailAddress,
    List<String> locationCodes
) {

}
