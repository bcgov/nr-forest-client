package ca.bc.gov.app.dto.legacy;

import java.time.LocalDateTime;
import lombok.With;

@With
public record AuditLogDto(
    String tableName, 
    String idx, 
    String columnName, 
    String oldValue, 
    String newValue, 
    LocalDateTime updateTimestamp, 
    String updateUserid, 
    String changeType
) {

}
