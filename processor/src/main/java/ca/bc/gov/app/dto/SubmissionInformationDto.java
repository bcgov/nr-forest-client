package ca.bc.gov.app.dto;

import java.time.LocalDate;
import lombok.With;

@With
public record SubmissionInformationDto(
    Integer submissionId,
    String corporationName,
    LocalDate dateOfBirth,
    String incorporationNumber,
    String goodStanding,
    String clientType
) {
}
