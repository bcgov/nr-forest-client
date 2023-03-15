package ca.bc.gov.app.dto.client;

import java.util.List;

public record ClientDetailsDto(
    String name,
    String id,
    boolean goodStanding,
    List<ClientDetailsAddressDto> addresses
) {
}
