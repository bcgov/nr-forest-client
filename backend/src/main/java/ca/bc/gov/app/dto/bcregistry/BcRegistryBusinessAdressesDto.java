package ca.bc.gov.app.dto.bcregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BcRegistryBusinessAdressesDto(
    BcRegistryAddressDto mailingAddress,
    BcRegistryAddressDto deliveryAddress
) {
  public boolean isValid(){
    return mailingAddress != null || deliveryAddress != null;
  }

  public Set<BcRegistryAddressDto> addresses(){
    Set<BcRegistryAddressDto> addressDtoSet = new HashSet<>();
    if(mailingAddress != null)
      addressDtoSet.add(mailingAddress.withAddressType("mailing"));
    if(deliveryAddress != null)
      addressDtoSet.add(deliveryAddress.withAddressType("delivery"));
    return addressDtoSet;
  }
}
