package ca.bc.gov.app.dto;

import lombok.With;

@With
public record SubmissionLocationDto(
	Integer submissionId, 
	String address, 
	String postalCode
) {
}