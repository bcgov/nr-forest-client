package ca.bc.gov.app.dto.legacy;

import java.util.List;
import lombok.With;

/**
 * A Data Transfer Object (DTO) representing the details of a forest client.
 *
 * @param client          The client details.
 * @param addresses       The list of addresses associated with the client.
 * @param contacts        The list of contacts associated with the client.
 * @param doingBusinessAs The list of "doing business as" names associated with the client.
 */
@With
public record ForestClientDetailsDto(
    ForestClientInformationDto client,
    List<ForestClientLocationDetailsDto> addresses,
    List<ForestClientContactDetailsDto> contacts,
    String doingBusinessAs
) {

}