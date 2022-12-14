package ca.bc.gov.app.m.ob.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import ca.bc.gov.app.m.ob.service.OrgBookApiService;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ClientDoingBusinessAsEntity;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ForestClientEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//NOTE: 
//This class is for testing purposes. This will be removed. 

@Tag(name = "OrgBook")
@RestController
@RequestMapping("/app/m/orgbook")
public class OrgBookApiController {

  public static final Logger logger = LoggerFactory.getLogger(OrgBookApiController.class);

  @Autowired
  private OrgBookApiService orgBookApiService;

  @RequestMapping(value = "/findByIncorporationNumber",
      method = RequestMethod.GET,
      produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> findByIncorporationNumber(
      @RequestParam("incorporationNumber") String incorporationNumber) {
    return orgBookApiService.findByIncorporationNumber(incorporationNumber);
  }

  //TODO: Remove it as it is for Data Analysis
  @GetMapping("/validateClientDoingBusinessAs")
  public List<ClientDoingBusinessAsEntity> validateClientDoingBusinessAs() {
    List<ClientDoingBusinessAsEntity> clients = orgBookApiService.validateClientDoingBusinessAs();
    return clients;
  }

  //TODO: Remove it as it is for Data Analysis
  @GetMapping("/validateUnregisteredCompanies")
  public List<ForestClientEntity> validateUnregisteredCompanies() {
    List<ForestClientEntity> clients = orgBookApiService.validateUnregisteredCompanies();
    return clients;
  }

}
