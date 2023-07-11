package ca.bc.gov.app.util;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.entity.client.SubmissionContactEntity;
import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationEntity;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientMapper {

  /**
   * Maps a {@link ClientBusinessInformationDto} object to a {@link SubmissionDetailEntity} object,
   * using the specified submission ID.
   *
   * @param submissionId                 the submission ID to be set on the {@link SubmissionDetailEntity}
   * @param clientBusinessInformationDto the {@link ClientBusinessInformationDto} object to be
   *                                     mapped to a {@link SubmissionDetailEntity}
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
        .withBusinessTypeCode(clientBusinessInformationDto.businessType())
        .withClientTypeCode(clientBusinessInformationDto.clientType())
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
        .withName(clientAddressDto.locationName())
        .withSubmissionId(submissionId)
        .withStreetAddress(clientAddressDto.streetAddress())
        .withCountryCode(clientAddressDto.country().value())
        .withProvinceCode(clientAddressDto.province().value())
        .withCityName(clientAddressDto.city())
        .withPostalCode(clientAddressDto.postalCode());
  }

  /**
   * Maps all {@link ClientAddressDto} objects to a {@link SubmissionLocationEntity} list,
   * using the specified submission ID.
   *
   * @param submissionId    the submission ID to be set on the {@link SubmissionLocationEntity}
   * @param clientaddresses the list of {@link ClientAddressDto} object to be
   *                        mapped to a list of {@link SubmissionLocationEntity}
   * @return the {@link SubmissionLocationEntity} object list mapped from {@link ClientAddressDto}
   */
  public static List<SubmissionLocationEntity> mapAllToSubmissionLocationEntity(
      Integer submissionId,
      List<ClientAddressDto> clientaddresses
  ) {
    return
        clientaddresses
            .stream()
            .map(clientAddressDto -> mapToSubmissionLocationEntity(submissionId, clientAddressDto))
            .toList();
  }

  /**
   * Maps a {@link ClientContactDto} object to a {@link SubmissionContactEntity} object.
   *
   * @param clientContactDto the {@link ClientContactDto} object to map to the entity
   * @return a {@link SubmissionContactEntity} object mapped from the input parameters
   */
  public static SubmissionContactEntity mapToSubmissionContactEntity(
      ClientContactDto clientContactDto
  ) {
    return new SubmissionContactEntity()
        .withContactTypeCode(clientContactDto.contactType().value())
        .withFirstName(clientContactDto.firstName())
        .withLastName(clientContactDto.lastName())
        .withBusinessPhoneNumber(clientContactDto.phoneNumber())
        .withEmailAddress(clientContactDto.email());
  }


  public static List<SubmissionContactEntity> mapAllToSubmissionLocationContactEntity(
      List<ClientContactDto> contacts
  ) {
    return
        contacts
            .stream()
            .map(clientContactDto ->
                new SubmissionContactEntity()
                    .withContactTypeCode(clientContactDto.contactType().value())
                    .withFirstName(clientContactDto.firstName())
                    .withLastName(clientContactDto.lastName())
                    .withBusinessPhoneNumber(clientContactDto.phoneNumber())
                    .withEmailAddress(clientContactDto.email())
            )
            .toList();
  }

  public static Integer getLocationIdByName(
      List<SubmissionLocationEntity> locations,
      ClientContactDto contactDto
  ) {
    return
        locations
            .stream()
            .filter(location ->
                contactDto
                    .locationNames()
                    .stream()
                    .anyMatch(valueText -> valueText.text().equals(location.getName()))
            )
            .map(SubmissionLocationEntity::getSubmissionLocationId)
            .findFirst()
            .orElse(0);
  }

}
