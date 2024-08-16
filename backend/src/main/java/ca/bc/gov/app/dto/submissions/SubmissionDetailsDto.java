package ca.bc.gov.app.dto.submissions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.With;

@With
public record SubmissionDetailsDto(
    Long submissionId,
    String submissionStatus,
    String submissionType,
    LocalDateTime submittedTimestamp,
    LocalDateTime updateTimestamp,
    LocalDateTime approvedTimestamp,
    String updateUser,
    SubmissionBusinessDto business,
    List<SubmissionContactDto> contact,
    List<SubmissionAddressDto> address,
    Map<String, Object> matchers,
    String rejectionReason,
    String confirmedMatchUserId
) {

}
