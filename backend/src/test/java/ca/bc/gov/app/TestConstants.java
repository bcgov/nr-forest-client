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

  public static final String BCREG_NAMELOOKUP_OK_RESPONSE = """
      [
        {
          "code": "BC0772006",
          "name": "U3 POWER CORP.",
          "status": "ACTIVE",
          "clientType": "SP"
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
        "clientType": "SP"
      }""";

  public static final String OPENMAPS_OK = """
      {
        "type": "FeatureCollection",
        "features": [{
          "type": "Feature",
          "id": "WHSE_HUMAN_CULTURAL_ECONOMIC.FN_COMMUNITY_LOCATIONS_SP.129",
          "geometry": {
            "type": "Point",
            "coordinates": [
              1190868.482,
              384526.713
            ]
          },
          "geometry_name": "SHAPE",
          "properties": {
            "COMMUNITY_LOCATION_ID": 129,
            "FIRST_NATION_BC_NAME": "Songhees First Nation",
            "FIRST_NATION_FEDERAL_NAME": "Songhees Nation",
            "FIRST_NATION_FEDERAL_ID": 656,
            "URL_TO_BC_WEBSITE": "http://www2.gov.bc.ca/gov/content/environment/natural-resource-stewardship/consulting-with-first-nations/first-nations-negotiations/first-nations-a-z-listing/songhees-nation",
            "URL_TO_FEDERAL_WEBSITE": "http://fnp-ppn.aadnc-aandc.gc.ca/fnp/Main/Search/FNMain.aspx?BAND_NUMBER=656&lang=eng",
            "URL_TO_FIRST_NATION_WEBSITE": "http://songheesnation.ca/",
            "MEMBER_ORGANIZATION_NAMES": "Te'mexw Treaty Association",
            "LANGUAGE_GROUP": "SENĆOŦEN / Malchosen / Lkwungen / Semiahmoo / T’Sou-ke",
            "BC_REGIONAL_OFFICE": "West Coast (Nanaimo)",
            "MAPSHEET_NUMBER": "92B",
            "PREFERRED_NAME": "Songhees Nation",
            "ALTERNATIVE_NAME_1": "formerly Songhees First Nation, SONGHEES (variation SONGHISH); includes SWENGWHUNG TRIBE, WHYOMILTH TRIBE,TEECHAMITSA TRIBE, CHE-KO-NEIN TRIBE and CHILCOWITCH TRIBE (all part of Douglas Treaty (1850))",
            "ALTERNATIVE_NAME_2": " ",
            "ADDRESS_LINE1": "1100 Admirals Road",
            "ADDRESS_LINE2": " ",
            "OFFICE_CITY": "VICTORIA",
            "OFFICE_PROVINCE": "BC",
            "OFFICE_POSTAL_CODE": "V9A 2P6",
            "LOCATION_DESCRIPTION": "ESQUIMALT DISTRICT SECTION 2A FRONTING ON ESQUIMALT HARBOUR AND EAST OF AND ADJOINING LOT 25",
            "SITE_NAME": "New Songhess No. 1A",
            "SITE_NUMBER": "06839",
            "COMMENTS": " ",
            "OBJECTID": 643,
            "SE_ANNO_CAD_DATA": null
          }
        }],
        "totalFeatures": 1,
        "numberMatched": 1,
        "numberReturned": 1,
        "timeStamp": "2023-01-03T18:55:48.913Z",
        "crs": {
          "type": "name",
          "properties": {
            "name": "urn:ogc:def:crs:EPSG::3005"
          }
        }
      }
      """;


  public static final String OPENMAPS_EMPTY = """
      {
        "type": "FeatureCollection",
        "features": [],
        "totalFeatures": 0,
        "numberMatched": 0,
        "numberReturned": 0,
        "timeStamp": "2023-01-03T18:55:48.913Z",
        "crs": {
          "type": "name",
          "properties": {
            "name": "urn:ogc:def:crs:EPSG::3005"
          }
        }
      }
      """;

  public static final String OPENMAPS_OK_RESPONSE = """
      {        
        "FIRST_NATION_FEDERAL_NAME": "Songhees Nation",
        "FIRST_NATION_FEDERAL_ID": 656,        
        "ADDRESS_LINE1": "1100 Admirals Road",
        "ADDRESS_LINE2": " ",
        "OFFICE_CITY": "VICTORIA",
        "OFFICE_PROVINCE": "BC",
        "OFFICE_POSTAL_CODE": "V9A 2P6"
      }
      """;

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
  public static final String BCREG_ADDR_OK = """
      {
          "businessOffice": {
              "deliveryAddress": {
                  "addressCity": "VICTORIA",
                  "addressCountry": "CA",
                  "addressRegion": "BC",
                  "addressType": "delivery",
                  "deliveryInstructions": null,
                  "id": 1,
                  "postalCode": "V8V1X4",
                  "streetAddress": "501 Belleville Street",
                  "streetAddressAdditional": ""
              },
              "mailingAddress": {
                  "addressCity": "VICTORIA",
                  "addressCountry": "CA",
                  "addressRegion": "BC",
                  "addressType": "mailing",
                  "deliveryInstructions": null,
                  "id": 2,
                  "postalCode": "V8V1X4",
                  "streetAddress": "501 Belleville Street",
                  "streetAddressAdditional": ""
              }
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
            "streetAddress": "501 Belleville Street",
            "country": {"value":"CA","text":"Canada"},
            "province": {"value":"BC","text":"British Columbia"},
            "city": "VICTORIA",
            "postalCode": "V8V1X4",
            "index": 0
          },
          {
            "streetAddress": "501 Belleville Street",
            "country": {"value":"CA","text":"Canada"},
            "province": {"value":"BC","text":"British Columbia"},
            "city": "VICTORIA",
            "postalCode": "V8V1X4",
            "index": 1
          }
        ]
      }""";

  public static final String BCREG_RESPONSE_OK2 = """
      {
        "name": "SAMPLE COMPANY",
        "id": "AA0000001",
        "goodStanding": true,
        "addresses": []
      }""";

  public static final String BCREG_RESPONSE_NOK = "No data found for client number AA0000001";

  public static final String BCREG_MISSING_ACCOUNTID = """
      {
          "errorMessage": "API required account ID is missing."
      }""";

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
}
