package ca.bc.gov.app.dto;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.With;

@With
public record HistoryLogDto(
    String tableName, 
    String idx, 
    String identifierLabel,
    @JsonIgnore
    String columnName,
    @JsonIgnore
    String oldValue,
    @JsonIgnore
    String newValue,
    LocalDateTime updateTimestamp, 
    String updateUserid,
    String changeType, 
    @JsonIgnore
    String actionCode,
    @JsonIgnore
    String reason,
    List<HistoryLogDetailsDto> details,
    List<HistoryLogReasonsDto> reasons
) {

}
