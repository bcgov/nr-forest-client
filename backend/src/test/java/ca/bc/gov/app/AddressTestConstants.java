package ca.bc.gov.app;

public class AddressTestConstants {
  public static final String ADDRESS_API_KEY = "AB12-CD34-EF56-GH78";

  public static final String LAST_ID =
      "BR|TT|POR|SÃO_PAULO-RIO_CLARO-JARDIM_RESIDENCIAL_DAS_PALMEIRAS-RUA_OITO_JP";

  public static final String ERROR = """
      {
          "Items": [
              {
                  "Error": "1001",
                  "Description": "SearchTerm or LastId Required",
                  "Cause": "The SearchTerm or LastId parameters were not supplied.",
                  "Resolution": "Check they were supplied and try again."
              }
          ]
      }""";

  public static final String EMPTY = """
      {
          "Items": []
      }
      """;

  public static final String ONLY_FIND_ADDRESS = """
      {
          "Items": [
              {
                  "Id": "BR|TT|POR|SÃO_PAULO-RIO_CLARO-JARDIM_RESIDENCIAL_DAS_PALMEIRAS-RUA_OITO_JP",
                  "Text": "Rua Oito JP",
                  "Highlight": "0-11",
                  "Cursor": 0,
                  "Description": "Jardim Residencial Das Palmeiras, Rio Claro - 2543 Addresses",
                  "Next": "Find"
              }
          ]
      }
      """;

  public static final String POSSIBLE_ADDRESSES_AFTER_FIND = """
      {
          "Items": [
              {
                  "Id": "BR|TT|B|10760092424108|1",
                  "Text": "Rua Oito JP 1",
                  "Highlight": "",
                  "Cursor": 0,
                  "Description": "Jardim Residencial Das Palmeiras, 13502130 Rio Claro",
                  "Next": "Retrieve"
              },
              {
                  "Id": "BR|TT|B|10760092424108|2",
                  "Text": "Rua Oito JP 2",
                  "Highlight": "",
                  "Cursor": 0,
                  "Description": "Jardim Residencial Das Palmeiras, 13502130 Rio Claro",
                  "Next": "Retrieve"
              },
              {
                  "Id": "BR|TT|B|10760092424108|3",
                  "Text": "Rua Oito JP 3",
                  "Highlight": "",
                  "Cursor": 0,
                  "Description": "Jardim Residencial Das Palmeiras, 13502130 Rio Claro",
                  "Next": "Retrieve"
              }
          ]
      }
      """;

  public static final String POSSIBLE_ADDRESSES = """
      {
          "Items": [
              {
                  "Id": "CA|CP|B|4867056",
                  "Text": "511-860 Rue Trémoy",
                  "Highlight": "0-3,4-7",
                  "Cursor": 0,
                  "Description": "Québec, QC, G1X 3Z2",
                  "Next": "Retrieve"
              },
              {
                  "Id": "CA|CP|B|7594083",
                  "Text": "511-860 Canterbury Ave",
                  "Highlight": "0-3,4-7",
                  "Cursor": 0,
                  "Description": "Ottawa, ON, K1G 3B2",
                  "Next": "Retrieve"
              },
              {
                  "Id": "CA|CP|B|8853396",
                  "Text": "511-860 Midridge Dr SE",
                  "Highlight": "0-3,4-7",
                  "Cursor": 0,
                  "Description": "Calgary, AB, T2X 1K1",
                  "Next": "Retrieve"
              },
              {
                  "Id": "CA|CP|B|10314700",
                  "Text": "511-860 View St",
                  "Highlight": "0-3,4-7",
                  "Cursor": 0,
                  "Description": "Victoria, BC, V8W 3Z8",
                  "Next": "Retrieve"
              },
              {
                  "Id": "CA|CP|B|12291339",
                  "Text": "511-860 Blackthorne Ave",
                  "Highlight": "0-3,4-7",
                  "Cursor": 0,
                  "Description": "Ottawa, ON, K1K 3Y7",
                  "Next": "Retrieve"
              },
              {
                  "Id": "CA|CP|B|989565",
                  "Text": "511-8600 Franklin Ave",
                  "Highlight": "0-3,4-7",
                  "Cursor": 0,
                  "Description": "Fort McMurray, AB, T9H 4G8",
                  "Next": "Retrieve"
              },
              {
                  "Id": "CA|CP|B|20358774",
                  "Text": "511-8601 16th Ave",
                  "Highlight": "0-3,4-7",
                  "Cursor": 0,
                  "Description": "Burnaby, BC, V3N 0G1",
                  "Next": "Retrieve"
              }
          ]
      }""";

  public static final String GET_ADDRESS = """
      {
          "Items": [
              {
                  "Id": "CA|CP|B|0000001",
                  "DomesticId": "1",
                  "Language": "FRE",
                  "LanguageAlternatives": "FRE,ENG",
                  "Department": "",
                  "Company": "",
                  "SubBuilding": "",
                  "BuildingNumber": "7360",
                  "BuildingName": "",
                  "SecondaryStreet": "",
                  "Street": "Rue Frontenac",
                  "Block": "",
                  "Neighbourhood": "",
                  "District": "",
                  "City": "Saint-Hyacinthe",
                  "Line1": "7360 Rue Frontenac",
                  "Line2": "",
                  "Line3": "",
                  "Line4": "",
                  "Line5": "",
                  "AdminAreaName": "",
                  "AdminAreaCode": "",
                  "Province": "QC",
                  "ProvinceName": "Québec",
                  "ProvinceCode": "QC",
                  "PostalCode": "J2S 7A9",
                  "CountryName": "Canada",
                  "CountryIso2": "CA",
                  "CountryIso3": "CAN",
                  "CountryIsoNumber": "124",
                  "SortingNumber1": "",
                  "SortingNumber2": "",
                  "Barcode": "",
                  "POBoxNumber": "",
                  "Label": "7360 Rue Frontenac\\nSAINT-HYACINTHE QC J2S 7A9\\nCANADA",
                  "Type": "Residential",
                  "DataLevel": "Premise",
                  "AcRbdi": "R",
                  "AcMua": "0"
              },
              {
                  "Id": "CA|CP|B|0000001",
                  "DomesticId": "1",
                  "Language": "ENG",
                  "LanguageAlternatives": "FRE,ENG",
                  "Department": "",
                  "Company": "",
                  "SubBuilding": "",
                  "BuildingNumber": "7360",
                  "BuildingName": "",
                  "SecondaryStreet": "",
                  "Street": "Frontenac St",
                  "Block": "",
                  "Neighbourhood": "",
                  "District": "",
                  "City": "Saint-Hyacinthe",
                  "Line1": "7360 Frontenac St",
                  "Line2": "",
                  "Line3": "",
                  "Line4": "",
                  "Line5": "",
                  "AdminAreaName": "",
                  "AdminAreaCode": "",
                  "Province": "QC",
                  "ProvinceName": "Quebec",
                  "ProvinceCode": "QC",
                  "PostalCode": "J2S 7A9",
                  "CountryName": "Canada",
                  "CountryIso2": "CA",
                  "CountryIso3": "CAN",
                  "CountryIsoNumber": "124",
                  "SortingNumber1": "",
                  "SortingNumber2": "",
                  "Barcode": "",
                  "POBoxNumber": "",
                  "Label": "7360 Frontenac St\\nSAINT-HYACINTHE QC J2S 7A9\\nCANADA",
                  "Type": "Residential",
                  "DataLevel": "Premise",
                  "AcRbdi": "R",
                  "AcMua": "0"
              }
          ]
      }""";
}
