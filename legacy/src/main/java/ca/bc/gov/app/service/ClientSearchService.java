package ca.bc.gov.app.service;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.repository.ForestClientRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    return
        forestClientRepository
            .findClientByIncorporationOrName(incorporationNumber, companyName)
            .map(entity ->
                new ForestClientDto(
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
                    entity.getClientComment()
                )
            );
  }

  public Flux<ForestClientDto> findByNameAndBirth(
      String firstName,
      String lastName,
      String birthDate
  ) {
    return forestClientRepository
        .findClientByNameAndBirthdate(
            firstName,
            lastName,
            LocalDate.parse(birthDate, DateTimeFormatter.BASIC_ISO_DATE)
        )
        .map(entity ->
            new ForestClientDto(
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
                entity.getClientComment()
            )
        );
  }

}
