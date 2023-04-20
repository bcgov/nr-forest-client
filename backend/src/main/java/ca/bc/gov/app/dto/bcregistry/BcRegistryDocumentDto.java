package ca.bc.gov.app.dto.bcregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.With;

@With
@JsonIgnoreProperties(ignoreUnknown = true)
public record BcRegistryDocumentDto(
    BcRegistryBusinessDto business,
    BcRegistryOfficesDto offices,
    List<BcRegistryPartyDto> parties
) {

  /**
   * Returns a mapping of addresses to sets of parties associated with those addresses. If the
   * offices object is valid, adds all of its addresses to the mapping with an empty set of parties.
   * For each valid party, adds it to the set of parties associated with its mailing or delivery
   * address in the mapping.
   *
   * @return a mapping of addresses to sets of parties associated with those addresses
   */
  public Map<BcRegistryAddressDto, Set<BcRegistryPartyDto>> matchOfficesParties() {

    Map<BcRegistryAddressDto, Set<BcRegistryPartyDto>> results = new HashMap<>();

    // If offices exist and are valid, add all their addresses to the results map
    if (offices != null && offices.isValid()) {
      offices.addresses().forEach(address -> results.put(address, new HashSet<>()));
    }

    // For each party, if it's valid and has a mailing or delivery address,
    // add it to the set of parties associated with that address in the results map
    for (BcRegistryPartyDto party : parties) {
      if (!party.isValid()) {
        continue;
      }

      BcRegistryAddressDto mailingAddress = party.mailingAddress();
      if (mailingAddress != null) {
        results.computeIfAbsent(mailingAddress, key -> new HashSet<>()).add(party);
      }

      BcRegistryAddressDto deliveryAddress = party.deliveryAddress();
      if (deliveryAddress != null) {
        results.computeIfAbsent(deliveryAddress, key -> new HashSet<>()).add(party);
      }
    }

    return results;
  }
}