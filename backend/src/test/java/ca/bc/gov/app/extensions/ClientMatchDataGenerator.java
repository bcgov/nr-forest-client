package ca.bc.gov.app.extensions;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
                    .withBusinessName(lastName)
                    .withFirstName(firstName)
                    .withBirthdate(birthdate)
                    .withIdentificationType(idType)
                    .withIdentificationProvince(idProvince)
                    .withClientIdentification(idValue)
                    .withClientType("I")
            );
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
            null
        )
    );

    return dto.withBusinessInformation(
        dto
            .businessInformation()
            .withClientType("I")
    );
  }

  public static ClientSubmissionDto getDtoType(String type) {
    ClientSubmissionDto dto = getDto(null);
    return dto.withBusinessInformation(
        dto
            .businessInformation()
            .withClientType(type)
    );

  }

  public static ClientSubmissionDto getDto(
      ClientAddressDto addressDto
  ) {
    return
        getDto(
            null,
            null,
            null,
            null,
            null,
            null,
            addressDto
        );
  }

  public static ClientSubmissionDto getDto(
      String firstName,
      String lastName,
      LocalDate birthdate,
      String idType,
      String idProvince,
      String idValue,
      ClientAddressDto addressDto
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
            idType,
            idValue,
            null,
            idProvince
        ),
        new ClientLocationDto(
            addressDto == null ? List.of() : List.of(addressDto),
            List.of()
        ),
        null,
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

}
