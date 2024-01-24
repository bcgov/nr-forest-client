package ca.bc.gov.app.dto;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.With;
import org.apache.commons.lang3.tuple.Pair;

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
