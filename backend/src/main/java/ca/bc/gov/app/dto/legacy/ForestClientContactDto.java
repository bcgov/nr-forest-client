package ca.bc.gov.app.dto.legacy;

import java.util.List;
import lombok.With;

@With
public record ForestClientContactDto(
    String clientNumber,
    String clientLocnCode,
    List<String> locationCode,
    String contactCode,
    String contactCodeDescription,
    String contactName,
    String businessPhone,
    String secondaryPhone,
    String faxNumber,
    String emailAddress,
    String createdBy,
    String updatedBy,
    Long orgUnit
) {

}
