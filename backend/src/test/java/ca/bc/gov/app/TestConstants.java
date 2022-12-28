package ca.bc.gov.app;

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

  public static final String ORGBOOK_NAMELOOKUP_OK = """
      {
        "total": 10,        
        "results": [
          {
            "type": "name",
            "sub_type": "entity_name",
            "value": "U3 POWER CORP",
            "topic_source_id": "BC0772006",
            "topic_type": "registration.registries.ca",
            "credential_type": "registration.registries.ca",
            "credential_id": "d759d210-628a-4a02-9da3-506cb253ee6c",
            "score": 59.46851
          },
          {
            "type": "name",
            "sub_type": "entity_name",
            "value": "STOTHERT POWER CORP",
            "topic_source_id": "BC0070832",
            "topic_type": "registration.registries.ca",
            "credential_type": "registration.registries.ca",
            "credential_id": "70bfb05d-a4a7-4dc9-8e82-037f9eeb90ef",
            "score": 59.150566
          },
          {
            "type": "name",
            "sub_type": "entity_name",
            "value": "UNIVERSAL POWER CORP",
            "topic_source_id": "BC0334496",
            "topic_type": "registration.registries.ca",
            "credential_type": "registration.registries.ca",
            "credential_id": "ab583755-b031-46a2-ab38-1f4b0644d817",
            "score": 59.150566
          }
        ]
      }
      """;

  public static final String ORGBOOK_NAMELOOKUP_OK_RESPONSE = """
      {
        "total": 10,        
        "results": [
          {
            "value": "U3 POWER CORP",
            "topic_source_id": "BC0772006"
          },
          {
            "value": "STOTHERT POWER CORP",
            "topic_source_id": "BC0070832"
          },
          {
            "value": "UNIVERSAL POWER CORP",
            "topic_source_id": "BC0334496"
          }
        ]
      }
      """;

  public static final String ORGBOOK_NAMELOOKUP_EMPTY = """
      {
        "total": 0,        
        "results": []
      }
      """;

  public static final String ORGBOOK_INCORP_EMPTY = """
      {
        "total": 0,
        "page_size": 10,
        "page": 1,
        "results": []
      }
      """;

  public static final String ORGBOOK_INCORP_OK = """
      {
        "total": 1,
        "page_size": 10,
        "page": 1,        
        "results": [
          {
            "id": 789282,
            "source_id": "BC0772006",
            "type": "registration.registries.ca",
            "names": [
              {
                "id": 1187440,
                "text": "U3 POWER CORP.",
                "language": null,
                "type": "entity_name",
                "credential_id": 1218748
              },
              {
                "id": 3355176,
                "text": "837723121",
                "language": null,
                "type": "business_number",
                "credential_id": 3471039
              }
            ],
            "addresses": [],
            "attributes": [
              {
                "id": 8336043,
                "type": "registration_date",
                "format": "datetime",
                "value": "2006-10-17T23:58:42+00:00",
                "credential_id": 1218748,
                "credential_type_id": 1
              },
              {
                "id": 8336044,
                "type": "entity_name_effective",
                "format": "datetime",
                "value": "2006-10-17T23:58:42+00:00",
                "credential_id": 1218748,
                "credential_type_id": 1
              },
              {
                "id": 8336045,
                "type": "entity_status",
                "format": "category",
                "value": "ACT",
                "credential_id": 1218748,
                "credential_type_id": 1
              },
              {
                "id": 8336046,
                "type": "entity_status_effective",
                "format": "datetime",
                "value": "2006-10-17T23:58:42+00:00",
                "credential_id": 1218748,
                "credential_type_id": 1
              },
              {
                "id": 8336047,
                "type": "entity_type",
                "format": "category",
                "value": "BC",
                "credential_id": 1218748,
                "credential_type_id": 1
              },
              {
                "id": 8336048,
                "type": "home_jurisdiction",
                "format": "jurisdiction",
                "value": "BC",
                "credential_id": 1218748,
                "credential_type_id": 1
              },
              {
                "id": 8336049,
                "type": "reason_description",
                "format": "category",
                "value": "Filing:ICORP",
                "credential_id": 1218748,
                "credential_type_id": 1
              }
            ],
            "credential_set": {
              "id": 789295,
              "create_timestamp": "2020-02-15T12:05:45.979364-08:00",
              "update_timestamp": "2020-02-15T12:05:45.979402-08:00",
              "latest_credential_id": 1218748,
              "topic_id": 789282,
              "first_effective_date": "2006-10-17T16:58:42-07:00",
              "last_effective_date": null
            },
            "credential_type": {
              "id": 1,
              "issuer": {
                "id": 1,
                "has_logo": true,
                "create_timestamp": "2020-02-14T14:27:35.624957-08:00",
                "update_timestamp": "2022-12-21T07:56:19.419445-08:00",
                "did": "HR6vs6GEZ8rHaVgjg2WodM",
                "name": "BC Corporate Registry",
                "abbreviation": "BCReg",
                "email": "bcregistries@gov.bc.ca",
                "url": "https://www2.gov.bc.ca/gov/content/governments/organizational-structure/ministries-organizations/ministries/citizens-services/bc-registries-online-services",
                "endpoint": ""
              },
              "has_logo": true,
              "create_timestamp": "2020-02-14T14:27:35.689210-08:00",
              "update_timestamp": "2022-12-28T10:15:17.328252-08:00",
              "description": "registration.registries.ca",
              "credential_def_id": "HR6vs6GEZ8rHaVgjg2WodM:3:CL:41051:tag",
              "last_issue_date": "2022-12-28T10:15:17.328141-08:00",
              "url": "/bcreg/incorporation",
              "credential_title": null,
              "highlighted_attributes": [
                "reason_description"
              ],
              "schema_label": {
                "en": {
                  "label": "Registration",
                  "description": "Registration Credential"
                }
              },
              "schema": {
                "id": 1,
                "create_timestamp": "2020-02-14T14:27:35.665876-08:00",
                "update_timestamp": "2022-12-21T07:56:19.426139-08:00",
                "name": "registration.registries.ca",
                "version": "1.0.42",
                "origin_did": "HR6vs6GEZ8rHaVgjg2WodM"
              }
            },
            "inactive": false,
            "revoked": false,
            "effective_date": "2006-10-17T16:58:42-07:00",
            "revoked_date": null
          }
        ]
      }            
      """;

  public static final String ORGBOOK_INCORP_OK_RESPONSE = """
      {
        "total": 1,
        "page_size": 10,
        "page": 1,
        "results": [
          {
            "id": 789282,
            "source_id": "BC0772006",
            "type": "registration.registries.ca",
            "names": [
              {
                "id": 1187440,
                "text": "U3 POWER CORP.",                
                "type": "entity_name",
                "credential_id": 1218748
              },
              {
                "id": 3355176,
                "text": "837723121",                
                "type": "business_number",
                "credential_id": 3471039
              }
            ],
            "addresses": [],
            "attributes": [
              {
                "id": 8336043,
                "type": "registration_date",
                "format": "datetime",
                "value": "2006-10-17T23:58:42+00:00",
                "credential_id": 1218748,
                "credential_type_id": 1
              },
              {
                "id": 8336044,
                "type": "entity_name_effective",
                "format": "datetime",
                "value": "2006-10-17T23:58:42+00:00",
                "credential_id": 1218748,
                "credential_type_id": 1
              },
              {
                "id": 8336045,
                "type": "entity_status",
                "format": "category",
                "value": "ACT",
                "credential_id": 1218748,
                "credential_type_id": 1
              },
              {
                "id": 8336046,
                "type": "entity_status_effective",
                "format": "datetime",
                "value": "2006-10-17T23:58:42+00:00",
                "credential_id": 1218748,
                "credential_type_id": 1
              },
              {
                "id": 8336047,
                "type": "entity_type",
                "format": "category",
                "value": "BC",
                "credential_id": 1218748,
                "credential_type_id": 1
              },
              {
                "id": 8336048,
                "type": "home_jurisdiction",
                "format": "jurisdiction",
                "value": "BC",
                "credential_id": 1218748,
                "credential_type_id": 1
              },
              {
                "id": 8336049,
                "type": "reason_description",
                "format": "category",
                "value": "Filing:ICORP",
                "credential_id": 1218748,
                "credential_type_id": 1
              }
            ],
            "credential_set": {
              "id": 789295,
              "create_timestamp": "2020-02-15T20:05:45.979364Z",
              "update_timestamp": "2020-02-15T20:05:45.979402Z",
              "latest_credential_id": 1218748,
              "topic_id": 789282,
              "first_effective_date": "2006-10-17T23:58:42Z",
              "last_effective_date": null
            }
          }
        ]
      }
      """;

}
