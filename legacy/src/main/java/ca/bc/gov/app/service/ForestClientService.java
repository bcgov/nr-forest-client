package ca.bc.gov.app.service;

import static java.util.function.Predicate.not;

import ca.bc.gov.app.dto.ClientPublicViewDto;
import ca.bc.gov.app.dto.FirstNationBandVidationDto;
import ca.bc.gov.app.dto.OrgBookResultListResponse;
import ca.bc.gov.app.dto.PropertyDto;
import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.repository.ClientDoingBusinessAsRepository;
import ca.bc.gov.app.repository.ClientLocationRepository;
import ca.bc.gov.app.repository.ForestClientRepository;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class ForestClientService {

  private final ForestClientRepository forestClientRepository;
  private final ClientLocationRepository clientLocationRepository;
  private final ClientDoingBusinessAsRepository clientDoingBusinessAsRepository;

  private final WebClient forestClientApi;

  public Flux<FirstNationBandVidationDto> getFirstNationBandInfo() {
    return
        forestClientRepository
            .findAllFirstNationBandClients()
            .flatMap(entity ->
                Mono
                    .just(entity)
                    .doOnNext(
                        current -> log.info("Checking data for {} CorpRegnNmbr {}",
                            current.getClientName(),
                            current.getCorpRegnNmbr())
                    )
                    .filter(current -> StringUtils.isNotBlank(current.getCorpRegnNmbr()))
                    .flatMap(getBandInfoFromMap())
                    .switchIfEmpty(getEmptyBandValidation(entity))
            );
  }

  public Flux<ClientPublicViewDto> getClientDoingBusiness() {
    return
        clientDoingBusinessAsRepository
            .findAll()
            .flatMap(entity ->
                findByClientName(entity.getDoingBusinessAsName())
                    .filter(not(OrgBookResultListResponse::empty))
                    .map(orgBookResponse ->
                        new ClientPublicViewDto(
                            entity.getClientNumber(),
                            null,
                            entity.getDoingBusinessAsName(),
                            null,
                            null,
                            null,
                            null,
                            orgBookResponse.results().get(0).value()
                        )
                    )
                    .switchIfEmpty(
                        Mono
                            .just(
                                new ClientPublicViewDto(
                                    entity.getClientNumber(),
                                    null,
                                    entity.getDoingBusinessAsName(),
                                    null,
                                    null,
                                    null,
                                    null,
                                    null
                                )
                            )
                    )
            );
  }

  public Flux<ClientPublicViewDto> getUnregisteredCompanies() {
    return
        forestClientRepository
            .findAll()
            .flatMap(entity ->
                findByClientName(entity.getClientName())
                    .filter(not(OrgBookResultListResponse::empty))
                    .map(orgBookResponse ->
                        new ClientPublicViewDto(
                            entity.getClientNumber(),
                            StringUtils
                                .equals(
                                    orgBookResponse.results().get(0).value(),
                                    entity.getClientName()
                                ) ? orgBookResponse.results().get(0).topicSourceId() : null,
                            entity.getClientName(),
                            null,
                            null,
                            null,
                            null,
                            orgBookResponse.results().get(0).value()
                        )
                    )
            );
  }

  private Mono<OrgBookResultListResponse> findByClientName(String clientName) {
    return
        forestClientApi
            .get()
            .uri("/api/orgbook/name/{name}", clientName)
            .exchangeToMono(response -> response.bodyToMono(OrgBookResultListResponse.class));
  }

  private static Mono<FirstNationBandVidationDto> getEmptyBandValidation(
      ForestClientEntity entity
  ) {
    return Mono.just(new FirstNationBandVidationDto(
            entity.getClientNumber(),
            entity.getCorpRegnNmbr(),
            entity.getClientName(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
    );
  }

  private Function<ForestClientEntity, Mono<FirstNationBandVidationDto>> getBandInfoFromMap() {
    return currentEntity ->
        forestClientApi
            .get()
            .uri("/api/maps/firstNation/{id}", currentEntity.getCorpRegnNmbr())
            .accept(MediaType.APPLICATION_JSON)
            .exchangeToMono(response -> response.bodyToMono(PropertyDto.class))
            .doOnNext(response ->
                log.info(
                    """
                        Information about first nation band with id {} and client number {} found.
                        Looking up for location details from band {}
                        """,
                    currentEntity.getCorpRegnNmbr(),
                    currentEntity.getClientNumber(),
                    response
                )
            )
            .flatMap(response ->
                clientLocationRepository
                    .findById(currentEntity.getClientNumber())
                    .doOnNext(clientLocation -> log.info("Location found as {}", clientLocation))
                    .map(clientLocation ->
                        new FirstNationBandVidationDto(
                            currentEntity.getClientNumber(),
                            currentEntity.getCorpRegnNmbr(),
                            currentEntity.getClientName(),
                            response.firstNationFederalName(),
                            clientLocation.getAddressOne(),
                            response.addressLine1(),
                            clientLocation.getAddressTwo(),
                            response.addressLine2(),
                            clientLocation.getCity(),
                            response.officeCity(),
                            clientLocation.getProvince(),
                            response.officeProvince(),
                            clientLocation.getPostalCode(),
                            response.officePostalCode()
                        )
                    )
            )
            .doOnNext(bandValidation -> log.info("Band validation completed for {} addr mt{} nm mt {}",bandValidation,bandValidation.addressMatch(),bandValidation.nameMatch()))
            .onErrorResume(t -> getEmptyBandValidation(currentEntity)
            );
  }

}
