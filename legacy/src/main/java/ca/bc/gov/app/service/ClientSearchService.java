package ca.bc.gov.app.service;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.entity.ClientDoingBusinessAsEntity;
import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.exception.MissingRequiredParameterException;
import ca.bc.gov.app.mappers.AbstractForestClientMapper;
import ca.bc.gov.app.repository.ClientDoingBusinessAsRepository;
import ca.bc.gov.app.repository.ForestClientRepository;
import io.micrometer.observation.annotation.Observed;
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
@Observed
public class ClientSearchService {

  private final ForestClientRepository forestClientRepository;
  private final ClientDoingBusinessAsRepository doingBusinessAsRepository;
  private final AbstractForestClientMapper<ForestClientDto, ForestClientEntity> mapper;

  public Flux<ForestClientDto> findByRegistrationNumberOrCompanyName(
      String registrationNumber,
      String companyName
  ) {

    if (StringUtils.isAllBlank(registrationNumber, companyName)) {
      log.error("Missing required parameter to search for registration number or company name");
      throw new MissingRequiredParameterException("registrationNumber or companyName");
    }

    log.info("Searching for registration number: {} or company name: {}", registrationNumber,
        companyName);

    return
        forestClientRepository
            .findClientByIncorporationOrName(registrationNumber, companyName)
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
      log.error("Missing required parameter to search for individual");
      throw new MissingRequiredParameterException("firstName, lastName, or dob");
    }

    log.info("Searching for individual: {} {} {}", firstName, lastName, dob);

    return
        forestClientRepository
            .findByIndividual(firstName, lastName, dob.atStartOfDay())
            .map(mapper::toDto)
            .doOnNext(
                dto -> log.info("Found individual: {} {}", dto.clientNumber(), dto.clientName()));
  }


  public Flux<ForestClientDto> matchBy(String companyName) {
    log.info("Searching for match: {}", companyName);
    return
        forestClientRepository
            .matchBy(companyName)
            .map(mapper::toDto)
            .doOnNext(dto -> log.info("Found match: {} {}", dto.clientNumber(), dto.clientName()));
  }

  public Flux<ForestClientDto> findByIdAndLastName(String clientId, String lastName) {
    log.info("Searching for client: {} {}", clientId, lastName);
    return forestClientRepository
        .findByClientIdentificationIgnoreCaseAndClientNameIgnoreCase(clientId, lastName)
        .map(mapper::toDto)
        .doOnNext(
            dto -> log.info("Found client: {} {}", dto.clientNumber(), dto.clientName())
        );
  }
}
