package ca.bc.gov.app.m.postgres.client.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.app.core.configuration.PostgresPersistenceConfiguration;
import ca.bc.gov.app.core.vo.CodeDescrVO;
import ca.bc.gov.app.m.postgres.client.service.ClientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

@Tag(name =  PostgresPersistenceConfiguration.POSTGRES_API_TAG)
@RestController
@RequestMapping("app/m/client")
public class ClientController {

    public static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Inject
    private ClientService clientService;

    @RequestMapping(value = "/findActiveClientTypeCodes", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public List<CodeDescrVO> findActiveClientTypeCodes() {
        return clientService.findActiveClientTypeCodes();
    }

}
