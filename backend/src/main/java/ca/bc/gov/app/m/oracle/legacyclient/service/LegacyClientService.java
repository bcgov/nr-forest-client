package ca.bc.gov.app.m.oracle.legacyclient.service;

import java.util.Date;
import java.util.List;

import ca.bc.gov.app.m.oracle.legacyclient.entity.ForestClientEntity;

public interface LegacyClientService {
	
	String BEAN_NAME = "legacyClientViewService";

	List<ForestClientEntity> findClientByIncorporationOrName(String incorporationNumber, String companyName);

	List<ForestClientEntity> findClientIndividualByNameAndDOB(String firstName, String lastName, Date birthdate);

}
