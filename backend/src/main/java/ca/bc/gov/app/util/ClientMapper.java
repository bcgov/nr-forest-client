package ca.bc.gov.app.util;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ClientSubmitterInformationDto;
import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationContactEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationEntity;
import ca.bc.gov.app.entity.client.SubmitterEntity;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientMapper {

  public static SubmitterEntity mapToSubmitterEntity(
      Integer submissionId,
      ClientSubmitterInformationDto clientSubmitter) {
    return new SubmitterEntity()
        .withSubmissionId(submissionId)
        .withFirstName(clientSubmitter.submitterFirstName())
        .withLastName(clientSubmitter.submitterLastName())
        .withPhoneNumber(clientSubmitter.submitterPhoneNumber())
        .withEmailAddress(clientSubmitter.submitterEmail());
  }

  public static SubmissionDetailEntity mapToSubmissionDetailEntity(
      Integer submissionId,
      ClientSubmissionDto clientSubmissionDto) {

    ClientBusinessInformationDto
        clientBusinessInformationDto = clientSubmissionDto.businessInformation();

    return new SubmissionDetailEntity()
        .withSubmissionId(submissionId)
        .withIncorporationNumber(clientBusinessInformationDto.incorporationNumber())
        .withOrganizationName(clientBusinessInformationDto.businessName())
        .withFirstName(clientBusinessInformationDto.firstName())
        .withLastName(clientBusinessInformationDto.lastName())
        .withClientTypeCode(clientSubmissionDto.businessType().clientType().value())
        .withDateOfBirth(LocalDate.parse(clientBusinessInformationDto.birthdate()))
        .withDoingBusinessAsName(clientBusinessInformationDto.doingBusinessAsName());
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
        .withContactTypeCode(clientContactDto.contactType())
        .withContactTypeCode(clientContactDto.contactType())
        .withFirstName(clientContactDto.firstName())
        .withLastName(clientContactDto.lastName())
        .withBusinessPhoneNumber(clientContactDto.phoneNumber())
        .withEmailAddress(clientContactDto.email());
  }
}
