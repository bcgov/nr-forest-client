package ca.bc.gov.app.service.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.bcregistry.BcRegistryAddressDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryFacetSearchResultEntryDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryPartyDto;
import ca.bc.gov.app.dto.bcregistry.ClientDetailsDto;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientLookUpDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.EmailRequestDto;
import ca.bc.gov.app.dto.client.LegalTypeEnum;
import ca.bc.gov.app.dto.legacy.ForestClientDetailsDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.exception.ClientAlreadyExistException;
import ca.bc.gov.app.exception.InvalidAccessTokenException;
import ca.bc.gov.app.exception.NoClientDataFound;
import ca.bc.gov.app.exception.UnableToProcessRequestException;
import ca.bc.gov.app.exception.UnexpectedErrorException;
import ca.bc.gov.app.exception.UnsupportedClientTypeException;
import ca.bc.gov.app.exception.UnsupportedLegalTypeException;
import ca.bc.gov.app.service.bcregistry.BcRegistryService;
import ca.bc.gov.app.service.ches.ChesService;
import ca.bc.gov.app.util.ClientValidationUtils;
import io.micrometer.observation.annotation.Observed;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientService {

  private final ClientCountryProvinceService countryProvinceService;
  private final BcRegistryService bcRegistryService;
  private final ChesService chesService;
  private final ClientLegacyService legacyService;
  private final Predicate<BcRegistryAddressDto> isMultiAddressEnabled;


  /**
   * Retrieves the client details for a given client number by making calls to BC Registry service.
   * The details include the company standing and addresses.
   *
   * @param clientNumber the client number for which to retrieve details
   * @return a Mono that emits a ClientDetailsDto object representing the details of the client
   */
  public Mono<ClientDetailsDto> getClientDetailsByIncorporationNumber(
      String clientNumber,
      String userId,
      String businessId,
      String provider
  ) {
    log.info("Loading details for {}", clientNumber);
    return bcRegistryService
        .requestDocumentData(clientNumber)
        .next()
        .doOnNext(document ->
            log.info("Searching on Oracle legacy db for {} {}",
                document.business().identifier(),
                document.business().getResolvedLegalName()
            )
        )
        .flatMap(document -> legacyService
            .searchLegacy(
                document.business().identifier(),
                document.business().getResolvedLegalName(),
                userId,
                businessId
            )
            .next()
            .filter(isMatchWith(document))
            .doOnNext(legacy ->
                log.info("Found legacy entry for {} {}",
                    document.business().identifier(),
                    document.business().getResolvedLegalName()
                )
            )
            .flatMap(legacy -> Mono.error(
                new ClientAlreadyExistException(
                    legacy.clientNumber(),
                    document.business().identifier(),
                    document.business().getResolvedLegalName())
            ))
            .defaultIfEmpty(document)
            .doOnNext(value ->
                log.info("No entry found on legacy for {} {}",
                    document.business().identifier(),
                    document.business().getResolvedLegalName()
                )
            )
        )
        .map(BcRegistryDocumentDto.class::cast)

        .flatMap(client -> {
          // Check for unsupported legal type
          LegalTypeEnum legalType = LegalTypeEnum.fromValue(client.business().legalType());
          if (legalType == null) {
            return Mono.error(
                new UnsupportedLegalTypeException(client.business().legalType())
            );
          }

          // FSADT1-1388: Allow IDIR users to search for any client type
          if (provider.equalsIgnoreCase("idir")) {
            return Mono.just(client);
          }

          if (ApplicationConstant.AVAILABLE_CLIENT_TYPES.contains(
              ClientValidationUtils.getClientType(legalType).toString()
          )) {
            return Mono.just(client);
          }

          return Mono.error(new UnsupportedClientTypeException(
              ClientValidationUtils.getClientType(legalType).toString()
          ));
        })

        // If document type is SP and party contains only one entry that is not a person, fail
        .filter(document -> provider.equalsIgnoreCase("idir")
                            || !("SP".equalsIgnoreCase(document.business().legalType())
                                 && document.parties().size() == 1
                                 && !document.parties().get(0).isPerson())
        )
        .flatMap(buildDetails())
        .switchIfEmpty(Mono.error(new UnableToProcessRequestException(
            "Unable to process request. This sole proprietor is not owned by a person"
        )));
  }

  public Mono<ForestClientDetailsDto> getClientDetailsByClientNumber(String clientNumber) {
    return legacyService
        .searchByClientNumber(clientNumber)
        .flatMap(details ->
            legacyService
                .getRelatedClientList(clientNumber)
                .map(details::withRelatedClients)
                .defaultIfEmpty(details)
            );
  }
  
  public Mono<String> getGoodStandingIndicator(String clientNumber) {
    return legacyService
        .searchByClientNumber(clientNumber)
        .filter(dto ->
            StringUtils.isNotBlank(dto.client().registryCompanyTypeCode())
            && StringUtils.isNotBlank(dto.client().corpRegnNmbr())
        )
        .flatMap(dto ->
            bcRegistryService
                .requestDocumentData(
                    dto.client().registryCompanyTypeCode()
                        + dto.client().corpRegnNmbr()
                )
                .next()
                .map(document -> {
                  Boolean goodStanding = document.business().goodStanding();
                  return BooleanUtils.toString(goodStanding, "Y", "N", StringUtils.EMPTY);
                })
        )
        .onErrorResume(NoClientDataFound.class, ex -> {
          log.error("No data found on BC Registry for client number: {}", clientNumber);
          return Mono.just(StringUtils.EMPTY);
        })
        .onErrorResume(UnexpectedErrorException.class, ex -> {
          log.error("Unexpected error occurred while fetching data for client number: {}", clientNumber);
          return Mono.just(StringUtils.EMPTY);
        })
        .switchIfEmpty(Mono.just(StringUtils.EMPTY));
  }

  /**
   * Searches the BC Registry API for {@link BcRegistryFacetSearchResultEntryDto} instances matching
   * the given value and maps them to {@link ClientLookUpDto} instances.
   *
   * @param name the client name to search for
   * @param incorporation the incorporation number or registration number to search for
   * @return a {@link Flux} of {@link ClientLookUpDto} instances representing matching BC Registry
   * entries
   * @throws NoClientDataFound           if no matching data is found
   * @throws InvalidAccessTokenException if the access token is invalid or expired
   */
  public Flux<ClientLookUpDto> findByClientNameOrIncorporation(String name, String incorporation) {
    log.info("Searching for {}", name);
    return bcRegistryService
        .searchByFacets(name, incorporation)
        .map(entry -> new ClientLookUpDto(
                entry.identifier(),
                entry.name(),
                entry.status(),
                entry.legalType()
            )
        )
        .sort(Comparator.comparing(ClientLookUpDto::name));
  }

  public Mono<Void> findByIndividual(String userId, String lastName) {
    return legacyService
        .searchIdAndLastName(userId, lastName)
        .doOnNext(legacy -> log.info("Found legacy entry for {} {}", userId, lastName))
        //If we have result, we return a Mono.error with the exception, 
        //otherwise return a Mono.empty
        .next()
        .flatMap(legacy -> Mono
            .error(new ClientAlreadyExistException(legacy.clientNumber()))
        );
  }

  /**
   * <p><b>Send Email</b></p>
   * <p>Send email to a client.</p>
   *
   * @param emailRequestDto The request data containing client details.
   * @return A {@link Mono} of {@link String}.
   */
  public Mono<String> sendEmail(EmailRequestDto emailRequestDto) {
    return chesService.sendEmail(
        emailRequestDto.templateName(),
        emailRequestDto.emailsCsv(),
        emailRequestDto.subject(),
        emailRequestDto.variables(),
        null);
  }

  /**
   * <p><b>Send Email</b></p>
   * <p>Send email to the client when entry already exists.</p>
   *
   * @param emailRequestDto The request data containing user and client details.
   * @return A {@link Mono} of {@link Void}.
   */
  public Mono<Void> triggerEmailDuplicatedClient(
      EmailRequestDto emailRequestDto,
      String userId,
      String businessId
  ) {
    return
        legacyService
            .searchLegacy(
                emailRequestDto.registrationNumber(),
                emailRequestDto.name(),
                userId,
                businessId
            )
            .next()
            .flatMap(
                triggerEmailDuplicatedClient(
                    emailRequestDto.emailsCsv(), 
                    emailRequestDto.userName())
                )
            .then();
  }

  private Function<BcRegistryDocumentDto, Mono<ClientDetailsDto>> buildDetails() {
    return document ->
        buildAddress(
            document,
            buildSimpleClientDetails(document.business(), document.isOwnedByPerson())
        );
  }

  private ClientDetailsDto buildSimpleClientDetails(
      BcRegistryBusinessDto businessDto,
      boolean isOwnedByPerson
  ) {

    if (businessDto == null) {
      return new ClientDetailsDto(
          "",
          "",
          false,
          "",
          List.of(),
          List.of(),
          isOwnedByPerson
      );
    }
    log.info("Building simple client details for {} with standing {}", businessDto.identifier(),
        businessDto.goodStanding());
    return
        new ClientDetailsDto(
            businessDto.getResolvedLegalName(),
            businessDto.identifier(),
            businessDto.goodStanding(),
            businessDto.legalType(),
            List.of(),
            List.of(),
            isOwnedByPerson
        );
  }

  private Mono<ClientDetailsDto> buildAddress(
      BcRegistryDocumentDto document,
      ClientDetailsDto clientDetails
  ) {
    AtomicInteger index = new AtomicInteger(0);

    return
        Flux.fromIterable(
                document
                    .offices()
                    .addresses()
            )
            .filter(BcRegistryAddressDto::isValid)
            .filter(isMultiAddressEnabled)
            .map(addressDto ->
                new ClientAddressDto(
                    addressDto.streetAddress(),
                    null,
                    null,
                    new ClientValueTextDto("", addressDto.addressCountry()),
                    new ClientValueTextDto(addressDto.addressRegion(), ""),
                    addressDto.addressCity(),
                    Optional
                        .ofNullable(addressDto.postalCode())
                        .map(String::trim)
                        .map(value -> value.replaceAll("\\s+", ""))
                        .orElse(StringUtils.EMPTY),
                    null,
                    null,
                    null,
                    null,
                    null,
                    index.getAndIncrement(),
                    (addressDto.addressType() != null ? addressDto.addressType() : "").concat(
                        " address").toUpperCase(Locale.ROOT)
                )
            )
            .flatMap(address -> countryProvinceService.loadCountry(address.country().text())
                .map(address::withCountry))
            .flatMap(address ->
                countryProvinceService.
                    loadProvince(address.country().value(), address.province().value())
                    .map(address::withProvince)
            )
            .sort(Comparator.comparing(ClientAddressDto::locationName).reversed())
            .collectList()
            .defaultIfEmpty(new ArrayList<>())
            .flatMap(addresses ->
                Flux.fromIterable(document.parties())
                    .filter(party ->
                        !"SP".equalsIgnoreCase(document.business().legalType()) || party.isPerson())
                    .map(party ->
                        new ClientContactDto(
                            null,
                            party.officer().firstName(),
                            party.officer().lastName(),
                            "",
                            "",
                            "",
                            party.officer().email(),
                            index.getAndIncrement(),
                            matchAddress(addresses, party)
                        )
                    )
                    .collectList()
                    .defaultIfEmpty(new ArrayList<>())
                    .map(contacts -> clientDetails
                        .withAddresses(addresses)
                        .withContacts(contacts)
                    )
            );
  }

  private static List<ClientValueTextDto> matchAddress(List<ClientAddressDto> addresses,
      BcRegistryPartyDto party) {
    return addresses
        .stream()
        .filter(address ->
            party.isMatch(
                address.city(),
                address.country().text(),
                address.province().value(),
                address.postalCode(),
                address.streetAddress()
            )
        )
        .map(address -> new ClientValueTextDto(
                String.valueOf(address.index()),
                address.locationName()
            )
        )
        .toList();
  }

  private Predicate<ForestClientDto> isMatchWith(BcRegistryDocumentDto document) {
    return legacy ->
        Objects.equals(
            StringUtils.defaultString(legacy.registryCompanyTypeCode()) +
            StringUtils.defaultString(legacy.corpRegnNmbr()),
            document.business().identifier()
        ) &&
        Objects.equals(
            document.business().legalName(),
            legacy.legalName()
        );
  }

  private Function<ForestClientDto, Mono<ForestClientDto>> triggerEmailDuplicatedClient(
      String email, String userName) {

    return legacy -> chesService.sendEmail(
            "matched",
            email,
            "Client number application can’t go ahead",
            legacy.description(userName),
            null)
        .thenReturn(legacy);
  }

}