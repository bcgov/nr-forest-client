package ca.bc.gov.app.dto.bcregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.With;

@With
@JsonIgnoreProperties(ignoreUnknown = true)
public record BcRegistryDocumentRequestResponseDto(
    String businessIdentifier,
    String businessName,
    List<BcRegistryDocumentRequestDocumentDto> documents,
    String paymentStatus,
    String status
) {
}
