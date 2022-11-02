package ca.bc.gov.app.m.om.controller;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.app.m.om.service.OpenMapsService;
import ca.bc.gov.app.m.om.vo.FirstNationBandVidationVO;
import io.swagger.annotations.Api;

@Api(tags = "Open Maps")
@RestController
@RequestMapping("app/m/openmaps/")
public class OpenMapsController {

    public static final Logger logger = LoggerFactory.getLogger(OpenMapsController.class);

    @Inject
    private OpenMapsService dataValidationService;

    @GetMapping("/validateFirstNationBand")
    public List<FirstNationBandVidationVO> validateFirstNationBand() {
    	List<FirstNationBandVidationVO> validationResult = dataValidationService.validateFirstNationBand();
      return validationResult;
    }

}
