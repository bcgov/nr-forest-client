package ca.bc.gov.app.dto.client;

import java.time.LocalDate;

public record ContactTypeCodeDto(
    String code,
    String description,
    LocalDate effectiveDate,
    LocalDate expiryDate) {
}
