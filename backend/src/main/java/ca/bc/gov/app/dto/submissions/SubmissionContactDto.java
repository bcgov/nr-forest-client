package ca.bc.gov.app.dto.submissions;

import java.util.Set;

public record SubmissionContactDto(
    Integer index,
    String contactType,
    String firstName,
    String lastName,
    String phoneNumber,
    String emailAddress,
    Set<String> locations,
    String userId
) {

}
