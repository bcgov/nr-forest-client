package ca.bc.gov.app.m.ob.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import ca.bc.gov.app.m.ob.vo.OrgBookResponseVO;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ClientDoingBusinessAsEntity;

public interface OrgBookApiService {

    String BEAN_NAME = "orgBookApiService";

    ResponseEntity<Object> findByIncorporationNumber(String incorporationNumber);

	List<ClientDoingBusinessAsEntity> validateClientDoingBusinessAs();

	OrgBookResponseVO findByClientName(String clientName);
    
}
