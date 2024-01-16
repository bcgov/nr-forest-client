package ca.bc.gov.app.dto.bcregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;
import lombok.With;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
@With
public record BcRegistryAddressDto(
    String addressCity,
    String addressCountry,
    String addressRegion,
    String deliveryInstructions,
    String postalCode,
    String streetAddress,
    String streetAddressAdditional,
    String addressType
) {
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BcRegistryAddressDto that = (BcRegistryAddressDto) o;

    return new EqualsBuilder()
        .append(addressCity, that.addressCity)
        .append(addressCountry, that.addressCountry)
        .append(addressRegion, that.addressRegion)
        .append(postalCode, that.postalCode)
        .append(streetAddress, that.streetAddress)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(addressCity)
        .append(addressCountry)
        .append(addressRegion)
        .append(postalCode)
        .append(streetAddress)
        .toHashCode();
  }

  public boolean isValid() {
    return !Objects.isNull(streetAddress) && !Objects.isNull(postalCode);
  }
  
}
