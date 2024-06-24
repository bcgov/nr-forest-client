package ca.bc.gov.app.service.client;

import ca.bc.gov.app.configuration.ForestClientConfiguration;
import ca.bc.gov.app.dto.client.AddressCompleteFindListDto;
import ca.bc.gov.app.dto.client.AddressCompleteRetrieveListDto;
import ca.bc.gov.app.dto.client.AddressError;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.exception.AddressLookupException;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@Observed
public class ClientAddressService {

  private final ForestClientConfiguration.AddressCompleteConfiguration configuration;
  private final WebClient addressCompleteApi;
  private final ObservationRegistry registry;

  public ClientAddressService(
      ForestClientConfiguration configuration,
      @Qualifier("addressCompleteApi") WebClient addressCompleteApi,
      ObservationRegistry registry
  ) {
    this.configuration = configuration.getAddressComplete();
    this.addressCompleteApi = addressCompleteApi;
    this.registry = registry;
  }

  public Flux<CodeNameDto> findPossibleAddresses(
      String country, Integer maxSuggestions, String searchTerm) {
    log.info("Searching for address {} for country {}", searchTerm, country);
    return
        addressCompleteApi
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/find/v2.10/json3.ws")
                    .queryParam("key", configuration.getApiKey())
                    .queryParam("Country", country)
                    .queryParam("MaxSuggestions", maxSuggestions)
                    .queryParam("SearchTerm", searchTerm)
                    .build(Map.of())
            )
            .retrieve()
            .bodyToMono(AddressCompleteFindListDto.class)
            .name("request.canadapost")
            .tag("kind", "search")
            .tap(Micrometer.observation(registry))
            .map(AddressCompleteFindListDto::items)
            .flatMap(addresses -> {
              try {
                handleError(addresses);
              } catch (AddressLookupException e) {
                return Mono.error(e);
              }

              boolean findOnly = addresses
                  .stream()
                  .allMatch(address -> "Find" .equalsIgnoreCase(address.next()));

              if (!findOnly) {
                return Mono.just(addresses);
              }

              String lastId = addresses.get(0).id();

              log.info("No retrieve addresses found, searching for lastId {}", lastId);

              return addressCompleteApi
                  .get()
                  .uri(uriBuilder ->
                      uriBuilder
                          .path("/find/v2.10/json3.ws")
                          .queryParam("key", configuration.getApiKey())
                          .queryParam("LastId", lastId)
                          .build(Map.of()))
                  .retrieve()
                  .bodyToMono(AddressCompleteFindListDto.class)
                  .name("request.canadapost")
                  .tag("kind", "lastid")
                  .tap(Micrometer.observation(registry))
                  .map(AddressCompleteFindListDto::items)
                  .flatMap(lastIdAddresses -> {

                    try {
                      handleError(lastIdAddresses);
                    } catch (AddressLookupException e) {
                      return Mono.error(e);
                    }

                    return Mono.just(lastIdAddresses);
                  });
            })
            .flatMapMany(Flux::fromIterable)
            .filter(address -> "Retrieve" .equalsIgnoreCase(address.next()))
            .map(address -> new CodeNameDto(
                    address.id(),
                    String.format("%s %s", address.text().trim(), address.description().trim())
                )
            );
  }

  public Mono<ClientAddressDto> getAddress(String addressId) {
    log.info("Get address with Id {}", addressId);
    return
        addressCompleteApi
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/retrieve/v2.11/json3.ws")
                    .queryParam("key", configuration.getApiKey())
                    .queryParam("id", addressId)
                    .build(Map.of())
            )
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(AddressCompleteRetrieveListDto.class)
            .name("request.canadapost")
            .tag("kind", "get")
            .tap(Micrometer.observation(registry))
            .map(AddressCompleteRetrieveListDto::items)
            .flatMap(addresses -> {
              try {
                handleError(addresses);
              } catch (AddressLookupException e) {
                return Mono.error(e);
              }

              return Mono.just(addresses);
            })
            .map(addresses ->
                addresses.stream()
                    .filter(address -> "ENG" .equalsIgnoreCase(address.language()))
                    .findFirst()
                    .orElseGet(() -> addresses.get(0))
            )
            .map(address ->
                new ClientAddressDto(
                    String.format("%s %s %s %s %s", address.line1(), address.line2(),
                        address.line3(), address.line4(), address.line5()
                    ).trim(),
                    null,
                    null,
                    new ClientValueTextDto(address.countryIso2(), address.countryName()),
                    new ClientValueTextDto(address.province(), address.provinceName()),
                    address.city(),
                    address.postalCode(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    0,
                    null
                )
            );
  }

  private <T extends AddressError> void handleError(List<T> addresses) {

    if(CollectionUtils.isEmpty(addresses)) {
      throw new AddressLookupException("No address data found");
    }

    boolean hasError = addresses
        .stream()
        .allMatch(address -> StringUtils.isNotBlank(address.error()));

    if(hasError) {
      throw new AddressLookupException(addresses.get(0).description());
    }
  }
}
