package ca.bc.gov.app.dto.bcregistry;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import java.util.List;
import lombok.With;

/**
 * A data transfer object representing the details of a client.
 */
@With
public record ClientDetailsDto(
    String name,
    String id,
    Boolean goodStanding,
    List<ClientAddressDto> addresses,
    List<ClientContactDto> contacts
) {

}
