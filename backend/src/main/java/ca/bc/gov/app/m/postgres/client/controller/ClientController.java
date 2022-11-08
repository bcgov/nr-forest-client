package ca.bc.gov.app.m.postgres.client.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.app.core.configuration.PostgresPersistenceConfiguration;
import io.swagger.annotations.Api;

@Api(tags = PostgresPersistenceConfiguration.POSTGRES_API_TAG)
@RestController
@RequestMapping("app/m/client")
public class ClientController {

    public static final Logger logger = LoggerFactory.getLogger(ClientController.class);


}
