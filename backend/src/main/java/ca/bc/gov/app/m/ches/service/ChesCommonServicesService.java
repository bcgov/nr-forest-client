package ca.bc.gov.app.m.ches.service;

import org.springframework.http.ResponseEntity;

public interface ChesCommonServicesService {

    String BEAN_NAME = "chesCommonServicesService";

    ResponseEntity<Object> sendEmail(String emailTo, String emailBody);
}
