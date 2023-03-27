package ca.bc.gov.app.service;

import ca.bc.gov.app.dto.ForestClientContactDto;
import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.dto.ForestClientLocationDto;
import ca.bc.gov.app.entity.ForestClientContactEntity;
import ca.bc.gov.app.entity.ForestClientDoingBusinessAsEntity;
import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.entity.ForestClientLocationEntity;
import ca.bc.gov.app.repository.ForestClientContactRepository;
import ca.bc.gov.app.repository.ForestClientDoingBusinessAsRepository;
import ca.bc.gov.app.repository.ForestClientLocationRepository;
import ca.bc.gov.app.repository.ForestClientRepository;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ClientService {

  private final ForestClientRepository forestClientRepository;
  private final ForestClientDoingBusinessAsRepository doingBusinessAsRepository;
  private final ForestClientLocationRepository locationRepository;
  private final ForestClientContactRepository contactRepository;

  public Flux<ForestClientDto> listClientData(
      String clientNumber,
      Integer page,
      Integer size
  ) {

    Flux<ForestClientEntity> resultList =
        StringUtils.isNotBlank(clientNumber) ?
            forestClientRepository.findByClientNumber(clientNumber) :
            forestClientRepository.findBy(PageRequest.of(page, size, Sort.by("clientName")));

    return
        resultList
            .map(mapToForestClientDto())
            .flatMap(dto -> loadDoingBusinessAs(dto).switchIfEmpty(Mono.just(dto)))
            .flatMap(dto ->
                locationRepository
                    .findByClientNumber(dto.clientNumber())
                    .flatMap(locationEntity ->
                        contactRepository
                            .findByClientNumberAndClientLocnCode(
                                dto.clientNumber(),
                                locationEntity.getClientLocnCode()
                            )
                            .map(mapToForestClientContactDto())
                            .collectList()
                            .switchIfEmpty(Mono.just(List.of()))
                            .map(mapToForestClientLocationDto(locationEntity))
                    )
                    .collectList()
                    .map(dto::withLocations)
                    .switchIfEmpty(Mono.just(dto))
            )
            .sort(Comparator.comparing(ForestClientDto::clientName));
  }

  private Function<List<ForestClientContactDto>, ForestClientLocationDto> mapToForestClientLocationDto(
      ForestClientLocationEntity locationEntity) {
    return contactList -> new ForestClientLocationDto(
        locationEntity.getClientNumber(),
        locationEntity.getClientLocnCode(),
        locationEntity.getClientLocnName(),
        locationEntity.getHdbsCompanyCode(),
        locationEntity.getAddressOne(),
        locationEntity.getAddressTwo(),
        locationEntity.getAddressThree(),
        locationEntity.getCity(),
        locationEntity.getProvince(),
        locationEntity.getPostalCode(),
        locationEntity.getCountry(),
        locationEntity.getBusinessPhone(),
        locationEntity.getHomePhone(),
        locationEntity.getCellPhone(),
        locationEntity.getFaxNumber(),
        locationEntity.getEmailAddress(),
        contactList
    );
  }

  private Function<ForestClientContactEntity, ForestClientContactDto> mapToForestClientContactDto() {
    return contactEntity -> new ForestClientContactDto(
        contactEntity.getContactId(),
        contactEntity.getClientNumber(),
        contactEntity.getClientLocnCode(),
        contactEntity.getContactCode(),
        contactEntity.getName(),
        contactEntity.getBusinessPhone(),
        contactEntity.getCellPhone(),
        contactEntity.getFaxNumber(),
        contactEntity.getEmailAddress()
    );
  }

  private Mono<ForestClientDto> loadDoingBusinessAs(ForestClientDto dto) {
    return
        doingBusinessAsRepository
            .findByClientNumber(dto.clientNumber())
            .map(ForestClientDoingBusinessAsEntity::getDoingBusinessAsName)
            .collectList()
            .switchIfEmpty(Mono.just(List.of()))
            .map(dto::withKnowAs);
  }

  private Function<ForestClientEntity, ForestClientDto> mapToForestClientDto() {
    return entity -> new ForestClientDto(
        entity.getClientNumber(),
        entity.getClientName(),
        entity.getLegalFirstName(),
        entity.getLegalMiddleName(),
        entity.getClientStatusCode(),
        entity.getClientTypeCode(),
        entity.getBirthdate(),
        entity.getClientIdTypeCode(),
        entity.getClientIdentification(),
        entity.getRegistryCompanyTypeCode(),
        entity.getCorpRegnNmbr(),
        entity.getClientAcronym(),
        entity.getWcbFirmNumber(),
        entity.getOcgSupplierNmbr(),
        entity.getClientComment(),
        List.of(),
        List.of()
    );
  }

}
