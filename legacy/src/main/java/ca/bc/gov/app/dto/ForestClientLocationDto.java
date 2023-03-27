package ca.bc.gov.app.dto;

import java.util.List;
import lombok.With;

@With
public record ForestClientLocationDto(
    String clientNumber,
    String code,
    String name,
    String hdbsCompanyCode,
    String addressOne,
    String addressTwo,
    String addressThree,
    String city,
    String province,
    String postalCode,
    String country,
    String businessPhone,
    String homePhone,
    String cellPhone,
    String faxNumber,
    String emailAddress,
    List<ForestClientContactDto> contacts
) {
}
