package ca.bc.gov.app.util;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientInformationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationContactEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationEntity;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientMapper {

  public static SubmissionDetailEntity mapToSubmissionDetailEntity(
      Integer submissionId,
      ClientSubmissionDto clientSubmissionDto) {

    ClientInformationDto clientInformationDto = clientSubmissionDto.clientInformationDto();

    return new SubmissionDetailEntity()
        .withSubmissionId(submissionId)
        .withIncorporationNumber(clientInformationDto.incorporationNumber())
        .withOrganizationName(clientInformationDto.businessName())
        .withFirstName(clientInformationDto.firstName())
        .withLastName(clientInformationDto.lastName())
        .withClientTypeCode(clientSubmissionDto.clientBusinessTypeDto().clientType())
        .withDateOfBirth(LocalDate.parse(clientInformationDto.birthdate()))
        .withDoingBusinessAsName(clientInformationDto.doingBusinessAsName());
  }

  public static SubmissionLocationEntity mapToSubmissionLocationEntity(
      Integer submissionId,
      ClientAddressDto clientAddressDto) {
    return new SubmissionLocationEntity()
        .withSubmissionId(submissionId)
        .withStreetAddress(clientAddressDto.streetAddress())
        .withCountryCode(clientAddressDto.country())
        .withProvinceCode(clientAddressDto.province())
        .withCityName(clientAddressDto.city())
        .withPostalCode(clientAddressDto.postalCode());
  }

  public static SubmissionLocationContactEntity mapToSubmissionLocationContactEntity(
      Integer submissionLocationId,
      ClientContactDto clientContactDto
  ) {
    return new SubmissionLocationContactEntity()
        .withSubmissionLocationId(submissionLocationId)
        .withFirstName(clientContactDto.firstName())
        .withLastName(clientContactDto.lastName())
        .withContactTypeCode(clientContactDto.contactType())
        .withBusinessPhoneNumber(clientContactDto.businessPhone())
        .withEmailAddress(clientContactDto.email());
  }
}
