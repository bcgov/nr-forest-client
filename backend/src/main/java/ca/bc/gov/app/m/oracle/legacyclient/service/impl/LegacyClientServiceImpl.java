package ca.bc.gov.app.m.oracle.legacyclient.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ca.bc.gov.app.m.oracle.legacyclient.service.LegacyClientService;

@Service(LegacyClientService.BEAN_NAME)
public class LegacyClientServiceImpl implements LegacyClientService {
		
	public static final Logger logger = LoggerFactory.getLogger(LegacyClientServiceImpl.class);
	
}
