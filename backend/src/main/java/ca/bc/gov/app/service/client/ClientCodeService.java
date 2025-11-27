package ca.bc.gov.app.service.client;

import static org.springframework.data.relational.core.query.Query.query;

import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.client.IdentificationTypeDto;
import ca.bc.gov.app.entity.client.ClientTypeCodeEntity;
import ca.bc.gov.app.entity.client.ContactTypeCodeEntity;
import ca.bc.gov.app.predicates.QueryPredicates;
import ca.bc.gov.app.repository.client.ClientTypeCodeRepository;
import ca.bc.gov.app.repository.client.ContactTypeCodeRepository;
import ca.bc.gov.app.repository.client.IdentificationTypeCodeRepository;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientCodeService {

  private final ClientTypeCodeRepository clientTypeCodeRepository;
  private final ContactTypeCodeRepository contactTypeCodeRepository;
  private final IdentificationTypeCodeRepository identificationTypeCodeRepository;
  private final R2dbcEntityTemplate template;

  /**
   * <p><b>Find Active Client Type Codes</b></p>
   * <p>List client type code based on it's effective and expiration date.
   * The rule used for it is the expiration date must not be set or should be bigger than provided
   * date and the effective date bust be before or equal to the provided date.</p>
   * <p>The order is by description.</p>
   *
   * @param targetDate The date to be used as reference.
   * @return A list of {@link CodeNameDto}
   */
  public Flux<CodeNameDto> findActiveClientTypeCodes(LocalDate targetDate) {
    log.info("Loading active client type codes for {}", targetDate);
    return
        clientTypeCodeRepository
            .findActiveAt(targetDate)
            .map(entity -> new CodeNameDto(
                    entity.getCode(),
                    entity.getDescription()
                )
            );
  }


  /**
   * Retrieves a client type by its unique code. This method queries the clientTypeCodeRepository to
   * find a client type entity with the specified code. If a matching entity is found, it is
   * converted to a {@code CodeNameDto} object containing the code and description, and wrapped in a
   * Mono. If no matching entity is found, the Mono will complete without emitting any items.
   *
   * @param code The unique code of the client type to retrieve.
   * @return A Mono emitting a {@code CodeNameDto} if a matching client type is found, or an empty
   * result if no match is found.
   * @see CodeNameDto
   */
  public Mono<CodeNameDto> getClientTypeByCode(String code) {
    log.info("Loading client type for {}", code);
    return clientTypeCodeRepository
        .findByCode(code)
        .map(entity -> new CodeNameDto(entity.getCode(),
            entity.getDescription()));
  }

  /**
 * Retrieves a map of client types that are currently active.
 * This method queries the database for client type entities that are effective as of now and not expired.
 * The results are collected into a list and then converted into a map where the keys are the client type codes
 * and the values are the descriptions.
 *
 * @return A Mono emitting a map with client type codes as keys and their descriptions as values.
 */
public Mono<Map<String, String>> getClientTypes() {
  return template
      .select(
          query(
              QueryPredicates.isBefore(LocalDateTime.now(), "effectiveAt")
                  .and(
                      QueryPredicates.isAfter(LocalDateTime.now(), "expiredAt")
                          .or(QueryPredicates.isNull("expiredAt"))
                  )
          ),
          ClientTypeCodeEntity.class
      )
      .collectList()
      // Convert the list into a map using code as the key and description as value
      .map(clientTypeCodeEntities ->
          clientTypeCodeEntities
              .stream()
              .collect(Collectors.toMap(ClientTypeCodeEntity::getCode,
                  ClientTypeCodeEntity::getDescription))
      );
}


  /**
   * <p><b>List contact types</b></p>
   * List contact type codes by page with a defined size.
   *
   * @param page The page number, it is a 0-index base.
   * @param size The amount of entries per page.
   * @return A list of {@link CodeNameDto} entries.
   */
  public Flux<CodeNameDto> listClientContactTypeCodes(LocalDate activeDate, int page, int size) {
    log.info("Loading contact types for page {} with size {}", page, size);
    return contactTypeCodeRepository
        .findActiveAt(activeDate, PageRequest.of(page, size))
        .map(entity -> new CodeNameDto(
            entity.getContactTypeCode(),
            entity.getDescription()));
  }

  /**
   * Retrieves all active identification types as of the specified target date.
   *
   * @param targetDate the date to check for active identification types.
   * @return a Flux stream of IdentificationTypeDto containing the code, description, and country
   * code of each active identification type.
   */
  public Flux<IdentificationTypeDto> getAllActiveIdentificationTypes(LocalDate targetDate) {
    log.info("Loading active identification type codes by {}", targetDate);
    return identificationTypeCodeRepository
        .findActiveAt(targetDate)
        .map(entity -> new IdentificationTypeDto(
            entity.getCode(),
            entity.getDescription(),
            entity.getCountryCode()));
  }

  /**
   * Retrieves an identification type by its code.
   *
   * @param idCode the code of the identification type to retrieve.
   * @return a Mono containing a CodeNameDto with the code and description of the identification
   * type, or an empty Mono if not found.
   */
  public Mono<CodeNameDto> getIdentificationTypeByCode(String idCode) {
    log.info("Loading identification type by {}", idCode);
    return identificationTypeCodeRepository
        .findByCode(idCode)
        .map(entity -> new CodeNameDto(entity.getCode(),
            entity.getDescription()));
  }

  public Mono<Map<String,String>> fetchContactTypesFromList(Set<String> contactTypes) {

    return contactTypeCodeRepository
        .findByContactTypeCodeIn(contactTypes)
        .collectMap(
            ContactTypeCodeEntity::getContactTypeCode,
            ContactTypeCodeEntity::getDescription
        );
  }

}
