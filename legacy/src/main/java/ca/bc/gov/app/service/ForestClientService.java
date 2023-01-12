package ca.bc.gov.app.service;

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
                            //TODO: This need to be validated
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
      ForestClientEntity entity) {
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
            .flatMap(response ->
                clientLocationRepository
                    .findById(currentEntity.getClientNumber())
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
            .onErrorResume(t -> getEmptyBandValidation(currentEntity)
            );
  }

}
