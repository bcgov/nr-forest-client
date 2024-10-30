package ca.bc.gov.app.dto.bcregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Comparator;
import java.util.List;
import lombok.With;
import org.springframework.util.CollectionUtils;

@With
@JsonIgnoreProperties(ignoreUnknown = true)
public record BcRegistryBusinessDto(
    List<BcRegistryAlternateNameDto> alternateNames,
    Boolean goodStanding,
    Boolean hasCorrections,
    Boolean hasCourtOrders,
    Boolean hasRestrictions,
    String identifier,
    String legalName,
    String legalType,
    String state
) {

  public String getResolvedLegalName() {

    List<BcRegistryAlternateNameDto> names =
        CollectionUtils.isEmpty(alternateNames)
            ? List.of()
            : alternateNames;

    return
        names
            .stream()
            .sorted(Comparator.comparing(BcRegistryAlternateNameDto::registeredDate))
            .map(BcRegistryAlternateNameDto::name)
            .findFirst()
            .orElse(legalName);
  }

}
