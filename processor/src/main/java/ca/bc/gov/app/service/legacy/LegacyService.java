package ca.bc.gov.app.service.legacy;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.legacy.ClientDoingBusinessAsDto;
import ca.bc.gov.app.dto.legacy.ForestClientContactDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.dto.legacy.ForestClientLocationDto;
import ca.bc.gov.app.entity.SubmissionLocationEntity;
import java.util.Locale;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
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

  public LegacyService(
      @Qualifier("legacyClientApi") WebClient legacyApi
  ) {
    this.legacyApi = legacyApi;
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
            detail.getName().toUpperCase(Locale.ROOT),
            detail.getAddressValue1(),
            detail.getAddressValue2(),
            detail.getAddressValue3(),
            detail.getCityName().toUpperCase(Locale.ROOT),
            detail.getProvinceCode().toUpperCase(Locale.ROOT),
            detail.getPostalCode(),
            detail.getCountryCode().toUpperCase(Locale.ROOT),
            RegExUtils.replaceAll(detail.getBusinessPhoneNumber(), "\\D", StringUtils.EMPTY),
            StringUtils.EMPTY,
            RegExUtils.replaceAll(detail.getSecondaryPhoneNumber(), "\\D", StringUtils.EMPTY),
            RegExUtils.replaceAll(detail.getFaxNumber(), "\\D", StringUtils.EMPTY),
            StringUtils.defaultString(detail.getEmailAddress()),
            "N",
            null,
            "N",
            StringUtils.defaultString(detail.getNotes()),
            user, user,
            ApplicationConstant.ORG_UNIT,
            ApplicationConstant.ORG_UNIT
        );

    return postRequestToLegacy("/api/locations", dto)
        .thenReturn(clientNumber);
  }

  public Mono<String> createContact(ForestClientContactDto dto) {
    return postRequestToLegacy(
        "/api/contacts",
        dto
    )
        .thenReturn(dto.clientNumber());
  }

  public Mono<String> createClient(ForestClientDto dto) {
    return postRequestToLegacy(
        "/api/clients",
        dto
    );
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
            Objects.toString(createdBy, ApplicationConstant.PROCESSOR_USER_NAME),
            Objects.toString(updatedBy, ApplicationConstant.PROCESSOR_USER_NAME),
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
                return Mono.error(new RuntimeException("Failed to submit to " + url));
              }
            });
  }

}
