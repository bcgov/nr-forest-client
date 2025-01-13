package ca.bc.gov.app.dto;

import java.time.LocalDateTime;
import lombok.With;

@With
public record AuditLogDto(
    String tableName, 
    String idx, 
    String identifierLabel,
    String columnName, 
    String oldValue, 
    String newValue, 
    LocalDateTime updateTimestamp, 
    String updateUserid, 
    String changeType,
    String reason
) {

}
