package ca.bc.gov.app.m.postgres.client.service;

import java.util.List;

import ca.bc.gov.app.core.vo.CodeDescrVO;

public interface ClientService {
	
	String BEAN_NAME = "clientService";

	List<CodeDescrVO> findActiveClientTypeCodes();

}
