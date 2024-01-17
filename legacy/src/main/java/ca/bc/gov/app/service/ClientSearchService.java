package ca.bc.gov.app.service;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.entity.ClientDoingBusinessAsEntity;
import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.exception.MissingRequiredParameterException;
import ca.bc.gov.app.mappers.AbstractForestClientMapper;
import ca.bc.gov.app.repository.ClientDoingBusinessAsRepository;
import ca.bc.gov.app.repository.ForestClientRepository;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientSearchService {

  private final ForestClientRepository forestClientRepository;
  private final ClientDoingBusinessAsRepository doingBusinessAsRepository;
  private final AbstractForestClientMapper<ForestClientDto, ForestClientEntity> mapper;

  public Flux<ForestClientDto> findByIncorporationOrName(
      String incorporationNumber,
      String companyName
  ) {

    if (StringUtils.isAllBlank(incorporationNumber, companyName)) {
      throw new MissingRequiredParameterException("incorporationNumber or companyName");
    }

    return
        forestClientRepository
            .findClientByIncorporationOrName(incorporationNumber, companyName)
            .doOnNext(
                dto -> log.info("Found client: {} {}", dto.getClientNumber(), dto.getClientName()))
            .switchIfEmpty(
                Flux
                    .from(Mono.justOrEmpty(Optional.ofNullable(companyName)))
                    .filter(StringUtils::isNotBlank)
                    .flatMap(name ->
                        doingBusinessAsRepository
                            .findByDoingBusinessAsName(name.toUpperCase())
                            .doOnNext(dto -> log.info("Found client doing business as: {} {}",
                                dto.getClientNumber(), dto.getDoingBusinessAsName()))
                            .map(ClientDoingBusinessAsEntity::getClientNumber)
                            .flatMap(forestClientRepository::findByClientNumber)
                    )
            )
            .map(mapper::toDto);
  }

  public Flux<ForestClientDto> findByIndividual(
      String firstName,
      String lastName,
      LocalDate dob
  ) {

    if (StringUtils.isAnyBlank(firstName, lastName) || dob == null) {
      throw new MissingRequiredParameterException("firstName, lastName, or dob");
    }

    return
        forestClientRepository
            .findByIndividual(firstName, lastName, dob.atStartOfDay())
            .map(mapper::toDto)
            .doOnNext(
                dto -> log.info("Found individual: {} {}", dto.clientNumber(), dto.clientName()));
  }


  public Flux<ForestClientDto> matchBy(String companyName) {
    return
        forestClientRepository
            .matchBy(companyName)
            .map(mapper::toDto)
            .doOnNext(dto -> log.info("Found match: {} {}", dto.clientNumber(), dto.clientName()));
  }
}
