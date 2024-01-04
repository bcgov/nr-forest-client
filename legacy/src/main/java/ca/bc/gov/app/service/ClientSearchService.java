package ca.bc.gov.app.service;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.exception.MissingRequiredParameterException;
import ca.bc.gov.app.mappers.AbstractForestClientMapper;
import ca.bc.gov.app.repository.ForestClientRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientSearchService {

  private final ForestClientRepository forestClientRepository;
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
            .map(mapper::toDto)
            .doOnNext(dto -> log.info("Found client: {} {}", dto.clientNumber(), dto.clientName()));
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
