package ca.bc.gov.app.dto.client;

import java.util.List;

public record ClientLocationDto(List<ClientAddressDto> addresses) {
}
