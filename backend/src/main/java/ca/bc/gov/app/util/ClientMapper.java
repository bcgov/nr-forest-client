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
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientMapper {

  /**
   * Maps a {@link ClientSubmitterInformationDto} object to a {@link SubmitterEntity} object,
   * using the specified submission ID.
   *
   * @param submissionId    the submission ID to be set on the SubmitterEntity
   * @param clientSubmitter the {@link ClientSubmitterInformationDto} object to be mapped
   *                        to a {@link SubmitterEntity}
   * @return the {@link SubmitterEntity} object mapped from {@link ClientSubmitterInformationDto}
   */
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

  /**
   * Maps a {@link ClientBusinessInformationDto} object to a {@link SubmissionDetailEntity} object,
   * using the specified submission ID.
   *
   * @param submissionId        the submission ID to be set on the {@link SubmissionDetailEntity}
   * @param clientBusinessInformationDto the {@link ClientBusinessInformationDto} object to be
   *                            mapped to a {@link SubmissionDetailEntity}
   * @return the {@link SubmissionDetailEntity}
   * object mapped from {@link ClientBusinessInformationDto}
   */
  public static SubmissionDetailEntity mapToSubmissionDetailEntity(
      Integer submissionId,
      ClientBusinessInformationDto clientBusinessInformationDto) {

    return new SubmissionDetailEntity()
        .withSubmissionId(submissionId)
        .withIncorporationNumber(clientBusinessInformationDto.incorporationNumber())
        .withOrganizationName(clientBusinessInformationDto.businessName())
        .withBusinessTypeCode(clientBusinessInformationDto.businessType().toString())
        .withClientTypeCode(clientBusinessInformationDto.clientType().toString())
        .withGoodStandingInd(clientBusinessInformationDto.goodStanding());
  }

  /**
   * Maps a {@link ClientAddressDto} object to a {@link SubmissionLocationEntity} object,
   * using the specified submission ID.
   *
   * @param submissionId     the submission ID to be set on the {@link SubmissionLocationEntity}
   * @param clientAddressDto the {@link ClientAddressDto} object to be
   *                         mapped to a {@link SubmissionLocationEntity}
   * @return the {@link SubmissionLocationEntity} object mapped from {@link ClientAddressDto}
   */
  public static SubmissionLocationEntity mapToSubmissionLocationEntity(
      Integer submissionId,
      ClientAddressDto clientAddressDto) {
    return new SubmissionLocationEntity()
        .withSubmissionId(submissionId)
        .withStreetAddress(clientAddressDto.streetAddress())
        .withCountryCode(clientAddressDto.country().value())
        .withProvinceCode(clientAddressDto.province().value())
        .withCityName(clientAddressDto.city())
        .withPostalCode(clientAddressDto.postalCode());
  }

  /**
   * Maps a {@link ClientContactDto} object to a {@link SubmissionLocationContactEntity} object.
   *
   * @param submissionLocationId the submission location ID to map to the entity
   * @param clientContactDto     the {@link ClientContactDto} object to map to the entity
   * @return a {@link SubmissionLocationContactEntity} object mapped from the input parameters
   */
  public static SubmissionLocationContactEntity mapToSubmissionLocationContactEntity(
      Integer submissionLocationId,
      ClientContactDto clientContactDto
  ) {
    return new SubmissionLocationContactEntity()
        .withSubmissionLocationId(submissionLocationId)
        .withContactTypeCode(clientContactDto.contactType().value())
        .withFirstName(clientContactDto.firstName())
        .withLastName(clientContactDto.lastName())
        .withBusinessPhoneNumber(clientContactDto.phoneNumber())
        .withEmailAddress(clientContactDto.email());
  }
}
