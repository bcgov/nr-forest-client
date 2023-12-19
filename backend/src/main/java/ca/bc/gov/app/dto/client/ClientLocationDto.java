package ca.bc.gov.app.dto.client;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record ClientLocationDto(
    List<ClientAddressDto> addresses,

    List<ClientContactDto> contacts
) {

  public Map<String, Object> description() {
    return
        Stream.concat(
                contacts
                    .stream()
                    .map(ClientContactDto::description),
                addresses
                    .stream()
                    .map(ClientAddressDto::description)
            )
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (v1, v2) -> v2
                )
            );
  }
}
