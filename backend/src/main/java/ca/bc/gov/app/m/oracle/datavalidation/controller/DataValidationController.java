package ca.bc.gov.app.m.oracle.datavalidation.controller;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.app.m.oracle.datavalidation.service.DataValidationService;
import ca.bc.gov.app.m.oracle.datavalidation.vo.FirstNationBandVidationVO;
import io.swagger.annotations.Api;

@Api(tags = "Oracle Data Validation")
@RestController
@RequestMapping("app/m/oracle/validation/")
public class DataValidationController {

    public static final Logger logger = LoggerFactory.getLogger(DataValidationController.class);

    @Inject
    private DataValidationService dataValidationService;

    @GetMapping("/validateFirstNationBand")
    public List<FirstNationBandVidationVO> validateFirstNationBand() {
    	List<FirstNationBandVidationVO> validationResult = dataValidationService.validateFirstNationBand();
      return validationResult;
    }

}
