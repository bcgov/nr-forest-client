package ca.bc.gov.app.dto.submissions;

import java.time.LocalDate;

public record SubmissionBusinessDto(
    String businessType,
    String registrationNumber,
    String clientNumber,
    String organizationName,
    String clientType,
    String clientTypeDesc,
    String goodStandingInd,
    LocalDate birthdate,
    String district,
    String districtDesc
) {

}
