package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.CodeNameDto;
import ca.bc.gov.app.dto.ForestClientLocationDto;
import ca.bc.gov.app.service.ClientLocationService;
import io.micrometer.observation.annotation.Observed;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
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

/**
 * REST controller for managing forest client locations.
 *
 * <p>This controller provides endpoints for creating, searching, and retrieving
 * client location information. It exposes operations under the {@code /api/locations}
 * base path and produces JSON responses.</p>
 *
 * @see ClientLocationService
 * @see ForestClientLocationDto
 * @see CodeNameDto
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/locations", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Observed
@Validated
public class ClientLocationController {

  private final ClientLocationService service;

  /**
   * Creates a new location for a forest client.
   *
   * <p>This endpoint receives a location data transfer object and persists it
   * to the database. Upon successful creation, it returns the client number
   * associated with the saved location.</p>
   *
   * @param dto the location data transfer object containing location details
   * @return a {@link Mono} emitting the client number of the saved location
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<String> saveLocation(@RequestBody ForestClientLocationDto dto) {
    log.info("Receiving request to save location for {}: {}", dto.clientNumber(),
        dto.clientLocnName());
    return service.saveAndGetIndex(dto);
  }

  /**
   * Searches for client locations by address and postal code.
   *
   * <p>This endpoint allows searching for existing client locations that match
   * the provided address and postal code criteria.</p>
   *
   * @param address    the address to search for
   * @param postalCode the postal code to search for
   * @return a {@link Flux} emitting matching {@link ForestClientLocationDto} objects
   */
  @GetMapping("/search")
  public Flux<ForestClientLocationDto> findLocations(
      @RequestParam String address,
      @RequestParam String postalCode
  ) {
    log.info("Receiving request to search by address {} and postal code {}", address, postalCode);
    return service.search(address, postalCode);
  }

  /**
   * Retrieves all locations that were updated when the client status changed.
   *
   * <p>This endpoint finds all location records that were modified at the same time
   * as the client's status change. This is useful for identifying which locations
   * were affected when a client was activated or deactivated.</p>
   *
   * @param clientNumber the unique identifier of the forest client
   * @param clientStatus the client status code. Valid values are:
   *                     <ul>
   *                       <li>{@code ACT} - Active</li>
   *                       <li>{@code DAC} - Deactivated</li>
   *                       <li>{@code DEC} - Deceased</li>
   *                       <li>{@code REC} - Receivership</li>
   *                       <li>{@code SPN} - Suspended</li>
   *                     </ul>
   * @return a {@link Flux} emitting {@link CodeNameDto} objects containing
   *         location codes and names that were updated with the client
   */
  @GetMapping("/{clientNumber}/{clientStatus}")
  public Flux<CodeNameDto> findAllLocationUpdatedWithClient(
      @PathVariable String clientNumber,
      @PathVariable 
      @Pattern(regexp = "^(ACT|DAC|DEC|REC|SPN)$", 
               message = "Client status must be one of: ACT, DAC, DEC, REC, SPN")
      String clientStatus
  ) {
    log.info("Receiving request to find all location updated with client {} and status {}",
        clientNumber, clientStatus
    );
    return service
        .findAllLocationUpdatedWithClient(clientNumber, clientStatus);
  }

}
