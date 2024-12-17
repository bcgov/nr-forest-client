package ca.bc.gov.app.dto;

import java.time.LocalDate;
import lombok.With;

@With
public record AuditLogDto(
    String clientNumber,
    String tableName, 
    String idx, 
    String columnName, 
    String oldValue, 
    String newValue, 
    LocalDate updateTimestamp, 
    String updateUserId, 
    String changeType
) {

}
