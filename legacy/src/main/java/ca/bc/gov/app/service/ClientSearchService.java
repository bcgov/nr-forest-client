package ca.bc.gov.app.service;

import static ca.bc.gov.app.util.LogUtil.logContent;

import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.exception.MissingRequiredParameterException;
import ca.bc.gov.app.repository.legacy.ForestClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
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
            .doOnNext(logContent(log, Level.INFO))
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
            .doOnNext(logContent(log, Level.INFO));
  }

}
