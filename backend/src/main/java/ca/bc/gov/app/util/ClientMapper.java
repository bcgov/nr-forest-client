package ca.bc.gov.app.util;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.entity.client.SubmissionContactEntity;
import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationEntity;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientMapper {

  /**
   * Maps a {@link ClientBusinessInformationDto} object to a {@link SubmissionDetailEntity} object,
   * using the specified submission ID.
   *
   * @param submissionId                 the submission ID to be set on the
   *                                     {@link SubmissionDetailEntity}
   * @param clientBusinessInformationDto the {@link ClientBusinessInformationDto} object to be
   *                                     mapped to a {@link SubmissionDetailEntity}
   * @return the {@link SubmissionDetailEntity} object mapped from
   * {@link ClientBusinessInformationDto}
   */
  public static SubmissionDetailEntity mapToSubmissionDetailEntity(
      Integer submissionId,
      ClientBusinessInformationDto clientBusinessInformationDto) {

    return new SubmissionDetailEntity()
        .withSubmissionId(submissionId)
        .withRegistrationNumber(clientBusinessInformationDto.registrationNumber())
        .withOrganizationName(clientBusinessInformationDto.businessName())
        .withBusinessTypeCode(clientBusinessInformationDto.businessType())
        .withClientTypeCode(clientBusinessInformationDto.clientType())
        .withGoodStandingInd(clientBusinessInformationDto.goodStandingInd())
        .withBirthdate(clientBusinessInformationDto.birthdate())
        .withDistrictCode(StringUtils.isBlank(clientBusinessInformationDto.district()) ? null
            : clientBusinessInformationDto.district())
        .withFirstName(clientBusinessInformationDto.firstName())
        .withMiddleName(clientBusinessInformationDto.middleName())
        .withLastName(clientBusinessInformationDto.lastName())
        .withWorkSafeBCNumber(clientBusinessInformationDto.workSafeBcNumber())
        .withDoingBusinessAs(clientBusinessInformationDto.doingBusinessAs())
        .withClientAcronym(clientBusinessInformationDto.clientAcronym())
        .withNotes(clientBusinessInformationDto.notes())
        .withIdentificationTypeCode(
            clientBusinessInformationDto.identificationType() != null
                && StringUtils.isNotBlank(clientBusinessInformationDto.identificationType().value())
                ? clientBusinessInformationDto.identificationType().value()
                : null
        )
        .withClientIdentification(
            StringUtils.isNotBlank(clientBusinessInformationDto.clientIdentification())
                ? clientBusinessInformationDto.clientIdentification()
                : null
        )
        .withProvinceCode(
            StringUtils.isNotBlank(clientBusinessInformationDto.identificationProvince())
                ? clientBusinessInformationDto.identificationProvince()
                : null
        )
        .withCountryCode(
            StringUtils.isNotBlank(clientBusinessInformationDto.identificationCountry())
                ? clientBusinessInformationDto.identificationCountry()
                : null
        );
  }

  /**
   * Maps a {@link ClientAddressDto} object to a {@link SubmissionLocationEntity} object, using the
   * specified submission ID.
   *
   * @param submissionId     the submission ID to be set on the {@link SubmissionLocationEntity}
   * @param clientAddressDto the {@link ClientAddressDto} object to be mapped to a
   *                         {@link SubmissionLocationEntity}
   * @return the {@link SubmissionLocationEntity} object mapped from {@link ClientAddressDto}
   */
  public static SubmissionLocationEntity mapToSubmissionLocationEntity(
      Integer submissionId,
      ClientAddressDto clientAddressDto
  ) {
    return new SubmissionLocationEntity()
        .withName(clientAddressDto.locationName())
        .withSubmissionId(submissionId)
        .withStreetAddress(clientAddressDto.streetAddress())
        .withCountryCode(clientAddressDto.country().value())
        .withProvinceCode(clientAddressDto.province().value())
        .withCityName(clientAddressDto.city())
        .withPostalCode(clientAddressDto.postalCode())
        .withBusinessPhoneNumber(clientAddressDto.businessPhoneNumber())
        .withSecondaryPhoneNumber(clientAddressDto.secondaryPhoneNumber())
        .withFaxNumber(clientAddressDto.faxNumber())
        .withEmailAddress(clientAddressDto.emailAddress())
        .withComplementaryAddress1(clientAddressDto.complementaryAddressOne())
        .withComplementaryAddress2(clientAddressDto.complementaryAddressTwo())
        .withNotes(clientAddressDto.notes());
  }

  /**
   * Maps all {@link ClientAddressDto} objects to a {@link SubmissionLocationEntity} list, using the
   * specified submission ID.
   *
   * @param submissionId    the submission ID to be set on the {@link SubmissionLocationEntity}
   * @param clientAddresses the list of {@link ClientAddressDto} object to be mapped to a list of
   *                        {@link SubmissionLocationEntity}
   * @return the {@link SubmissionLocationEntity} object list mapped from {@link ClientAddressDto}
   */
  public static List<SubmissionLocationEntity> mapAllToSubmissionLocationEntity(
      Integer submissionId,
      List<ClientAddressDto> clientAddresses
  ) {
    return
        clientAddresses
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
        .withSecondaryPhoneNumber(clientContactDto.secondaryPhoneNumber())
        .withFaxNumber(clientContactDto.faxNumber())
        .withEmailAddress(clientContactDto.email());
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

  public static Integer getLocationIdByName(
      List<SubmissionLocationEntity> locations,
      String locationName
  ) {
    return
        locations
            .stream()
            .filter(location -> locationName.equalsIgnoreCase(location.getName()))
            .map(SubmissionLocationEntity::getSubmissionLocationId)
            .findFirst()
            .orElse(0);
  }

  public static Map<String, String> parseName(String displayName, String provider) {
    String[] nameParts =
        displayName.contains(",")
            ? displayName.split(",")
            : displayName.split(" ");

    if ("IDIR".equalsIgnoreCase(provider) && nameParts.length >= 2) {

      String firstName = nameParts[1].replaceAll("\\s+[^\\s:]+:[^\\s:]+$", StringUtils.EMPTY).trim();
      String lastName = nameParts[0].trim();

      return
          Map.of(
              ApplicationConstant.FIRST_NAME, firstName.split(" ")[0].trim(),
              ApplicationConstant.LAST_NAME,

              Stream.concat(
                      Stream
                          .of(Arrays.copyOfRange(firstName.split(" "), 1, firstName.split(" ").length)),
                      Stream.of(lastName)
                  )
                  .filter(StringUtils::isNotBlank)
                  .collect(Collectors.joining(" "))
          );

    } else if (nameParts.length >= 2) {
      return Map.of(
          ApplicationConstant.FIRST_NAME, nameParts[0].trim(),
          ApplicationConstant.LAST_NAME, String.join(" ", Arrays.copyOfRange(nameParts, 1, nameParts.length))
      );
    }

    return Map.of(
        ApplicationConstant.FIRST_NAME, nameParts[0],
        ApplicationConstant.LAST_NAME, ""
    );
  }

}
