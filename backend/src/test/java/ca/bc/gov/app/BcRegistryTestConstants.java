package ca.bc.gov.app;

import ca.bc.gov.app.dto.bcregistry.BcRegistryAddressDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryAlternateNameDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessAdressesDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryOfficerDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryOfficesDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryPartyDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryRoleDto;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class BcRegistryTestConstants {

  public static final String NO_DATA = """
      {}""";

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

  public static final String BCREG_DOC_REQ_FAIL = """
      {
          "errorMessage": "API backend third party service error.",
          "rootCause": "details:[],message:Business not found. "
      }""";


  public static final String BCREG_FACET_FAIL = """
      {
           "errorMessage": "API backend third party service error.",
           "rootCause": "details:[Invalid payload:Expected an object for 'query'.],message:Errors processing request. "
      }""";

  public static final String BCREG_FACET_500 = """
      {
           "errorMessage": "API unexpected error. If error persists, contact API support.",
           "rootCause": "fault:faultstring:Execution of ServiceCallout SC-Request-JWT failed. Reason: timeout occurred in SC-Request-JWT,detail:errorcode:steps.servicecallout.ExecutionFailed"
       }""";

  public static final String BCREG_FACET_401 = """
      {
          "errorMessage": "API security error: API key check failed.",
          "rootCause": "fault:faultstring:Invalid ApiKey,detail:errorcode:oauth.v2.InvalidApiKey"
      }""";

  public static final String BCREG_FACET_EMPTY = """
      {
        "searchResults": {
           "results": [],
           "totalResults": 0
       }
      }""";

  public static final String BCREG_RESPONSE_ANY = """
      {
        "name": "SAMPLE COMPANY",
        "id": "AA0000001",
        "goodStanding": true,
        "addresses": [],
        "contacts": []
      }""";

  public static final String BCREG_FACET_ANY = """
      {
           "searchResults": {
               "results": [
                   {
                       "bn": "100000000000001",
                       "goodStanding": true,
                       "identifier": "C0123456",
                       "legalType": "C",
                       "name": "EXAMPLE COMPANY LTD.",
                       "score": 57.321865,
                       "status": "ACTIVE"
                   }
               ],
               "totalResults": 1
           }
       }""";

  public static final BcRegistryDocumentDto BCREG_DOCOBJ_ANY = new BcRegistryDocumentDto(
      new BcRegistryBusinessDto(
          List.of(
              new BcRegistryAlternateNameDto(
                  "C",
                  "C0123456",
                  "EXAMPLE COMPANY LTD.",
                  null,
                  null
              )
          ),
          true,
          false,
          false,
          false,
          "C0123456",
          "EXAMPLE COMPANY LTD.",
          "C",
          "ACTIVE"
      ),
      new BcRegistryOfficesDto(
          new BcRegistryBusinessAdressesDto(
              new BcRegistryAddressDto(null, null, null, null, null, null, null, null),
              new BcRegistryAddressDto(null, null, null, null, null, null, null, null)
          )
      ),
      List.of()
  );

  public static final String BCREG_FACET_SP_ORG = """
      {
        "searchResults": {
          "results": [
            {
              "bn": "100000000000002",
              "identifier": "FM0123456",
              "legalType": "SP",
              "name": "EXAMPLE SOLE PROPRIETORSHIP",
              "parties": [
                {
                  "partyName": "EXAMPLE COMPANY LTD.",
                  "partyRoles": [
                    "proprietor"
                  ],
                  "partyType": "organization",
                  "score": 0.0
                }
              ],
              "score": 56.05646,
              "status": "ACTIVE"
            }
          ],
          "totalResults": 1
        }
      }""";

  public static final String BCREG_DOC_SP_ORG = """
      {
        "business": {
          "alternateNames": [
            {
              "entityType": "SP",
              "identifier": "FM0123456",
              "name": "EXAMPLE SOLE PROPRIETORSHIP",
              "registeredDate": "1985-07-23T07:00:00+00:00",
              "startDate": null
            }
          ],
          "goodStanding": true,
          "hasCorrections": false,
          "hasCourtOrders": false,
          "hasRestrictions": false,
          "identifier": "FM0123456",
          "legalName": "EXAMPLE COMPANY LTD.",
          "legalType": "SP",
          "state": "ACTIVE"
        },
        "offices": {
          "message": "FM0123456 address not found"
        },
        "parties": [
          {
            "officer": {
              "email": "",
              "identifier": "BC0123456",
              "organizationName": "EXAMPLE COMPANY LTD.",
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
        ]
      }""";

  public static final BcRegistryDocumentDto BCREG_DOCOBJ_SP_ORG = new BcRegistryDocumentDto(
      new BcRegistryBusinessDto(
          List.of(
              new BcRegistryAlternateNameDto(
                  "SP",
                  "FM0123456",
                  "EXAMPLE SOLE PROPRIETORSHIP",
                  ZonedDateTime.of(1985, 7, 23, 7, 0, 0, 0, ZoneOffset.UTC),
                  null
              )
          ),
          true,
          false,
          false,
          false,
          "FM0123456",
          "EXAMPLE COMPANY LTD.",
          "SP",
          "ACTIVE"
      ),
      new BcRegistryOfficesDto(null),
      List.of(
          new BcRegistryPartyDto(
              null, null,
              new BcRegistryOfficerDto(
                  StringUtils.EMPTY,
                  null,
                  null,
                  null,
                  "BC0123456",
                  "EXAMPLE COMPANY LTD.",
                  "organization"
              ),
              List.of(
                  new BcRegistryRoleDto(
                      LocalDate.of(1985, 7, 23),
                      null,
                      "Proprietor"
                  )
              )
          )
      )
  );

  public static final String BCREG_FACET_SP_PERSON = """
      {
         "searchResults": {
           "results": [
             {
               "bn": "100000000000003",
               "identifier": "FM0123210",
               "legalType": "SP",
               "name": "EXAMPLE SOLE PROPRIETORSHIP",
               "parties": [
                 {
                   "partyName": "JOHNATHAN WICK",
                   "partyRoles": [
                     "proprietor"
                   ],
                   "partyType": "person",
                   "score": 0.0
                 }
               ],
               "score": 56.05646,
               "status": "ACTIVE"
             }
           ],
           "totalResults": 1
         }
       }""";

  public static final String BCREG_DOC_SP_PERSON = """
      {
         "business": {
           "alternateNames": [
             {
                 "entityType": "SP",
                 "identifier": "FM0123210",
                 "name":  "EXAMPLE SOLE PROPRIETORSHIP",
                 "registeredDate": "2009-11-30T08:00:00+00:00",
                 "startDate": "2009-10-01"
             }
           ],
           "goodStanding": true,
           "hasCorrections": false,
           "hasCourtOrders": false,
           "hasRestrictions": false,
           "identifier": "FM0123210",
           "legalName": "JOHNATHAN WICK",
           "legalType": "SP",
           "state": "ACTIVE"
         },
         "offices": {
           "businessOffice": {
             "deliveryAddress": {
                 "addressCity": "Victoria",
                 "addressCountry": "Canada",
                 "addressRegion": "BC",
                 "addressType": "delivery",
                 "deliveryInstructions": "",
                 "postalCode": "V8T 5J9",
                 "streetAddress": "2975 Jutland Rd.",
                 "streetAddressAdditional": ""
             },
             "mailingAddress": {
                 "addressCity": "Victoria",
                 "addressCountry": "Canada",
                 "addressRegion": "BC",
                 "addressType": "mailing",
                 "deliveryInstructions": "",
                 "postalCode": "V8T 5J9",
                 "streetAddress": "2975 Jutland Rd.",
                 "streetAddressAdditional": ""
             }
           }
         },
         "parties": [
           {
             "deliveryAddress": {
                 "addressCity": "Victoria",
                 "addressCountry": "Canada",
                 "addressRegion": "BC",
                 "deliveryInstructions": "",
                 "postalCode": "V8T 5J9",
                 "streetAddress": "2975 Jutland Rd.",
                 "streetAddressAdditional": ""
             },
             "mailingAddress": {
                 "addressCity": "Victoria",
                 "addressCountry": "Canada",
                 "addressRegion": "BC",
                 "deliveryInstructions": "",
                 "postalCode": "V8T 5J9",
                 "streetAddress": "2975 Jutland Rd.",
                 "streetAddressAdditional": ""
             },
             "officer": {
                 "email": "",
                 "firstName": "JOHNATHAN",
                 "lastName": "WICK",
                 "partyType": "person"
             },
             "roles": [
               {
                 "appointmentDate": "2009-11-30",
                 "cessationDate": null,
                 "roleType": "Proprietor"
               }
             ]
           }
         ]
       }""";

  public static final BcRegistryDocumentDto BCREG_DOCOBJ_SP_PERSON = new BcRegistryDocumentDto(
      new BcRegistryBusinessDto(
          List.of(
              new BcRegistryAlternateNameDto(
                  "SP",
                  "FM0123210",
                  "EXAMPLE SOLE PROPRIETORSHIP",
                  ZonedDateTime.of(2009, 11, 30, 8, 0, 0, 0, ZoneOffset.UTC),
                  LocalDate.of(2009, 10, 1)
              )
          ),
          true,
          false,
          false,
          false,
          "FM0123210",
          "JOHNATHAN WICK",
          "SP",
          "ACTIVE"
      ),
      new BcRegistryOfficesDto(
          new BcRegistryBusinessAdressesDto(
              new BcRegistryAddressDto(
                  "Victoria",
                  "Canada",
                  "BC",
                  StringUtils.EMPTY,
                  "V8T 5J9",
                  "2975 Jutland Rd.",
                  StringUtils.EMPTY,
                  "mailing"
              ),
              new BcRegistryAddressDto(
                  "Victoria",
                  "Canada",
                  "BC",
                  StringUtils.EMPTY,
                  "V8T 5J9",
                  "2975 Jutland Rd.",
                  StringUtils.EMPTY,
                  "delivery"
              )
          )
      ),
      List.of(
          new BcRegistryPartyDto(
              new BcRegistryAddressDto(
                  "Victoria",
                  "Canada",
                  "BC",
                  StringUtils.EMPTY,
                  "V8T 5J9",
                  "2975 Jutland Rd.",
                  StringUtils.EMPTY,
                  null
              ),
              new BcRegistryAddressDto(
                  "Victoria",
                  "Canada",
                  "BC",
                  StringUtils.EMPTY,
                  "V8T 5J9",
                  "2975 Jutland Rd.",
                  StringUtils.EMPTY,
                  null
              ),
              new BcRegistryOfficerDto(
                  StringUtils.EMPTY,
                  "JOHNATHAN",
                  "WICK",
                  null,
                  null,
                  null,
                  "person"
              ),
              List.of(
                  new BcRegistryRoleDto(
                      LocalDate.of(2009, 11, 30),
                      null, "Proprietor"
                  )
              )
          )
      )
  );

  public static final String BCREG_FACET_GP = """
      {
        "searchResults": {
          "results": [
            {
              "bn": "",
              "identifier": "FM0123432",
              "legalType": "GP",
              "name": "GENERAL PARTNERSHIP",
              "parties": [
                {
                  "partyName": "JOHNATHAN VALELONG WICK",
                  "partyRoles": [
                    "partner"
                  ],
                  "partyType": "person",
                  "score": 0.0
                },
                {
                  "partyName": "RUSKA ROMA",
                  "partyRoles": [
                    "partner"
                  ],
                  "partyType": "person",
                  "score": 0.0
                }
              ],
              "score": 58.579773,
              "status": "ACTIVE"
            }
          ],
          "totalResults": 1
        }
      }""";

  public static final String BCREG_DOC_GP = """
      {
           "business": {
               "alternateNames": [
                   {
                       "entityType": "GP",
                       "identifier": "FM0123432",
                       "name": "GENERAL PARTNERSHIP",
                       "registeredDate": "1994-04-25T07:00:00+00:00",
                       "startDate": null
                   }
               ],
               "goodStanding": true,
               "hasCorrections": false,
               "hasCourtOrders": false,
               "hasRestrictions": false,
               "identifier": "FM0123432",
               "legalName": "JOHNATHAN VALELONG WICK, MARCEL ST. AMANT",
               "legalType": "GP",
               "state": "ACTIVE"
           },
           "offices": {
               "message": "FM0123432 address not found"
           },
           "parties": [
               {
                   "officer": {
                       "email": "",
                       "firstName": "JOHNATHAN",
                       "lastName": "WICK",
                       "middleInitial": "VALELONG",
                       "partyType": "person"
                   },
                   "roles": [
                       {
                           "appointmentDate": "1994-04-25",
                           "cessationDate": null,
                           "roleType": "Partner"
                       }
                   ]
               },
               {
                   "officer": {
                       "email": "",
                       "firstName": "RUSKA",
                       "lastName": "ROMA",
                       "partyType": "person"
                   },
                   "roles": [
                       {
                           "appointmentDate": "1994-04-25",
                           "cessationDate": null,
                           "roleType": "Partner"
                       }
                   ]
               }
           ]
       }""";

  public static final BcRegistryDocumentDto BCREG_DOCOBJ_GP = new BcRegistryDocumentDto(
      new BcRegistryBusinessDto(
          List.of(
              new BcRegistryAlternateNameDto(
                  "GP",
                  "FM0123432",
                  "GENERAL PARTNERSHIP",
                  ZonedDateTime.of(1994, 4, 25, 7, 0, 0, 0, ZoneOffset.UTC),
                  null
              )
          ),
          true,
          false,
          false,
          false,
          "FM0123432",
          "JOHNATHAN VALELONG WICK, MARCEL ST. AMANT",
          "GP",
          "ACTIVE"
      ),
      new BcRegistryOfficesDto(null),
      List.of(
          new BcRegistryPartyDto(
              null, null,
              new BcRegistryOfficerDto(
                  StringUtils.EMPTY,
                  "JOHNATHAN",
                  "WICK",
                  "VALELONG",
                  null,
                  null,
                  "person"
              ),
              List.of(
                  new BcRegistryRoleDto(
                      LocalDate.of(1994, 4, 25),
                      null, "Partner"
                  )
              )
          ),
          new BcRegistryPartyDto(
              null, null,
              new BcRegistryOfficerDto(
                  StringUtils.EMPTY,
                  "RUSKA",
                  "ROMA",
                  null,
                  null,
                  null,
                  "person"
              ),
              List.of(
                  new BcRegistryRoleDto(
                      LocalDate.of(1994, 4, 25),
                      null, "Partner"
                  )
              )
          )
      )
  );

  public static final String BCREG_FACET_XP = """
      {
        "searchResults": {
          "results": [
            {
                "identifier": "XP0123456",
                "legalType": "XP",
                "name": "EXAMPLE FUND",
                "parties": [
                    {
                        "partyName": "EXAMPLE INVESTMENTS INC.",
                        "partyRoles": [
                            "partner"
                        ],
                        "partyType": "organization",
                        "score": 0.0
                    }
                ],
                "score": 50.60915,
                "status": "ACTIVE"
            }
          ],
          "totalResults": 1
        }
      }""";

  public static final BcRegistryDocumentDto BCREG_DOCOBJ_XP = new BcRegistryDocumentDto(
      new BcRegistryBusinessDto(
          List.of(
              new BcRegistryAlternateNameDto(
                  "XP",
                  "XP0123456",
                  "EXAMPLE FUND",
                  null,
                  null
              )
          ),
          null,
          false,
          false,
          false,
          "XP0123456",
          "EXAMPLE FUND",
          "XP",
          "ACTIVE"
      ),
      new BcRegistryOfficesDto(
          new BcRegistryBusinessAdressesDto(
              new BcRegistryAddressDto(null, null, null, null, null, null, null, null),
              new BcRegistryAddressDto(null, null, null, null, null, null, null, null)
          )
      ),
      List.of()
  );


}
