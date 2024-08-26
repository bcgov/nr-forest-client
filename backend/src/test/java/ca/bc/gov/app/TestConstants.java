package ca.bc.gov.app;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.EmailRequestDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestConstants {

  public static final String CHES_SUCCESS_MESSAGE = """      
      {
         "messages": [
           {
             "msgId": "00000000-0000-0000-0000-000000000000",
             "tag": "tag",
             "to": [
               "baz@gov.bc.ca"
             ]
           }
         ],
         "txId": "00000000-0000-0000-0000-000000000000"
      }
       """;

  public static final String CHES_TOKEN_MESSAGE = """      
      {
          "access_token": "a.b.c",
          "expires_in": 300,
          "refresh_expires_in": 0,
          "token_type": "Bearer",
          "not-before-policy": 0,
          "scope": ""
      }""";

  public static final String CHES_400_MESSAGE = """
      {
        "type": "https://httpstatuses.com/400",
        "title": "Bad Request",
        "status": 400,
        "detail": "string"
      }
      """;

  public static final String CHES_422_MESSAGE = """
      {
         "type": "https://httpstatuses.com/422",
         "title": "Unprocessable Entity",
         "status": 422,
         "detail": "string",
         "errors": [
           {
             "value": "utf-8x",
             "message": "Invalid value `encoding`."
           }
         ]
       }
      """;

  public static final String CHES_500_MESSAGE = """
      {
        "type": "https://httpstatuses.com/500",
        "title": "Internal Server Error",
        "status": 500,
        "detail": "string"
      }
      """;

  public static final String BCREG_NAMELOOKUP_OK_RESPONSE = """
      [
        {
          "code": "BC0772006",
          "name": "U3 POWER CORP.",
          "status": "ACTIVE",
          "legalType": "SP"
        }
      ]""";

  public static final String BCREG_NAMELOOKUP_EMPTY = "[]";

  public static final String ORGBOOK_INCORP_EMPTY = """
      {
          "facets": {
              "fields": {
                  "legalType": [
                      {
                          "count": 1,
                          "value": "SP"
                      }
                  ],
                  "status": [
                      {
                          "count": 1,
                          "value": "ACTIVE"
                      }
                  ]
              }
          },
          "searchResults": {
              "queryInfo": {
                  "categories": {
                      "legalType": "",
                      "status": ""
                  },
                  "query": {
                      "bn": "",
                      "identifier": "",
                      "name": "",
                      "value": "BC0000000"
                  },
                  "rows": 100,
                  "start": 0
              },
              "results": [],
              "totalResults": 0
          }
      }""";

  public static final String ORGBOOK_INCORP_OK = """
      {
          "facets": {
              "fields": {
                  "legalType": [
                      {
                          "count": 1,
                          "value": "SP"
                      }
                  ],
                  "status": [
                      {
                          "count": 1,
                          "value": "ACTIVE"
                      }
                  ]
              }
          },
          "searchResults": {
              "queryInfo": {
                  "categories": {
                      "legalType": "",
                      "status": ""
                  },
                  "query": {
                      "bn": "",
                      "identifier": "",
                      "name": "",
                      "value": "BC0772006"
                  },
                  "rows": 100,
                  "start": 0
              },
              "results": [
                  {
                      "bn": "",
                      "identifier": "BC0772006",
                      "legalType": "SP",
                      "name": "U3 POWER CORP.",
                      "score": 10.930866,
                      "status": "ACTIVE"
                  }
              ],
              "totalResults": 1
          }
      }""";

  public static final String ORGBOOK_INCORP_OK_RESPONSE = """
      {
        "code": "BC0772006",
        "name": "U3 POWER CORP.",
        "status": "ACTIVE",
        "legalType": "SP"
      }""";

  public static final String BCREG_NOK = """
      {
          "errorMessage": "API backend third party service error.",
          "rootCause": "message:AA0000001 not found "
      }""";

  public static final String BCREG_DETAIL_OK = """
      {
          "business": {
              "adminFreeze": false,
              "arMaxDate": "2006-12-31",
              "arMinDate": "2006-01-01",
              "associationType": null,
              "complianceWarnings": [],
              "fiscalYearEndDate": "2022-10-12",
              "foundingDate": "2005-07-27T07:00:00+00:00",
              "goodStanding": true,
              "hasCorrections": false,
              "hasCourtOrders": false,
              "hasRestrictions": false,
              "identifier": "AA0000001",
              "lastAddressChangeDate": "2005-07-27",
              "lastAnnualGeneralMeetingDate": "",
              "lastAnnualReportDate": "",
              "lastDirectorChangeDate": "2005-07-27",
              "lastLedgerTimestamp": "2022-10-12T20:03:19.229545+00:00",
              "lastModified": "2005-07-27T07:00:00+00:00",
              "legalName": "SAMPLE COMPANY",
              "legalType": "SP",
              "naicsCode": null,
              "naicsDescription": "DEMO APP",
              "naicsKey": null,
              "nextAnnualReport": "2006-07-27T07:00:00+00:00",
              "startDate": "2005-06-17",
              "state": "ACTIVE",
              "submitter": "AB12345",
              "warnings": []
          }
      }
      """;

  public static final String BCREG_RESPONSE_OK = """
      {
        "name": "SAMPLE COMPANY",
        "id": "AA0000001",
        "goodStanding": true,
        "addresses": [
          {
            "locationName": "MAILING ADDRESS",
            "streetAddress": "501 Belleville Street",
            "country": {"value":"CA","text":"Canada"},
            "province": {"value":"BC","text":"British Columbia"},
            "city": "VICTORIA",
            "postalCode": "V8V1X4",
            "index": 0
          }
        ],
        "contacts": [
          {
            "contactType": null,
            "firstName": "JAMES",
            "lastName": "BAXTER",
            "phoneNumber": "",
            "email": "",
            "index": 1,
            "locationNames": [{
            "text": "MAILING ADDRESS",
            "value": "0"
            }]
          }
        ]
      }""";

  public static final String BCREG_RESPONSE_NOCONTACTOK = """
      {
        "name": "SAMPLE COMPANY",
        "id": "AA0000001",
        "goodStanding": true,
        "addresses": [
          {
            "locationName": "MAILING ADDRESS",
            "streetAddress": "501 Belleville Street",
            "country": {"value":"CA","text":"Canada"},
            "province": {"value":"BC","text":"British Columbia"},
            "city": "VICTORIA",
            "postalCode": "V8V1X4",
            "index": 0
          }
        ],
        "contacts": []
      }""";

  public static final String BCREG_RESPONSE_NOK = "No data found for client number AA0000001";

  public static final String BCREG_400 = """
      {
          "errorMessage": "API security error: API key check failed.",
          "rootCause": "fault:faultstring:Failed to resolve API Key variable request.header.x-apikey,detail:errorcode:steps.oauth.v2.FailedToResolveAPIKey"
      }""";

  public static final String BCREG_401 = """
      {
          "errorMessage": "API security error: API key check failed.",
          "rootCause": "fault:faultstring:Invalid ApiKey,detail:errorcode:oauth.v2.InvalidApiKey"
      }""";

  public static final String BCREG_RESPONSE_401 = "Provided access token is missing or invalid";

  public static final String BCREG_DOC_REQ = """
      {
        "documentAccessRequest":{
          "documents":[
            {"type":"BUSINESS_SUMMARY_FILING_HISTORY"}
          ]
        }
      }""";

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

  public static final String BCREG_DOC_DATA = """
      {
          "business": {
              "goodStanding": true,
              "identifier": "AA0000001",
              "legalName": "SAMPLE COMPANY",
              "legalType": "SP",
              "state": "ACTIVE"
          },
          "offices": {
              "businessOffice": {
                  "deliveryAddress": {
                      "addressCity": "VICTORIA",
                      "addressCountry": "Canada",
                      "addressCountryDescription": "Canada",
                      "addressRegion": "BC",
                      "deliveryInstructions": "",
                      "postalCode": "V8V1X4",
                      "streetAddress": "501 Belleville Street",
                      "streetAddressAdditional": ""
                  },
                  "mailingAddress": {
                      "addressCity": "VICTORIA",
                      "addressCountry": "Canada",
                      "addressCountryDescription": "Canada",
                      "addressRegion": "BC",
                      "deliveryInstructions": "",
                      "postalCode": "V8V1X4",
                      "streetAddress": "501 Belleville Street",
                      "streetAddressAdditional": ""
                  }
              }
          },
          "parties": [
              {
                  "deliveryAddress": {
                      "addressCity": "VICTORIA",
                      "addressCountry": "Canada",
                      "addressCountryDescription": "Canada",
                      "addressRegion": "BC",
                      "deliveryInstructions": "",
                      "postalCode": "V8V1X4",
                      "streetAddress": "501 Belleville Street",
                      "streetAddressAdditional": ""
                  },
                  "mailingAddress": {
                      "addressCity": "VICTORIA",
                      "addressCountry": "Canada",
                      "addressCountryDescription": "Canada",
                      "addressRegion": "BC",
                      "deliveryInstructions": "",
                      "postalCode": "V8V1X4",
                      "streetAddress": "501 Belleville Street",
                      "streetAddressAdditional": ""
                  },
                  "officer": {
                      "email": "",
                      "firstName": "JAMES",
                      "lastName": "BAXTER",
                      "middleInitial": "middleInitial",
                      "partyType": "person"
                  },
                  "roles": [
                      {
                          "appointmentDate": "2005-07-27",
                          "cessationDate": null,
                          "roleType": "Director"
                      }
                  ]
              }
          ],
          "registrarInfo": {
              "endDate": null,
              "name": "Wattles",
              "startDate": "2022-06-01T00:00:00",
              "title": "Registrar of Companies"
          }
      }""";

  public static final String BCREG_DOC_DATA_SPORG = """
      {
         "business": {
           "alternateNames": [
             {
               "entityType": "SP",
               "identifier": "FM00004455",
               "name": "JAMES BAXTER WOOD HANDCRAFTED FURNITURE",
               "registeredDate": "2009-11-30T08:00:00+00:00",
               "startDate": "2009-10-01"
             }
           ],
           "goodStanding": true,
           "identifier": "FM00004455",
           "legalName": "SAMPLE HOLDINGS LTD.",
           "legalType": "SP",
           "state": "ACTIVE"
         },
         "offices": {
           "businessOffice": {
             "deliveryAddress": {
               "addressCity": "VICTORIA",
               "addressCountry": "Canada",
               "addressCountryDescription": "Canada",
               "addressRegion": "BC",
               "deliveryInstructions": "",
               "postalCode": "V8V1X4",
               "streetAddress": "501 Belleville Street",
               "streetAddressAdditional": ""
             },
             "mailingAddress": {
               "addressCity": "VICTORIA",
               "addressCountry": "Canada",
               "addressCountryDescription": "Canada",
               "addressRegion": "BC",
               "deliveryInstructions": "",
               "postalCode": "V8V1X4",
               "streetAddress": "501 Belleville Street",
               "streetAddressAdditional": ""
             }
           }
         },
         "parties": [
           {
             "deliveryAddress": {
               "addressCity": "VICTORIA",
               "addressCountry": "Canada",
               "addressCountryDescription": "Canada",
               "addressRegion": "BC",
               "deliveryInstructions": "",
               "postalCode": "V8V1X4",
               "streetAddress": "501 Belleville Street",
               "streetAddressAdditional": ""
             },
             "mailingAddress": {
               "addressCity": "VICTORIA",
               "addressCountry": "Canada",
               "addressCountryDescription": "Canada",
               "addressRegion": "BC",
               "deliveryInstructions": "",
               "postalCode": "V8V1X4",
               "streetAddress": "501 Belleville Street",
               "streetAddressAdditional": ""
             },
             "officer": {
               "email": "",
               "identifier": "BC0000001",
               "organizationName": "SAMPLE HOLDINGS LTD.",
               "partyType": "organization"
             },
             "roles": [
               {
                 "appointmentDate": "1985-07-23",
                 "cessationDate": null,
                 "roleType": "Proprietor"
               }
             ]
           }
         ],
         "registrarInfo": {
           "endDate": null,
           "name": "Wattles",
           "startDate": "2022-06-01T00:00:00",
           "title": "Registrar of Companies"
         }
       }""";

  public static final String SUBMISSION_LIST_CONTENT = """
      [
        {
          "id":1,
          "requestType":"Submission pending processing",
          "name":"Goldfinger",
          "clientType":"General Partnership",
          "updated":"%s",
          "user":"Test User",
          "status":"New"
        }
      ]""";
  public static final String SUBMISSION_LIST_CONTENT_EMPTY = "[]";
  public static final String LEGACY_OK = """
      [
          {
              "clientNumber": "00000002",
              "clientName": "SAMPLE COMPANY",
              "legalFirstName": null,
              "legalMiddleName": null,
              "clientStatusCode": "ACT",
              "clientTypeCode": "C",
              "clientIdTypeCode": "BCDL",
              "clientIdentification": null,
              "registryCompanyTypeCode": "AA",
              "corpRegnNmbr": "0000001",
              "clientAcronym": null,
              "wcbFirmNumber": null,
              "ocgSupplierNmbr": null,
              "clientComment": "C v."
          }
      ]""";
  public static final String LEGACY_EMPTY = "[]";

  public static final String BCREG_RESPONSE_DUP =
      "SAMPLE COMPANY already exists with the Incorporation number AA0000001 and client number 00000002";


  public static final ClientSubmissionDto REGISTERED_BUSINESS_SUBMISSION_DTO =
      new ClientSubmissionDto(
          new ClientBusinessInformationDto(
              "1234",
              "Goldfinger",
              "R",
              "RSP",
              "Y",
              "SP",
              LocalDate.now().minusYears(20),
              "DCC",
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
          ),
          new ClientLocationDto(
              List.of(
                  new ClientAddressDto(
                      "3570 S Las Vegas Blvd",
                      null,
                      null,
                      new ClientValueTextDto("US", ""),
                      new ClientValueTextDto("NV", ""),
                      "Las Vegas", "89109",
                      null,
                      null,
                      null,
                      null,
                      null,
                      0,
                      "Billing Address"
                  )
              ),
              List.of(
                  new ClientContactDto(
                      new ClientValueTextDto("LP", "LP"),
                      "James",
                      "Bond",
                      "9876543210",
                      null,
                      null,
                      "bond_james_bond@007.com",
                      0,
                      List.of(
                          new ClientValueTextDto(
                              "0",
                              "Billing Address"
                          )
                      )
                  )
              )
          ),
          "ABC123",
          "Bond"
      );

  public static final ClientSubmissionDto UNREGISTERED_BUSINESS_SUBMISSION_DTO =
      new ClientSubmissionDto(
          new ClientBusinessInformationDto(
              "",
              "James Baxter",
              "U",
              "I",
              "",
              "SP",
              LocalDate.of(1975, 1, 31),
              "DCC",
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
          ),
          new ClientLocationDto(
              List.of(
                  new ClientAddressDto(
                      "3570 S Las Vegas Blvd",
                      null,
                      null,
                      new ClientValueTextDto("US", ""),
                      new ClientValueTextDto("NV", ""),
                      "Las Vegas", "89109",
                      null,
                      null,
                      null,
                      null,
                      null,
                      0,
                      "Billing Address"
                  )
              ),
              List.of(
                  new ClientContactDto(
                      new ClientValueTextDto("LP", "LP"),
                      "James",
                      "Bond",
                      "9876543210",
                      null,
                      null,
                      "bond_james_bond@007.com",
                      0,
                      List.of(
                          new ClientValueTextDto(
                              "0",
                              "Billing Address"
                          )
                      )
                  )
              )
          ),
          "ABC123",
          "Bond"
      );

  public static final ClientSubmissionDto UNREGISTERED_BUSINESS_SUBMISSION_MULTI_DTO =
      new ClientSubmissionDto(
          new ClientBusinessInformationDto(
              "",
              "James Baxter",
              "U",
              "I",
              "",
              "SP",
              LocalDate.of(1975, 1, 31),
              "DCC",
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
          ),
          new ClientLocationDto(
              List.of(
                  new ClientAddressDto(
                      "3570 S Las Vegas Blvd",
                      null,
                      null,
                      new ClientValueTextDto("US", ""),
                      new ClientValueTextDto("NV", ""),
                      "Las Vegas", "89109",
                      null,
                      null,
                      null,
                      null,
                      null,
                      0,
                      "Billing Address"
                  )
              ),
              List.of(
                  new ClientContactDto(
                      new ClientValueTextDto("LP", "LP"),
                      "James",
                      "Bond",
                      "9876543210",
                      null,
                      null,
                      "bond_james_bond@007.com",
                      0,
                      List.of(
                          new ClientValueTextDto(
                              "0",
                              "Billing Address"
                          )
                      )
                  ),
                  new ClientContactDto(
                      new ClientValueTextDto("BL", "BL"),
                      "James",
                      "Baxter",
                      "9826543210",
                      null,
                      null,
                      "jbaxter@007.com",
                      1,
                      List.of(
                          new ClientValueTextDto(
                              "0",
                              "Billing Address"
                          )
                      )
                  )
              )
          ),
          "ABC123",
          "Bond"
      );

  public static final ClientSubmissionDto UNREGISTERED_BUSINESS_SUBMISSION_BROKEN_DTO =
      new ClientSubmissionDto(
          new ClientBusinessInformationDto(
              "",
              "forest1",
              "U",
              "USP",
              "",
              "SP",
              LocalDate.of(1975, 1, 31),
              "DCC",
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
          ),
          new ClientLocationDto(
              List.of(
                  new ClientAddressDto(
                      "3570 S Las Vegas Blvd",
                      null,
                      null,
                      new ClientValueTextDto("US", ""),
                      new ClientValueTextDto("NV", ""),
                      "Las Vegas", "89109",
                      null,
                      null,
                      null,
                      null,
                      null,
                      0,
                      "Billing Address"
                  )
              ),
              List.of(
                  new ClientContactDto(
                      new ClientValueTextDto("LP", "LP"),
                      "James",
                      "Bond",
                      "9876543210",
                      null,
                      null,
                      "bond_james_bond@007.com",
                      0,
                      List.of(
                          new ClientValueTextDto(
                              "0",
                              "Billing Address"
                          )
                      )
                  )
              )
          ),
          "ABC123",
          "Bond"
      );

  public static final EmailRequestDto EMAIL_REQUEST =
      new EmailRequestDto(
          "XX1234567",
          "Example Inc.",
          "12345",
          "John Doe",
          "johndoe@example.com",
          "match",
          "Subject example",
          Map.of(
              "key1", "value1",
              "key2", 42,
              "key3", true,
              "userName", "John Doe",
              "name", "Example Inc."
          )
      );

  public static final String AUTH_RESPONSE_OK = """
      {
        "access_token": "a.b.c",
        "expires_in": 300,
        "refresh_token": "d.e.f",
        "token_type": "Bearer",
        "id_token": "g.h.i"
      }""";

  public static final String SUBMISSION_DETAILS = """
      {
        "submissionId": 2,
        "submissionStatus": "New",
        "submissionType": "Submission pending processing",
        "submittedTimestamp": "2023-09-21T12:44:28.840456",
        "updateTimestamp": "2023-09-21T12:44:27.829389",
        "updateUser": "Test User",
        "business": {
          "businessType": "Unegistered Business",
          "registrationNumber": "",
          "clientNumber": null,
          "organizationName": "James",
          "clientType": "Individual",
          "goodStanding": ""
        },
        "contact": [
          {
            "index": 0,
            "contactType": "Limited Partner",
            "firstName": "James",
            "lastName": "Bond",
            "phoneNumber": "9876543210",
            "emailAddress": "bond_james_bond@007.com",
            "locations": [
              "Billing Address"
            ],
            "userId": "testUserId"
          }
        ],
        "address": [
          {
            "index": 0,
            "streetAddress": "3570 S Las Vegas Blvd",
            "country": "United States of America",
            "province": "Nevada",
            "city": "Las Vegas",
            "postalCode": "89109",
            "name": "Billing Address"
          }
        ],
        "matchers": {}
      }""";

  public static final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJjdXN0b206aWRwX3VzZXJfaWQiOiJ1YXR0ZXN0IiwiY3VzdG9tOmlkcF9uYW1lIjoiaWRpciIsImN1c3RvbTppZHBfZGlzcGxheV9uYW1lIjoiVGVzdCwgVUFUIFdMUlM6RVgiLCJnaXZlbl9uYW1lIjoiVWF0IiwiZmFtaWx5X25hbWUiOiJUZXN0IiwiZW1haWwiOiJ1YXR0ZXN0QHRlc3QuY29tIiwiaWRwX2J1c2luZXNzX25hbWUiOiJBdXRvbWF0ZWQgVGVzdCJ9.lzTcimHRjALlD2sNDH8nPqMnAHvt2j_vt-l1IuLJYcE";

  public static final String COGNITO_REFRESH = """
      {
          "AuthenticationResult": {
              "AccessToken": "eyJhbGciOiJIUzI1NiJ9.eyJjdXN0b206aWRwX3VzZXJfaWQiOiJ1YXR0ZXN0IiwiY3VzdG9tOmlkcF9uYW1lIjoiaWRpciIsImN1c3RvbTppZHBfZGlzcGxheV9uYW1lIjoiVGVzdCwgVUFUIFdMUlM6RVgiLCJnaXZlbl9uYW1lIjoiVWF0IiwiZmFtaWx5X25hbWUiOiJUZXN0IiwiZW1haWwiOiJ1YXR0ZXN0QHRlc3QuY29tIiwiaWRwX2J1c2luZXNzX25hbWUiOiJBdXRvbWF0ZWQgVGVzdCJ9.lzTcimHRjALlD2sNDH8nPqMnAHvt2j_vt-l1IuLJYcE",
              "ExpiresIn": 300,
              "IdToken": "eyJhbGciOiJIUzI1NiJ9.eyJjdXN0b206aWRwX3VzZXJfaWQiOiJ1YXR0ZXN0IiwiY3VzdG9tOmlkcF9uYW1lIjoiaWRpciIsImN1c3RvbTppZHBfZGlzcGxheV9uYW1lIjoiVGVzdCwgVUFUIFdMUlM6RVgiLCJnaXZlbl9uYW1lIjoiVWF0IiwiZmFtaWx5X25hbWUiOiJUZXN0IiwiZW1haWwiOiJ1YXR0ZXN0QHRlc3QuY29tIiwiaWRwX2J1c2luZXNzX25hbWUiOiJBdXRvbWF0ZWQgVGVzdCJ9.lzTcimHRjALlD2sNDH8nPqMnAHvt2j_vt-l1IuLJYcE",
              "TokenType": "Bearer"
          },
          "ChallengeParameters": {}
      }""";


  public static Map<String, Object> getClaims(String idpName) {

    Map<String, Object> idir = Map.of(
        "custom:idp_user_id", UUID.randomUUID().toString(),
        "custom:idp_username", "jdoe",
        "custom:idp_name", "idir",
        "custom:idp_display_name", "Doe, Jhon UAT:EX",
        "email", "jdoe@mail.ca"
    );

    Map<String, Object> bceid = Map.of(
        "custom:idp_user_id", UUID.randomUUID().toString(),
        "custom:idp_username", "jdoe",
        "custom:idp_name", "bceidbusiness",
        "custom:idp_display_name", "Jhon Doe",
        "email", "jdoe@mail.ca",
        "custom:idp_business_id", UUID.randomUUID().toString(),
        "custom:idp_business_name", "Example Inc.",
        "given_name", "Jhon",
        "family_name", "Doe"
    );

    Map<String, Object> bcsc = Map.of(
        "custom:idp_user_id", UUID.randomUUID().toString(),
        "custom:idp_username", "jdoe",
        "custom:idp_name", "idir",
        "custom:idp_display_name", "Jhon Doe",
        "email", "jdoe@mail.ca",
        "address", Map.of("formatted",
            "{\\\"street_address\\\":\\\"4000 SEYMOUR PLACE\\\",\\\"country\\\":\\\"CA\\\",\\\"locality\\\":\\\"VICTORIA\\\",\\\"region\\\":\\\"BC\\\",\\\"postal_code\\\":\\\"V8Z 1C8\\\"}"),
        "birthdate", "1986-11-12",
        "given_name", "Jhon",
        "family_name", "Doe"
    );

    return switch (idpName) {
      case "bceidbusiness" -> bceid;
      case "bcsc" -> bcsc;
      default -> idir;
    };

  }

  public static final String OPENMAPS_SAC_DATA = """
      {
        "type": "FeatureCollection",
        "crs": {
            "type": "name",
            "properties": {
                "name": "EPSG:4326"
            }
        },
        "features": [
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [
                        -87.35969666,
                        52.98282376
                    ]
                },
                "properties": {
                    "GmlID": "Première_Nation___First_Nation.1351416",
                    "OBJECTID": 1351416,
                    "Numéro_de_bande___Band_Number": 240,
                    "Nom_de_bande___Band_Name": "Webequie"
                }
            }
        ]
      }""";

  public static final String OPENMAPS_SAC_NODATA = """
      {
         "type": "FeatureCollection",
         "crs": {
             "type": "name",
             "properties": {
                 "name": "EPSG:4326"
             }
         },
         "features": []
       }""";

  public static final String OPENMAPS_SACT_DATA = """
      {
          "type": "FeatureCollection",
          "crs": {
              "type": "name",
              "properties": {
                  "name": "EPSG:4326"
              }
          },
          "features": [
              {
                  "type": "Feature",
                  "geometry": {
                      "type": "Point",
                      "coordinates": [
                          -72.785381,
                          47.44122729
                      ]
                  },
                  "properties": {
                      "GmlID": "Conseil_tribal___Tribal_Council.272501",
                      "OBJECTID": 272501,
                      "Numéro_du_conseil_tribal___Tribal_Council_Number": 1064,
                      "Nom_du_conseil_tribal___Tribal_Council_Name": "ATIKAMEKW SIPI - CONSEIL DE LA NATION ATIKAMEKW",
                      "CPC_CODE": "QC"
                  }
              }
          ]
      }""";

  public static final String OPENMAPS_BCMAPS_DATA = """
       {
          "type": "FeatureCollection",
          "features": [
              {
                  "type": "Feature",
                  "id": "WHSE_HUMAN_CULTURAL_ECONOMIC.FN_COMMUNITY_LOCATIONS_SP.29",
                  "geometry": {
                      "type": "Point",
                      "coordinates": [
                          1208415.942,
                          482549.267
                      ]
                  },
                  "geometry_name": "SHAPE",
                  "properties": {
                      "COMMUNITY_LOCATION_ID": 29,
                      "FIRST_NATION_BC_NAME": "Squamish Nation",
                      "FIRST_NATION_FEDERAL_NAME": "Squamish",
                      "FIRST_NATION_FEDERAL_ID": 555,
                      "URL_TO_BC_WEBSITE": "http://www2.gov.bc.ca/gov/content/environment/natural-resource-stewardship/consulting-with-first-nations/first-nations-negotiations/first-nations-a-z-listing/squamish-nation",
                      "URL_TO_FEDERAL_WEBSITE": "http://fnp-ppn.aadnc-aandc.gc.ca/fnp/Main/Search/FNMain.aspx?BAND_NUMBER=555&lang=eng",
                      "URL_TO_FIRST_NATION_WEBSITE": "http://www.squamish.net/",
                      "MEMBER_ORGANIZATION_NAMES": "Independent",
                      "LANGUAGE_GROUP": "Sḵwx̱wú7mesh sníchim",
                      "BC_REGIONAL_OFFICE": "South Coast (Surrey)",
                      "MAPSHEET_NUMBER": "92G",
                      "PREFERRED_NAME": "Squamish",
                      "ALTERNATIVE_NAME_1": "variation SKWAMISH, SKWAWAMISH; alternate CH'CH'ELXWIKW; SEYMOUR; includes FALSE CREEK (pre-1914); includes CAPILANO (variation KAPILANO - 1923; CAPITANO CREEK - circa 1917; alternate HOMULCHSEAN); includes KITSILANO (1923) (alternate SENAKW)),",
                      "ALTERNATIVE_NAME_2": "K'IK'ELXEN, SEAICHEM (1923), CHEAKAMUS, CHEKWELP, WAIWAKUM, MISSION (traditional name USTLAWN), KOWTAIN, POQUIOSIN, POYAM, SHELTER ISLAND (alternate SXAALTXW), STAWAMUS, SKOWISHIN, YOOKWITZ; includes HOWE SOUND",
                      "ADDRESS_LINE1": "P.O. Box 86131",
                      "ADDRESS_LINE2": "320 Seymour Boulevard",
                      "OFFICE_CITY": "NORTH VANCOUVER",
                      "OFFICE_PROVINCE": "BC",
                      "OFFICE_POSTAL_CODE": "V7L 4J5",
                      "LOCATION_DESCRIPTION": "NEW WESTMINSTER DISTRICT ON NORTH SHORE OF BURRARD INLET AT FIRST NARROWS, N. END OF LIONS GATE BRIDGE",
                      "SITE_NAME": "Capilano 5",
                      "SITE_NUMBER": "07969",
                      "COMMENTS": "Moved to reflect largest community",
                      "OBJECTID": 543,
                      "SE_ANNO_CAD_DATA": null
                  }
              }
          ],
          "totalFeatures": 1,
          "numberMatched": 1,
          "numberReturned": 1,
          "timeStamp": "2024-05-16T21:45:54.500Z",
          "crs": {
              "type": "name",
              "properties": {
                  "name": "urn:ogc:def:crs:EPSG::3005"
              }
          }
      }""";

  public static final String OPENMAPS_BCMAPS_NODATA = """
      {
        "type": "FeatureCollection",
        "features": [],
        "totalFeatures": 0,
        "numberMatched": 0,
        "numberReturned": 0,
        "timeStamp": "2024-07-05T16:10:50.847Z",
        "crs": null
      }""";

  public static final String STAFF_SUBMITTED_INDIVIDUAL_JSON = """
      {
          "businessInformation": {
              "district": "",
              "businessType": "U",
              "legalType": "SP",
              "clientType": "I",
              "registrationNumber": "",
              "businessName": "John Wick",
              "goodStandingInd": "Y",
              "birthdate": "1974-08-12",
              "firstName": "John",
              "lastName": "Wick",
              "identificationType": {
                  "value": "CDDL",
                  "text": "Canadian driver's licence",
                  "countryCode": "CA"
              },
              "identificationCountry": "CA",
              "clientIdentification": "54621654",
              "identificationProvince": "BC"
          },
          "location": {
              "addresses": [
                  {
                      "locationName": "Hangar",
                      "complementaryAddressOne": "",
                      "complementaryAddressTwo": null,
                      "streetAddress": "1234 Nowhere St",
                      "country": {
                          "value": "CA",
                          "text": "Canada"
                      },
                      "province": {
                          "value": "BC",
                          "text": "British Columbia"
                      },
                      "city": "Victoria",
                      "postalCode": "V8V8V8",
                      "businessPhoneNumber": "",
                      "secondaryPhoneNumber": "",
                      "faxNumber": "",
                      "emailAddress": "",
                      "notes": "",
                      "index": 0
                  }
              ],
              "contacts": [
                  {
                      "locationNames": [
                          {
                              "value": "0",
                              "text": "Hangar"
                          }
                      ],
                      "contactType": {
                          "value": "BL",
                          "text": "Billing"
                      },
                      "firstName": "John",
                      "lastName": "Wick",
                      "phoneNumber": "(250) 445-4540",
                      "secondaryPhoneNumber": "",
                      "faxNumber": "",
                      "email": "thatmail@maila.ca",
                      "index": 0
                  }
              ]
          }
      }""";

  public static final String STAFF_SUBMITTED_SPORG_JSON = """
      {
          "businessInformation": {
              "district": "",
              "businessType": "R",
              "legalType": "SP",
              "clientType": "RSP",
              "registrationNumber": "FM00004455",
              "businessName": "JAMES BAXTER WOOD HANDCRAFTED FURNITURE",
              "goodStandingInd": "Y",
              "birthdate": "1974-08-12",
              "firstName": "John",
              "lastName": "Wick",
              "identificationType": {
                  "value": "CDDL",
                  "text": "Canadian driver's licence",
                  "countryCode": "CA"
              },
              "identificationCountry": "CA",
              "clientIdentification": "54621654",
              "identificationProvince": "BC"
          },
          "location": {
              "addresses": [
                  {
                      "locationName": "Hangar",
                      "complementaryAddressOne": "",
                      "complementaryAddressTwo": null,
                      "streetAddress": "1234 Nowhere St",
                      "country": {
                          "value": "CA",
                          "text": "Canada"
                      },
                      "province": {
                          "value": "BC",
                          "text": "British Columbia"
                      },
                      "city": "Victoria",
                      "postalCode": "V8V8V8",
                      "businessPhoneNumber": "",
                      "secondaryPhoneNumber": "",
                      "faxNumber": "",
                      "emailAddress": "",
                      "notes": "",
                      "index": 0
                  }
              ],
              "contacts": [
                  {
                      "locationNames": [
                          {
                              "value": "0",
                              "text": "Hangar"
                          }
                      ],
                      "contactType": {
                          "value": "BL",
                          "text": "Billing"
                      },
                      "firstName": "John",
                      "lastName": "Wick",
                      "phoneNumber": "(250) 445-4540",
                      "secondaryPhoneNumber": "",
                      "faxNumber": "",
                      "email": "thatmail@maila.ca",
                      "index": 0
                  }
              ]
          }
      }""";


}
