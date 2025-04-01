package ca.bc.gov.app.extensions;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientMatchDataGenerator {

  public static ClientSubmissionDto getIndividualDto(
      String firstName,
      String lastName,
      LocalDate birthdate,
      String idType,
      String idProvince,
      String idValue
  ) {

    ClientSubmissionDto dto = getDtoType("I");

    return
        dto
            .withBusinessInformation(
                dto
                    .businessInformation()
                    .withBusinessName(firstName + " " + lastName)
                    .withFirstName(firstName)
                    .withLastName(lastName)
                    .withBusinessType("U")
                    .withBirthdate(birthdate)
                    .withIdentificationType(new ClientValueTextDto(idType, idType))
                    .withIdentificationProvince(idProvince)
                    .withClientIdentification(idValue)
            );
  }

  public static ClientSubmissionDto getIndividualDto(
      String firstName,
      String lastName,
      LocalDate birthdate,
      String idType,
      String idProvince,
      String idValue,
      ClientAddressDto addressDto,
      ClientContactDto contactDto
  ) {

    ClientSubmissionDto dto = getDto(addressDto, contactDto);

    return
        dto
            .withBusinessInformation(
                dto
                    .businessInformation()
                    .withBusinessName(lastName)
                    .withFirstName(firstName)
                    .withBusinessType("U")
                    .withClientType("I")
                    .withBirthdate(birthdate)
                    .withIdentificationType(new ClientValueTextDto(idType, idType))
                    .withIdentificationProvince(idProvince)
                    .withClientIdentification(idValue)
            );
  }

  public static ClientSubmissionDto getRegistered(
      String registrationNumber,
      String businessName,
      String legalType,
      String workSafeBcNumber,
      String doingBusinessAs,
      String clientAcronym,
      String clientType
  ) {

    ClientSubmissionDto dto = getDtoType(clientType);

    return
        dto
            .withBusinessInformation(
                dto
                    .businessInformation()
                    .withRegistrationNumber(registrationNumber)
                    .withBusinessName(businessName)
                    .withBusinessType("R")
                    .withLegalType(legalType)
                    .withWorkSafeBcNumber(workSafeBcNumber)
                    .withDoingBusinessAs(doingBusinessAs)
                    .withClientAcronym(clientAcronym)

            );
  }

  public static ClientSubmissionDto getRegisteredSP(
      String registrationNumber,
      String businessName,
      String workSafeBcNumber,
      String doingBusinessAs,
      String clientAcronym,
      String firstName,
      String lastName,
      LocalDate birthdate
  ) {

    ClientSubmissionDto dto = getDtoType("RSP");

    return
        dto
            .withBusinessInformation(
                dto
                    .businessInformation()
                    .withRegistrationNumber(registrationNumber)
                    .withBusinessName(businessName)
                    .withBusinessType("")
                    .withLegalType("SP")
                    .withWorkSafeBcNumber(workSafeBcNumber)
                    .withDoingBusinessAs(doingBusinessAs)
                    .withClientAcronym(clientAcronym)
                    .withFirstName(firstName)
                    .withLastName(lastName)
                    .withBirthdate(birthdate)
            );
  }

  public static ClientSubmissionDto getOther(
      String businessName,
      String legalType,
      String workSafeBcNumber,
      String clientAcronym,
      String clientType
  ) {

    ClientSubmissionDto dto = getDtoType(clientType);

    return
        dto
            .withBusinessInformation(
                dto
                    .businessInformation()
                    .withBusinessName(businessName)
                    .withBusinessType("U")
                    .withLegalType(legalType)
                    .withWorkSafeBcNumber(workSafeBcNumber)
                    .withClientAcronym(clientAcronym)

            );
  }

  public static ClientSubmissionDto getFirstNations(
      String businessName,
      String workSafeBcNumber,
      String clientAcronym,
      String clientType,
      String registration
  ) {

    ClientSubmissionDto dto = getDtoType(clientType);

    return
        dto
            .withBusinessInformation(
                dto
                    .businessInformation()
                    .withBusinessName(businessName)
                    .withRegistrationNumber(registration)
                    .withBusinessType("U")
                    .withWorkSafeBcNumber(workSafeBcNumber)
                    .withClientAcronym(clientAcronym)

            );
  }

  public static ClientSubmissionDto getRandomData(String type) {
    return getRandomData(type,null,null);
  }

  public static ClientSubmissionDto getRandomData(
      String type,
      ClientAddressDto addressDto,
      ClientContactDto contactDto
  ) {

    return switch (type) {
      case "I" -> getIndividualDto(
          UUID.randomUUID().toString(),
          UUID.randomUUID().toString(),
          LocalDate.now(),
          UUID.randomUUID().toString(),
          UUID.randomUUID().toString(),
          UUID.randomUUID().toString(),
          addressDto,
          contactDto
      );
      case "C", "RSP", "S", "A", "P", "L" -> getRegistered(
          "C1234567",
          UUID.randomUUID().toString(),
          "C",
          StringUtils.EMPTY,
          StringUtils.EMPTY,
          StringUtils.EMPTY,
          type
      )
          .withLocation(getLocationDto(addressDto, contactDto));
      case "G", "F", "U", "R" -> getOther(
          UUID.randomUUID().toString(),
          "C",
          StringUtils.EMPTY,
          StringUtils.EMPTY,
          type
      )
          .withLocation(getLocationDto(addressDto, contactDto));
      default -> getDtoType(type);
    };

  }

  public static ClientSubmissionDto getAddress() {
    ClientSubmissionDto dto = getDto(
        new ClientAddressDto(
            "1234 Fake St",
            null,
            null,
            new ClientValueTextDto("CA", null),
            new ClientValueTextDto("BC", null),
            "Victoria",
            "V8V4K9",
            "2505551234",
            "2505555678",
            "2505559012",
            "a@a.com",
            null,
            0,
            "That Place"
        ),
        null
    );

    return dto.withBusinessInformation(
        dto
            .businessInformation()
            .withClientType("I")
    );
  }

  public static ClientSubmissionDto getContact() {
    ClientSubmissionDto dto = getDto(
        null,
        new ClientContactDto(
            null,
            "Avery",
            "McCallister",
            "9688296499",
            "2218198891",
            "2047810215",
            "amccallister8@php.net",
            0,
            List.of()
        )
    );

    return dto.withBusinessInformation(
        dto
            .businessInformation()
            .withClientType("I")
    );
  }

  public static ClientSubmissionDto getDtoType(String type) {
    ClientSubmissionDto dto = getDto(null, null);
    return dto.withBusinessInformation(
        dto
            .businessInformation()
            .withClientType(type)
    );

  }

  public static ClientSubmissionDto getDto(
      ClientAddressDto addressDto,
      ClientContactDto contactDto
  ) {
    return
        getDto(
            null,
            null,
            null,
            null,
            null,
            null,
            addressDto,
            contactDto
        );
  }

  public static ClientSubmissionDto getDto(
      String firstName,
      String lastName,
      LocalDate birthdate,
      String idType,
      String idProvince,
      String idValue,
      ClientAddressDto addressDto,
      ClientContactDto contactDto
  ) {
    return new ClientSubmissionDto(
        new ClientBusinessInformationDto(
            null,
            "",
            null,
            null,
            null,
            null,
            birthdate,
            null,
            null,
            null,
            null,
            firstName,
            null,
            lastName,
            null,
            new ClientValueTextDto(idType,idType),
            idValue,
            null,
            idProvince,
            null
        ),
        getLocationDto(addressDto, contactDto),
        null
    );
  }

  public static ForestClientDto getForestClientDto(String clientNumber) {
    return new ForestClientDto(
        clientNumber,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );
  }

  public static ClientLocationDto getLocationDto(
      ClientAddressDto addressDto,
      ClientContactDto contactDto
  ) {
    return new ClientLocationDto(
        addressDto == null ? List.of() : List.of(addressDto),
        contactDto == null ? List.of() : List.of(contactDto)
    );
  }

}
