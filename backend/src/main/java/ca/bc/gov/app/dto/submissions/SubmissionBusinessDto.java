package ca.bc.gov.app.dto.submissions;

import java.time.LocalDate;

public record SubmissionBusinessDto(
    String businessType,
    String incorporationNumber,
    String clientNumber,
    String organizationName,
    String clientType,
    String goodStandingInd,
    LocalDate birthdate
) {

}
