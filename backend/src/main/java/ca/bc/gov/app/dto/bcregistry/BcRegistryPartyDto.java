package ca.bc.gov.app.dto.bcregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.With;
import org.apache.commons.lang3.StringUtils;

@With
@JsonIgnoreProperties(ignoreUnknown = true)
public record BcRegistryPartyDto(
    BcRegistryAddressDto deliveryAddress,
    BcRegistryAddressDto mailingAddress,
    BcRegistryOfficerDto officer,
    List<BcRegistryRoleDto> roles
) {
  public boolean isValid() {
    return officer != null && (mailingAddress != null || deliveryAddress != null);
  }

  public Set<BcRegistryAddressDto> addresses() {
    return
        Stream
            .of(
                mailingAddress,
                deliveryAddress
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
  }

  public boolean isMatch(
      String city,
      String country,
      String region,
      String postalCode,
      String streetAddress
  ) {
    BcRegistryAddressDto provided = new BcRegistryAddressDto(
        city,
        country,
        region,
        StringUtils.EMPTY,
        postalCode,
        streetAddress,
        StringUtils.EMPTY,
        StringUtils.EMPTY
    );

    return addresses()
        .stream()
        .anyMatch(provided::equals);
  }

  public boolean isPerson(){
    return officer != null && officer().isPerson();
  }
}
