package ca.bc.gov.app.m.postgres.client.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import ca.bc.gov.app.core.CoreConstant;
import ca.bc.gov.app.core.dto.CodeDescrDTO;
import ca.bc.gov.app.m.postgres.client.service.ClientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = CoreConstant.POSTGRES_API_TAG)
@RestController
@RequestMapping("/app/m/client")
@RequiredArgsConstructor
public class ClientController {

  private final ClientService clientService;

  @GetMapping(value = "/findActiveClientTypeCodes", produces = APPLICATION_JSON_VALUE)
  public List<CodeDescrDTO> findActiveClientTypeCodes() {
    return clientService.findActiveClientTypeCodes();
  }

}
