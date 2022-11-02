package ca.bc.gov.app.m.oracle.datavalidation.service;

import java.util.List;

import ca.bc.gov.app.m.oracle.datavalidation.vo.FirstNationBandVidationVO;

public interface DataValidationService {
	
	String BEAN_NAME = "dataValidationService";

	List<FirstNationBandVidationVO> validateFirstNationBand();

}
