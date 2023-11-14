package ca.bc.gov.app.dto.submissions;

import java.util.Date;

public record SubmissionBusinessDto(
    String businessType,
    String incorporationNumber,
    String clientNumber,
    String organizationName,
    String clientType,
    String goodStandingInd,
    Date birthdate
) {

}
