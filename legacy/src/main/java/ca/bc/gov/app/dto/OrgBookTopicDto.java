package ca.bc.gov.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrgBookTopicDto(
    Integer id,
    @JsonProperty("source_id") String sourceId,
    List<OrgBookTopicNameDto> names,
    Boolean inactive
) {
  public String name() {
    if (CollectionUtils.isEmpty(names)) {
      return StringUtils.EMPTY;
    }
    return names
        .stream()
        .filter(entry -> entry.topicNameType().equalsIgnoreCase("entity_name"))
        .map(OrgBookTopicNameDto::text)
        .findFirst()
        .orElse(StringUtils.EMPTY);
  }
}
