package ca.bc.gov.app.service.legacy;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.legacy.ClientDoingBusinessAsDto;
import ca.bc.gov.app.dto.legacy.ForestClientContactDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.dto.legacy.ForestClientLocationDto;
import ca.bc.gov.app.entity.SubmissionLocationEntity;
import ca.bc.gov.app.repository.CountryCodeRepository;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class LegacyService {

  private final WebClient legacyApi;
  private final CountryCodeRepository countryCodeRepository;
  private final Map<String, String> countryList = new HashMap<>();


  public LegacyService(
      @Qualifier("legacyClientApi") WebClient legacyApi,
      CountryCodeRepository countryCodeRepository
  ) {
    this.legacyApi = legacyApi;
    this.countryCodeRepository = countryCodeRepository;
  }

  /**
   * Loads the country list from the database.
   */
  @PostConstruct
  public void setUp() {
    countryCodeRepository.findAll().doOnNext(
            countryCode -> countryList.put(countryCode.getCountryCode(), countryCode.getDescription()))
        .collectList().subscribe();
  }


  public Mono<String> createLocation(
      SubmissionLocationEntity detail,
      String clientNumber,
      Long index,
      String user
  ) {

    ForestClientLocationDto dto =
        new ForestClientLocationDto(
            clientNumber,
            String.format("%02d", index),
            detail.getName(),
            detail.getStreetAddress(),
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            detail.getCityName(),
            detail.getProvinceCode(),
            detail.getPostalCode(),
            countryList.getOrDefault(detail.getCountryCode(), detail.getCountryCode()),
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            "N",
            null,
            "Y",
            StringUtils.EMPTY,
            user,
            user,
            ApplicationConstant.ORG_UNIT,
            ApplicationConstant.ORG_UNIT
        );

    return postRequestToLegacy("/api/locations", dto)
        .thenReturn(clientNumber);
  }

  public Mono<String> createContact(ForestClientContactDto dto) {
    return postRequestToLegacy("/api/contacts", dto)
        .thenReturn(dto.clientNumber());
  }

  public Mono<String> createClient(ForestClientDto dto) {
    return postRequestToLegacy("/api/clients", dto);
  }

  public Mono<String> createDoingBusinessAs(
      String clientNumber,
      String doingBusinessAsName,
      String createdBy,
      String updatedBy
  ) {
    return postRequestToLegacy(
        "/api/dba",
        new ClientDoingBusinessAsDto(
            clientNumber,
            doingBusinessAsName,
            createdBy,
            updatedBy,
            ApplicationConstant.ORG_UNIT
        )
    )
        .thenReturn(clientNumber);
  }

  public Flux<ClientDoingBusinessAsDto> matchDba(String dbaName) {
    return legacyApi
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/dba/search")
                .queryParam("dbaName", dbaName)
                .build()
        )
        .exchangeToFlux(response -> {
          if (response.statusCode().is2xxSuccessful()) {
            return response.bodyToFlux(ClientDoingBusinessAsDto.class);
          } else {
            return Flux.empty();
          }
        });
  }

  private Mono<String> postRequestToLegacy(
      String url,
      Object dto
  ) {
    return
        legacyApi
            .post()
            .uri(url)
            .body(BodyInserters.fromValue(dto))
            .exchangeToMono(response -> {
              //if 201 is good, else already exist, so move forward
              if (response.statusCode().is2xxSuccessful()) {
                return response.bodyToMono(String.class);
              } else {
                return Mono.error(new RuntimeException("Failed to submit " + url));
              }
            });
  }

}
