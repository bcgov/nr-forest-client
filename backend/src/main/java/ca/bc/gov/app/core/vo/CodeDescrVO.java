package ca.bc.gov.app.core.vo;

import java.util.Date;


public record CodeDescrVO(
    String code,
    String description,
    Date effectiveDate,
    Date expiryDate,
    Long order
) {
}
