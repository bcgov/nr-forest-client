package ca.bc.gov.app.m.oracle.legacyclient.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.app.core.configuration.OraclePersistenceConfiguration;
import ca.bc.gov.app.core.util.CoreUtil;
import ca.bc.gov.app.m.oracle.legacyclient.service.LegacyClientService;
import ca.bc.gov.app.m.oracle.legacyclient.vo.ClientPublicViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Api(tags = OraclePersistenceConfiguration.ORACLE_API_TAG)
@RestController
@RequestMapping("app/m/legacyclient/")
public class LegacyClientController {

    public static final Logger logger = LoggerFactory.getLogger(LegacyClientController.class);

    @Inject
    private LegacyClientService legacyClientService;

    @Inject
    private CoreUtil coreUtil;

    @RequestMapping(value = "/findByClientNumber", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findByClientNumber(@RequestParam("clientNumber") String clientNumber) {
        if (coreUtil.isNumber(clientNumber)) {
            return ResponseEntity.ok(legacyClientService.findByClientNumber(clientNumber));
        } 
        else {
            return new ResponseEntity<Object>("Couldn't recognize one or many properties. Please check parameters!",
                                              HttpStatus.BAD_REQUEST);
        }
    }

    //TODO: Improve logic
    @GetMapping("/findAllNonIndividuals")
    public ResponseEntity<Page<ClientPublicViewVO>> findAllNonIndividuals(@RequestParam(defaultValue = "0") Integer currentPage,
                                                                          @RequestParam(defaultValue = "10") Integer itemsPerPage,
                                                                          @RequestParam(defaultValue = "CLIENT_NUMBER") String sortBy) {
        try {
            Page<ClientPublicViewVO> clientData = legacyClientService.findAllNonIndividualClients(currentPage, itemsPerPage, sortBy);
            return new ResponseEntity<Page<ClientPublicViewVO>>(clientData, HttpStatus.OK);
        } 
        catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @RequestMapping(value = "/findByNames", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> findByNames(@RequestParam(name="clientNumber", required=false) 
    										  @ApiParam(value = "The name of the entity or individual's last name") 
    										  String clientName,
    										  
    										  @RequestParam(name="clientFirstName", required=false) 
    										  @ApiParam(value = "The client's first name", required=false) 
    										  String clientFirstName,
    										  
    										  @RequestParam(name="clientMiddleName", required=false) 
    										  @ApiParam(value = "The client's middle name", required=false) 
    										  String clientMiddleName,
    										  
    										  @RequestParam(name="clientTypeCodesAsCsv", required=false) 
    										  @ApiParam(value = "A code indicating a type of ministry client.<br>" +
    									                 		"Examples include but are not limited to: Corporation, Individual, Association, First Nation Band...<br>" + 
    									                 		"Please enter one or more client type codes as CSV, i.e. C,A,B.") 
    										  String clientTypeCodesAsCsv,
    										  
    										  @RequestParam(defaultValue = "1") Integer currentPage,
                                              @RequestParam(defaultValue = "10") Integer itemsPerPage) {
    	
    	return ResponseEntity.ok(legacyClientService.findByNames(clientName,
    															 clientFirstName,
    															 clientMiddleName,
    															 clientTypeCodesAsCsv,
    															 currentPage,
    															 itemsPerPage));
    }

}
