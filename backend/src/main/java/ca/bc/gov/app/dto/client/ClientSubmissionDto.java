package ca.bc.gov.app.dto.client;

import java.util.Map;

public record ClientSubmissionDto(
    ClientBusinessInformationDto businessInformation,
    ClientLocationDto location
) {
  public Map<String, Object> description() {
    return
        Map
            .of(
                "business", businessInformation.description(),
                "location", location.description(),
                "submitter", submitterInformation.description()
            );
  }
}
