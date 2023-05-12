package ca.bc.gov.app.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ClientLocationDto(
    List<ClientAddressDto> addresses,

    @Schema(description = "A list of contacts for this address",example = """
         {
          "type": "person",
          "firstName": "JAMES",
          "lastName": "BAXTER",
          "phoneNumber": "123456789"
          "email": "james@email.ca",
        }""")
    List<ClientContactDto> contacts
) {
}
