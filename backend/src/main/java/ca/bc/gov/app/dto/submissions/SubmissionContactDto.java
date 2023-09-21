package ca.bc.gov.app.dto.submissions;

import java.util.List;

public record SubmissionContactDto(
    Integer index,
    String contactType,
    String firstName,
    String lastName,
    String phoneNumber,
    String emailAddress,
    List<String> locations,
    String userId
) {

}
