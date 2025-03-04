package ca.bc.gov.app.dto;

import lombok.With;

@With
public record ForestClientLocationDetailsDto(
    String clientNumber,
    String clientLocnCode,
    String clientLocnName,
    String addressOne,
    String addressTwo,
    String addressThree,
    String city,
    String provinceCode,
    String provinceDesc,
    String countryCode,
    String countryDesc,
    String postalCode,
    String businessPhone,
    String homePhone,
    String cellPhone,
    String faxNumber,
    String emailAddress,
    String locnExpiredInd,
    String cliLocnComment
) {

}

