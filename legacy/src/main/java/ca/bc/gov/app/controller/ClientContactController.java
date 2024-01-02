package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.ForestClientContactDto;
import ca.bc.gov.app.service.ClientContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(value = "/api/contacts", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ClientContactController {

  private final ClientContactService service;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<String> saveLocation(@RequestBody ForestClientContactDto dto) {
    return service.saveAndGetIndex(dto);
  }

  @GetMapping("/search")
  public Flux<ForestClientContactDto> findIndividuals(
      @RequestParam String firstName,
      @RequestParam String lastName,
      @RequestParam String email,
      @RequestParam String phone
  ) {
    return service.search(firstName, lastName, email, phone);
  }

}
