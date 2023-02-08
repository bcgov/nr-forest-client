package ca.bc.gov.app.service;

import ca.bc.gov.app.ApplicationConstants;
import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.exception.MissingRequiredParameterException;
import ca.bc.gov.app.repository.ForestClientRepository;
import ca.bc.gov.app.util.MonoUtil;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
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
            )
            .doOnNext(MonoUtil.logContent(log));
  }

  public Flux<ForestClientDto> findByNameAndBirth(
      Optional<String> firstName,
      Optional<String> lastName,
      Optional<String> birthDate
  ) {

    //First, we validate to see if it's empty or not, empty values cannot be passed on
    //For requests, when someone is consuming our endpoint and want to send an empty value
    //for example, when looking up for companies, not individuals, it has to provide the
    //query param without any value, such as firstName=&birthdate=&lastName=My Company
    if (lastName.isEmpty()) {
      throw new MissingRequiredParameterException(ApplicationConstants.CLIENT_SEARCH_LAST_NAME);
    }

    if (birthDate.isEmpty()) {
      throw new MissingRequiredParameterException(ApplicationConstants.CLIENT_SEARCH_BIRTHDATE);
    }

    if (firstName.isEmpty()) {
      throw new MissingRequiredParameterException(ApplicationConstants.CLIENT_SEARCH_FIRST_NAME);
    }

    //The search probe is just an example object with the fields populated with the values we
    //are looking for.
    ForestClientEntity searchProbe = new ForestClientEntity()
        .withBirthdate(
            birthDate
                .filter(StringUtils::isNotBlank)
                .map(date -> LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE))
                .orElse(null)
        )
        .withLegalFirstName(firstName.filter(StringUtils::isNotBlank).orElse(null))
        .withClientName(lastName.filter(StringUtils::isNotBlank).orElse(null));

    //The matcher defines how we will look up for the data provided
    ExampleMatcher searchMatcher = ExampleMatcher
        .matchingAll()
        .withMatcher(
            "clientName",
            ExampleMatcher.GenericPropertyMatcher::ignoreCase
        )
        .withMatcher(
            "legalFirstName",
            ExampleMatcher.GenericPropertyMatcher::ignoreCase
        )
        .withIncludeNullValues();

    return forestClientRepository
        //Then we look up for all that matches the example
        .findAll(Example.of(searchProbe, searchMatcher))
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
