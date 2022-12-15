package ca.bc.gov.app.core.dto;

import java.util.Date;


public record CodeDescrDTO(
    String code,
    String description,
    Date effectiveDate,
    Date expiryDate,
    Long order
) {
}
