package ca.bc.gov.app.m.oracle.legacyclient.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import ca.bc.gov.app.core.configuration.OraclePersistenceConfiguration;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ForestClientEntity;
import ca.bc.gov.app.m.oracle.legacyclient.service.LegacyClientService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = OraclePersistenceConfiguration.ORACLE_API_TAG)
@CrossOrigin(origins = "${frontend.url}")
@RestController
@RequestMapping("/app/m/legacyclient")
public class LegacyClientController {

  public static final Logger logger = LoggerFactory.getLogger(LegacyClientController.class);

  @Autowired
  private LegacyClientService legacyClientService;

  @RequestMapping(value = "/findClientByIncorporationNumberOrName", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
  public List<ForestClientEntity> findClientByIncorporationOrName(
      @Param("incorporationNumber") String incorporationNumber,
      @Param("companyName") String companyName) {

    return legacyClientService.findClientByIncorporationOrName(incorporationNumber, companyName);
  }

  @RequestMapping(value = "/findClientByNameAndBirthdate", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
  public List<ForestClientEntity> findClientByNameAndBirthdate(
      @RequestParam("firstName") String firstName,
      @RequestParam("lastName") String lastName,
      @Parameter(name = "birthdate", required = true, description = "in the format of yyyy-mm-dd")
      String birthdate) {

    return legacyClientService.findClientByNameAndBirthdate(firstName, lastName, birthdate);
  }

}
