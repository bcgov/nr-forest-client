package ca.bc.gov.app.service;

import ca.bc.gov.app.ApplicationConstants;
import ca.bc.gov.app.dto.ForestClientContactDto;
import ca.bc.gov.app.entity.ForestClientContactEntity;
import ca.bc.gov.app.mappers.ForestClientContactMapper;
import ca.bc.gov.app.repository.ForestClientContactRepository;
import io.micrometer.observation.annotation.Observed;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Slf4j
@Observed
public class ClientContactService {

  private final R2dbcEntityOperations entityTemplate;
  private final ForestClientContactRepository repository;
  private final ForestClientContactMapper mapper;

  public Mono<String> saveAndGetIndex(ForestClientContactDto dto) {
    log.info("Saving forest client contact {} {}", dto.clientNumber(), dto.contactName());
    return
        Mono
            .just(dto)
            .filterWhen(locationDto ->
                locateClientContact(
                    locationDto.clientNumber(),
                    locationDto.clientLocnCode(),
                    locationDto.contactName()
                )
                    .map(forestClientLocation -> false) // means you can't create it
                    .defaultIfEmpty(true) // means you can create it
                    .doOnNext(canCreate ->
                        log.info(
                            "Can create forest client contact {} {}? {}",
                            locationDto.clientNumber(),
                            locationDto.contactName(),
                            canCreate
                        )
                    )
            )
            .doOnNext(forestClientContact ->
                log.info(
                    "Creating forest client contact {} {}",
                    forestClientContact.clientNumber(),
                    forestClientContact.contactName()
                )
            )
            .map(mapper::toEntity)
            .flatMap(entity -> getNextContactId().map(entity::withClientContactId))
            .flatMap(entity -> entityTemplate
                .insert(ForestClientContactEntity.class)
                .using(entity)
            )
            .doOnNext(forestClientContact ->
                log.info(
                    "Saved forest client contact {} {}",
                    forestClientContact.getClientNumber(),
                    forestClientContact.getContactName()
                )
            )
            .map(ForestClientContactEntity::getClientNumber);
  }

  /**
   * Searches for forest client contacts based on the provided search criteria. This method combines
   * the first name and last name into a single search string, and uses this along with the email
   * and phone number to search for matching client contacts. The search is case-insensitive and can
   * partially match the contact information.
   *
   * @param firstName The first name of the client contact to search for.
   * @param lastName  The last name of the client contact to search for.
   * @param email     The email address of the client contact to search for.
   * @param phone     The phone number of the client contact to search for.
   * @return A {@link Flux<ForestClientContactDto>} containing the search results mapped to DTOs.
   */
  public Flux<ForestClientContactDto> search(
      String firstName,
      String lastName,
      String email,
      String phone
  ) {
    log.info("Searching forest client contact {} {} {} {}", firstName, lastName, email, phone);
    return
        repository
            .matchBy(String.join(" ", firstName, lastName), email, phone)
            .doOnNext(forestClientContact ->
                log.info(
                    "Found forest client contact {} {}",
                    forestClientContact.getClientNumber(),
                    forestClientContact.getContactName()
                )
            )
            .map(mapper::toDto);
  }

  /**
   * Locates a client contact based on the provided client number, location code, and contact name.
   * This method performs a query to find any existing client contact entities that match the given
   * criteria. The search criteria include the client's number, the location code, and the contact
   * name (case-insensitive). If a matching client contact is found, it logs the existence of the
   * client location.
   *
   * @param clientNumber The client's unique identifier.
   * @param locationCode The location code associated with the client.
   * @param contactName  The name of the contact, case-insensitive.
   * @return A {@link Flux<ForestClientContactEntity>} containing any found client contacts that
   * match the criteria.
   */
  private Flux<ForestClientContactEntity> locateClientContact(
      String clientNumber,
      String locationCode,
      String contactName
  ) {
    return
        entityTemplate
            .select(
                Query
                    .query(
                        Criteria
                            .where(ApplicationConstants.CLIENT_NUMBER)
                            .is(clientNumber)
                            .and("CLIENT_LOCN_CODE").is(locationCode)
                            .and("CONTACT_NAME").is(contactName.toUpperCase(Locale.ROOT))
                    ),
                ForestClientContactEntity.class
            )
            .doOnNext(forestClientLocation -> log.info(
                    "Forest client location {} {} already exists",
                    forestClientLocation.getClientLocnCode(),
                    forestClientLocation.getContactName()
                )
            );
  }

  /**
   * Retrieves the next available contact ID from the database sequence. This method executes a SQL
   * query to fetch the next value from a sequence named `client_contact_seq`. The sequence is
   * expected to be present in the database schema, and it is used to generate unique identifiers
   * for new client contact entries.
   *
   * @return a {@link Mono} emitting the next available contact ID as {@link Long}. If the query
   * execution fails or the sequence value cannot be retrieved, the returned {@link Mono} will
   * terminate with an error.
   */
  private Mono<Long> getNextContactId() {
    return entityTemplate
        .getDatabaseClient()
        .sql("SELECT THE.client_contact_seq.NEXTVAL FROM dual")
        .fetch()
        .first()
        .map(row -> row.get("NEXTVAL"))
        .map(value -> Long.valueOf(value.toString()));
  }

}
