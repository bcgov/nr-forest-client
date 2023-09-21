package ca.bc.gov.app.dto.client;

import ca.bc.gov.app.models.client.SubmissionStatusEnum;
import ca.bc.gov.app.models.client.SubmissionTypeCodeEnum;
import java.time.LocalDateTime;
import lombok.With;

@With
public record ClientDetailSubmissionDto(
    Integer submissionId,
    SubmissionStatusEnum submissionStatus,
    SubmissionTypeCodeEnum submissionType,
    LocalDateTime submissionDate,
    ClientSubmissionDto submission
    ) {

}
