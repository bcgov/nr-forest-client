package ca.bc.gov.app.dto.orgbook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CredentialType")
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrgBookCredentialTypeDTO(
    Integer id
) {
}
