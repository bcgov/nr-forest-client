package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.dto.bcregistry.ClientDetailsDto;
import ca.bc.gov.app.dto.client.ClientLookUpDto;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.client.EmailRequestDto;
import ca.bc.gov.app.exception.NoClientDataFound;
import ca.bc.gov.app.service.client.ClientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.WordUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Tag(
    name = "FSA Clients",
    description = "The FSA Client endpoint, responsible for handling client data"
)
@RequestMapping(value = "/api/clients", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ClientController {

  private final ClientService clientService;

  @GetMapping("/{clientNumber}")
  public Mono<ClientDetailsDto> getClientDetails(@PathVariable String clientNumber) {
    return clientService.getClientDetails(clientNumber);
  }

  @GetMapping("/activeCountryCodes")
  public Flux<CodeNameDto> listCountries(
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size) {
    return clientService
        .listCountries(page, size);
  }
  
  @GetMapping("/getCountryByCode/{countryCode}")
  public Mono<Object> getCountryByCode(
      @PathVariable String countryCode) {
    return clientService.getCountryByCode(countryCode);
  }

  @GetMapping("/activeCountryCodes/{countryCode}")
  public Flux<CodeNameDto> listProvinces(
      @PathVariable String countryCode,
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size) {
    return clientService
        .listProvinces(countryCode, page, size);
  }

  @GetMapping("/activeClientTypeCodes")
  public Flux<CodeNameDto> findActiveClientTypeCodes() {
    return clientService
        .findActiveClientTypeCodes(LocalDate.now());
  }

  @GetMapping("/activeContactTypeCodes")
  public Flux<CodeNameDto> listClientContactTypeCodes(
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size
  ) {
    return clientService
        .listClientContactTypeCodes(page, size);
  }

  /**
   * Retrieve a Flux of ClientLookUpDto objects by searching for clients with a specific name.
   *
   * @param name The name to search for.
   * @return A Flux of ClientLookUpDto objects that match the given name.
   */
  @GetMapping(value = "/name/{name}")
  public Flux<ClientLookUpDto> findByClientName(
      @PathVariable String name
  ) {
    return clientService
        .findByClientNameOrIncorporation(name)
        .map(client -> client.withName(WordUtils.capitalize(client.name())));
  }

  @GetMapping(value = "/incorporation/{incorporationId}")
  public Mono<ClientLookUpDto> findByIncorporationNumber(
      @PathVariable String incorporationId) {
    return clientService
        .findByClientNameOrIncorporation(incorporationId)
        .next()
        .switchIfEmpty(Mono.error(new NoClientDataFound(incorporationId)));
  }

  @PostMapping("/mail")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<Void> sendEmail(@RequestBody EmailRequestDto emailRequestDto) {
    return clientService.triggerEmailDuplicatedClient(emailRequestDto);
  }
  
}
