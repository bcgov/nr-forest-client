package ca.bc.gov.app.m.ob.service;

import org.springframework.http.ResponseEntity;

public interface OrgBookApiService {

    String BEAN_NAME = "orgBookApiService";

    ResponseEntity<Object> findByIncorporationNumber(String incorporationNumber);
    
}
