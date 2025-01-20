package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.ForestClientContactEntity;
import java.util.Set;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository for the ForestClientContactEntity.
 *
 * <p>Provides methods to query the client table in the database. The methods are used to find
 * client by straight field comparison and fuzzy match </p>
 */
@Repository
public interface ForestClientContactRepository
    extends ReactiveCrudRepository<ForestClientContactEntity, Long> {

  /**
   * Finds client contacts by matching the contact name, business phone, or email address.
   *
   * @param contactName   the contact name to match
   * @param email         the email address to match
   * @param businessPhone the business phone number to match
   * @return a Flux containing the matching ForestClientContactEntity objects
   */
  @Query("""
      SELECT *
      FROM THE.CLIENT_CONTACT
      WHERE
      UTL_MATCH.JARO_WINKLER_SIMILARITY(UPPER(CONTACT_NAME),UPPER(:contactName)) >= 95
      OR UPPER(BUSINESS_PHONE) = UPPER(:businessPhone)
      OR UPPER(EMAIL_ADDRESS) = UPPER(:email)""")
  Flux<ForestClientContactEntity> matchBy(
      String contactName,
      String email,
      String businessPhone
  );

  /**
   * Finds client contacts by matching the contact name, email address, and any of the provided
   * phone numbers.
   *
   * @param contactName   the contact name to match
   * @param email         the email address to match
   * @param businessPhone the business phone number to match
   * @param cellPhone     the cell phone number to match
   * @param fax           the fax number to match
   * @return a Flux containing the matching ForestClientContactEntity objects
   */
  @Query("""
      SELECT *
      FROM THE.CLIENT_CONTACT
      WHERE
      UTL_MATCH.JARO_WINKLER_SIMILARITY(UPPER(CONTACT_NAME),UPPER(:contactName)) >= 95
      AND (
        UPPER(BUSINESS_PHONE) = UPPER(:businessPhone)
        OR UPPER(CELL_PHONE) = UPPER(:businessPhone)
        OR UPPER(FAX_NUMBER) = UPPER(:businessPhone)
        OR UPPER(BUSINESS_PHONE) = UPPER(:cellPhone)
        OR UPPER(CELL_PHONE) = UPPER(:cellPhone)
        OR UPPER(FAX_NUMBER) = UPPER(:cellPhone)
        OR UPPER(BUSINESS_PHONE) = UPPER(:fax)
        OR UPPER(CELL_PHONE) = UPPER(:fax)
        OR UPPER(FAX_NUMBER) = UPPER(:fax)
      )
      AND UPPER(EMAIL_ADDRESS) = UPPER(:email)""")
  Flux<ForestClientContactEntity> matchByExpanded(
      String contactName,
      String email,
      String businessPhone,
      String cellPhone,
      String fax
  );

  /**
   * Finds all client contacts by client number.
   *
   * @param clientNumber the client number to match
   * @return a Flux containing the matching ForestClientContactEntity objects
   */
  Flux<ForestClientContactEntity> findAllByClientNumber(String clientNumber);

  Flux<ForestClientContactEntity> findAllByClientNumberAndContactName(String clientNumber,
      String contactName);

  Mono<Void> deleteByClientNumberAndAndContactNameAndClientLocnCodeIn(String clientNumber,
      String contactName, Set<String> locationCode);


}
