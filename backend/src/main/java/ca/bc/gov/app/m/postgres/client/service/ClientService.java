package ca.bc.gov.app.m.postgres.client.service;

import java.util.List;

import ca.bc.gov.app.m.postgres.client.entity.ClientStatusCodeEntity;

public interface ClientService {
	
	String BEAN_NAME = "clientService";

	List<ClientStatusCodeEntity> findAllClientStatusCodes();

}
