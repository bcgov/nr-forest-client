package ca.bc.gov.app.dto;

import org.springframework.data.relational.core.mapping.Column;
import lombok.With;

/**
 * DTO representing location details for a forest client, including addresses, contact numbers, 
 * and additional comments.
 */
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

