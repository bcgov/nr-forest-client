package ca.bc.gov.app.dto.submissions;

public record SubmissionAddressDto(
    Integer index,
    String streetAddress,
    String country,
    String province,
    String city,
    String postalCode,
    String name
) {

}
