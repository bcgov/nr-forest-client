package ca.bc.gov.app.dto.legacy;

import java.time.LocalDate;
import lombok.With;

@With
public record AuditLogDto(
    String tableName, 
    String idx, 
    String columnName, 
    String oldValue, 
    String newValue, 
    LocalDate updateTimestamp, 
    String updateUserid, 
    String changeType
) {

}
