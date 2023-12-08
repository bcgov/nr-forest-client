package ca.bc.gov.app.service;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.exception.MissingRequiredParameterException;
import ca.bc.gov.app.repository.ForestClientRepository;
import ca.bc.gov.app.util.MonoUtil;
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
            .doOnNext(MonoUtil.logContent(log))
            .map(entity ->
                new ForestClientDto(
                    entity.getClientNumber(),
                    entity.getClientName(),
                    entity.getLegalFirstName(),
                    entity.getLegalMiddleName(),
                    entity.getClientStatusCode(),
                    entity.getClientTypeCode(),
                    entity.getClientIdTypeCode(),
                    entity.getClientIdentification(),
                    entity.getRegistryCompanyTypeCode(),
                    entity.getCorpRegnNmbr(),
                    entity.getClientAcronym(),
                    entity.getWcbFirmNumber(),
                    entity.getOcgSupplierNmbr(),
                    entity.getClientComment()
                )
            )
            .doOnNext(MonoUtil.logContent(log));
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
            .doOnNext(MonoUtil.logContent(log))
            .map(entity ->
                new ForestClientDto(
                    entity.getClientNumber(),
                    entity.getClientName(),
                    entity.getLegalFirstName(),
                    entity.getLegalMiddleName(),
                    entity.getClientStatusCode(),
                    entity.getClientTypeCode(),
                    entity.getClientIdTypeCode(),
                    entity.getClientIdentification(),
                    entity.getRegistryCompanyTypeCode(),
                    entity.getCorpRegnNmbr(),
                    entity.getClientAcronym(),
                    entity.getWcbFirmNumber(),
                    entity.getOcgSupplierNmbr(),
                    entity.getClientComment()
                )
            )
            .doOnNext(MonoUtil.logContent(log));
  }

}
