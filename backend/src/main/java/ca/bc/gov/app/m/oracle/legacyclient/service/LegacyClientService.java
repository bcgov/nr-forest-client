package ca.bc.gov.app.m.oracle.legacyclient.service;

import java.util.List;

import ca.bc.gov.app.m.oracle.legacyclient.entity.ForestClientEntity;

public interface LegacyClientService {
	
	String BEAN_NAME = "legacyClientViewService";

	List<ForestClientEntity> findClientByIncorporationOrName(String incorporationNumber, String companyName);

	List<ForestClientEntity> findClientByNameAndBirthdate(String firstName, String lastName, String birthdate);

}
