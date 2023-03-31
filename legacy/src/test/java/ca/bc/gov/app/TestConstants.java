package ca.bc.gov.app;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestConstants {

  public static final String ORGBOOK_TOPIC = """
      {
        "total": 1,
        "page": 1,
        "results": [
          {
            "id": 1461713,
            "source_id": "FM0000000",            
            "names": [
              {
                "text": "DUMMY",                
                "type": "entity_name"
              }
            ],              
            "inactive": false
          }
        ]
      }""";
}
