package ca.bc.gov.app.m.om.service;

import java.util.List;

import ca.bc.gov.app.m.om.vo.FirstNationBandVidationVO;

public interface OpenMapsService {
	
	String BEAN_NAME = "openMapsService";

	List<FirstNationBandVidationVO> validateFirstNationBand();

}
