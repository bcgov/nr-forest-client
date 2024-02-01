package ca.bc.gov.app;

import ca.bc.gov.app.dto.EmailRequestDto;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryOfficerDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryPartyDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryRoleDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.entity.SubmissionContactEntity;
import ca.bc.gov.app.entity.SubmissionDetailEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestConstants {

  public static final SubmissionDetailEntity SUBMISSION_DETAIL = SubmissionDetailEntity
      .builder()
      .submissionDetailId(1)
      .submissionId(1)
      .incorporationNumber("00000000")
      .organizationName("TEST")
      .businessTypeCode("T")
      .clientTypeCode("C")
      .goodStandingInd("Y")
      .build();

  public static final SubmissionInformationDto SUBMISSION_INFORMATION =
      new SubmissionInformationDto(1, "TEST", null, "00000000", "Y", "C");

  public static final EmailRequestDto EMAIL_REQUEST = new EmailRequestDto(
      "ABC1234",
      "Test Corp",
      "testuserid",
      "Test User",
      "testuser@mail.tst",
      "test",
      "Processor Tests",
      Map.of(
          "name", "Test User",
          "business", Map.of(
              "name", "Test Corp",
              "identifier", "ABC1234"
          )
      )
  );

  public static final String EMAIL_REQUEST_JSON = """
      {
        "incorporation": "ABC1234",
        "name": "Test Corp",
        "userId": "testuserid",
        "userName": "Test User",
        "email": "testuser@mail.tst",
        "templateName": "test",
        "subject": "Processor Tests",
        "variables": {
          "name": "Test User",
          "business": {
            "name": "Test Corp",
            "identifier": "ABC1234"
          }
        }
      }
      """;

  public static final SubmissionContactEntity SUBMISSION_CONTACT = SubmissionContactEntity
      .builder()
      .firstName("James")
      .emailAddress("mail@mail.ca")
      .userId("abc1234")
      .build();
  public static final EmailRequestDto EMAIL_REQUEST_DTO = new EmailRequestDto(
      SUBMISSION_DETAIL.getIncorporationNumber(),
      SUBMISSION_DETAIL.getOrganizationName(),
      SUBMISSION_CONTACT.getUserId(),
      SUBMISSION_CONTACT.getFirstName(),
      SUBMISSION_CONTACT.getEmailAddress(),
      "approval",
      "Success",
      Map.of(
          "userName", SUBMISSION_CONTACT.getFirstName(),
          "business", Map.of(
              "name", "TEST",
              "clientNumber", "00001000"
          )
      )
  );
  public static final BcRegistryDocumentDto BCREG_DOC_DATA =
      new BcRegistryDocumentDto(
          List.of(
              new BcRegistryPartyDto(
                  new BcRegistryOfficerDto(
                      "baxterj@baxter.com",
                      "James",
                      "Baxter",
                      "W",
                      "Director"
                  ),
                  List.of(
                      new BcRegistryRoleDto(
                          LocalDate.of(2005, 7, 27),
                          null,
                          "Proprietor"
                      )
                  )
              )
          )
      );

  public static final String BCREG_DOC_REQ_RES = """
      {
          "businessIdentifier": "AA0000001",
          "documents": [
              {
                  "documentKey": "aa0a00a0a",
                  "documentType": "BUSINESS_SUMMARY_FILING_HISTORY",
                  "id": 18315
              }
          ]
      }""";

  public static final String BCREG__RES1 = """
      {
          "parties": [
              {
                  "officer": {
                      "email": "",
                      "firstName": "JAMES",
                      "lastName": "BAXTER",
                      "middleInitial": "G.",
                      "partyType": "person"
                  },
                  "roles": [
                      {
                          "appointmentDate": "1992-09-11",
                          "cessationDate": null,
                          "roleType": "Proprietor"
                      }
                  ]
              }
          ]
      }""";

  public static final String BCREG_RES2 = """
        {
          "errorMessage": "API backend third party service error.",
          "rootCause": "message:ResourceErrorCodes.NOT_FOUND_ERR: no Document found for 7QbI0M6uBxx. "
      }""";

  public static final ForestClientDto CLIENT_ENTITY = new ForestClientDto(
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
      null,
      null,
      null
  ).withClientNumber("00001000")
      .withClientTypeCode("C");

}
