package ca.bc.gov.app.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.With;

@With
public record HistoryLogDto(
    String tableName, 
    String idx, 
    String identifierLabel, 
    String columnName,
    String oldValue, 
    String newValue,
    LocalDateTime updateTimestamp, 
    String updateUserid,
    String changeType, 
    String actionCode,
    String reason,
    List<HistoryLogDetailsDto> details,
    List<HistoryLogReasonsDto> reasons
) {

}
