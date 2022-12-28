package ca.bc.gov.app.dto.orgbook;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CredentialType")
public record OrgBookCredentialTypeDTO(
    Integer id
) {
}
