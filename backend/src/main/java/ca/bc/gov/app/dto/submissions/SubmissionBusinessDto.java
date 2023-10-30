package ca.bc.gov.app.dto.submissions;

public record SubmissionBusinessDto(
    String businessType,
    String incorporationNumber,
    String clientNumber,
    String organizationName,
    String clientType,
    String goodStandingInd
) {

}
