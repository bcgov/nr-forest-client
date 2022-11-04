package ca.bc.gov.app.m.oracle.legacyclient.service;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import ca.bc.gov.app.m.oracle.legacyclient.vo.ClientPublicViewVO;

public interface LegacyClientService {
	
	String BEAN_NAME = "legacyClientViewService";

	ClientPublicViewVO findByClientNumber(String clientNumber);

	Page<ClientPublicViewVO> findAllNonIndividualClients(Integer currentPage, 
														 Integer itemsPerPage, 
														 String sortBy);

	ResponseEntity<Object> findByNames(String clientName, 
									   String clientFirstName, 
									   String clientMiddleName,
									   String clientTypeCodesAsCsv, 
									   Integer currentPage, 
									   Integer itemsPerPage);
}
