package ca.bc.gov.app.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AddressCompleteRetrieveListDto(
    @JsonProperty("Items")
    List<AddressCompleteRetrieveDto> items) {
}
