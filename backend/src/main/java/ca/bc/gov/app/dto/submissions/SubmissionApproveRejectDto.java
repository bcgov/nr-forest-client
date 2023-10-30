package ca.bc.gov.app.dto.submissions;

import java.util.List;

public record SubmissionApproveRejectDto(
    boolean approved,
    List<String> reasons,
    String message
) {

}
