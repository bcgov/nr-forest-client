package ca.bc.gov.app.dto.legacy;


import java.time.LocalDate;
import lombok.With;

@With
public record ForestClientLocationDto(
    String clientNumber,
    String clientLocnCode,
    String clientLocnName,
    String addressOne,
    String addressTwo,
    String addressThree,
    String city,
    String province,
    String postalCode,
    String country,
    String businessPhone,
    String homePhone,
    String cellPhone,
    String faxNumber,
    String emailAddress,
    String locnExpiredInd,
    LocalDate returnedMailDate,
    String trustLocationInd,
    String cliLocnComment,
    String createdBy,
    String updatedBy,
    Long updateOrgUnit,
    Long addOrgUnit
) {

}
