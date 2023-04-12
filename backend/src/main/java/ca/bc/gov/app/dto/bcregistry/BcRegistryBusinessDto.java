package ca.bc.gov.app.dto.bcregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.With;

@With
@JsonIgnoreProperties(ignoreUnknown = true)
public record BcRegistryBusinessDto(
    Boolean goodStanding,
    Boolean hasCorrections,
    Boolean hasCourtOrders,
    Boolean hasRestrictions,
    String identifier,
    String legalName,
    String legalType,
    String state
) {
}
