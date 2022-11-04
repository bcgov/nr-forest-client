package ca.bc.gov.app.m.ches.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

public interface ChesEmailService {

    String BEAN_NAME = "chesEmailService";

    ResponseEntity<Object> sendEmail(List<String> emailTo, String emailBody);
}
