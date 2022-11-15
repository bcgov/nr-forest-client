package ca.bc.gov.app.m.postgres.client.service;

import java.util.List;

import ca.bc.gov.app.m.postgres.client.entity.ClientTypeCodeEntity;

public interface ClientService {
	
	String BEAN_NAME = "clientService";

	List<ClientTypeCodeEntity> findActiveClientTypeCodes();

}
