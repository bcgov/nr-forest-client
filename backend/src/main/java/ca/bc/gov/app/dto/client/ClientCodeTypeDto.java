package ca.bc.gov.app.dto.client;

import java.time.LocalDate;


public record ClientCodeTypeDto(
    String code,
    String description,
    LocalDate effectiveDate,
    LocalDate expiryDate,
    Long order
) {
}
