package ca.bc.gov.app.dto;

import java.util.List;
import lombok.With;

@With
public record ForestClientDetailsDto(
    ForestClientInformationDto client,
    List<ForestClientLocationDto> addresses,
    List<ForestClientContactDto> contacts,
    List<ClientDoingBusinessAsDto> doingBusinessAs
) {

}
