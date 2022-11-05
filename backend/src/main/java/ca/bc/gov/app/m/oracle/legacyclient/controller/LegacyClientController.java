package ca.bc.gov.app.m.oracle.legacyclient.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.app.core.configuration.OraclePersistenceConfiguration;
import io.swagger.annotations.Api;

@Api(tags = OraclePersistenceConfiguration.ORACLE_API_TAG)
@RestController
@RequestMapping("app/m/legacyclient/")
public class LegacyClientController {

	public static final Logger logger = LoggerFactory.getLogger(LegacyClientController.class);

}
