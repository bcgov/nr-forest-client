package ca.bc.gov.app.dto.bcregistry;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import lombok.With;

@With
@JsonIgnoreProperties(ignoreUnknown = true)
public record BcRegistryAlternateNameDto(
    String entityType,
    String identifier,
    String name,
    ZonedDateTime registeredDate,
    LocalDate startDate
) {

}
