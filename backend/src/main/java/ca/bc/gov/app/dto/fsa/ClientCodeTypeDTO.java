package ca.bc.gov.app.dto.fsa;

import java.time.LocalDate;


public record ClientCodeTypeDTO(
    String code,
    String description,
    LocalDate effectiveDate,
    LocalDate expiryDate,
    Long order
) {
}
