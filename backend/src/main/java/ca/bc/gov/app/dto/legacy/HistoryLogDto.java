package ca.bc.gov.app.dto.legacy;

import java.time.LocalDateTime;
import java.util.List;
import lombok.With;

@With
public record HistoryLogDto(
    String tableName, 
    String idx, 
    String identifierLabel, 
    LocalDateTime updateTimestamp, 
    String updateUserid,
    String changeType, 
    List<HistoryLogDetailsDto> details,
    List<HistoryLogReasonsDto> reasons
) {

}
