package ca.bc.gov.app;

import java.util.List;
import java.util.Map;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.EmailRequestDto;
import ca.bc.gov.app.dto.cognito.AuthResponse;

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
            "locationName": "Mailing Address",
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
            "contactType": {
                "value": "DI",
                "text": "Director"
            },
            "firstName": "JAMES",
            "lastName": "BAXTER",
            "phoneNumber": "",
            "email": "",
            "index": 1,
            "locationNames": [{
            "text": "Mailing Address",
            "value": "0"
            }]
          }
        ]
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
                      "middleInitial": "JAMES",
                      "partyType": "Director"
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
  public static final String SUBMISSION_LIST_CONTENT = """
      [
        {
          "id":1,
          "requestType":"Submission pending processing",
          "name":"Goldfinger",
          "clientType":"P",
          "updated":"testUserId | %s",
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
              "P",
              "Y",
              "GP"
          ),
          new ClientLocationDto(
              List.of(
                  new ClientAddressDto(
                      "3570 S Las Vegas Blvd",
                      new ClientValueTextDto("US", ""),
                      new ClientValueTextDto("NV", ""),
                      "Las Vegas", "89109",
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
          )
      );

  public static final ClientSubmissionDto UNREGISTERED_BUSINESS_SUBMISSION_DTO =
      new ClientSubmissionDto(
          new ClientBusinessInformationDto(
              "",
              "James",
              "U",
              "I",
              "",
              "SP"
          ),
          new ClientLocationDto(
              List.of(
                  new ClientAddressDto(
                      "3570 S Las Vegas Blvd",
                      new ClientValueTextDto("US", ""),
                      new ClientValueTextDto("NV", ""),
                      "Las Vegas", "89109",
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
          )
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
              "key3", true
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

  public static final AuthResponse AUTH_RESPONSE = new AuthResponse(
      "a.b.c",
      300,

      "Bearer",
      "d.e.f",
      "g.h.i"
  );
}
