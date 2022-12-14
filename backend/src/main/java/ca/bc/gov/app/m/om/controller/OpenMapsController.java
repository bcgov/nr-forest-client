package ca.bc.gov.app.m.om.controller;

//import java.util.List;
//
//import javax.inject.Inject;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Open Maps")
@RestController
@RequestMapping("/app/m/openmaps")
public class OpenMapsController {

  public static final Logger logger = LoggerFactory.getLogger(OpenMapsController.class);
//
//    @Autowired
//    private OpenMapsService dataValidationService;
//
//    @ApiOperation(value = "This method is used to get the current date.", hidden = true)
//    @GetMapping("/validateFirstNationBand")
//    public List<FirstNationBandVidationVO> validateFirstNationBand() {
//    	List<FirstNationBandVidationVO> validationResult = dataValidationService.validateFirstNationBand();
//      return validationResult;
//    }

}
