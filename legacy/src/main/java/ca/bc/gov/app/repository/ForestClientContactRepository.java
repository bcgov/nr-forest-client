package ca.bc.gov.app.repository;

import ca.bc.gov.app.dto.ForestClientContactDetailsDto;
import ca.bc.gov.app.entity.ForestClientContactEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Repository for the ForestClientContactEntity.
 *
 * <p>Provides methods to query the client table in the database. The methods are used to find
 * client by straight field comparison and fuzzy match </p>
 */
@Repository
public interface ForestClientContactRepository
    extends ReactiveCrudRepository<ForestClientContactEntity, String> {

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

  @Query("""
      SELECT
          MAX(C.CLIENT_NUMBER) AS CLIENT_NUMBER,
          MAX(C.CLIENT_CONTACT_ID) AS CONTACT_ID,
          LISTAGG(C.CLIENT_LOCN_CODE, ',') WITHIN GROUP (ORDER BY C.CLIENT_LOCN_CODE) AS LOCATION_CODES_CSV,
          C.CONTACT_NAME,
          MAX(C.BUS_CONTACT_CODE) AS CONTACT_TYPE_CODE,
          MAX(B.DESCRIPTION) AS CONTACT_TYPE_DESC,
          MAX(C.BUSINESS_PHONE) AS BUSINESS_PHONE,
          MAX(C.CELL_PHONE) AS SECONDARY_PHONE,
          MAX(C.FAX_NUMBER) AS FAX_NUMBER,
          MAX(C.EMAIL_ADDRESS) AS EMAIL_ADDRESS
      FROM THE.CLIENT_CONTACT C
          INNER JOIN THE.BUSINESS_CONTACT_CODE B
          ON C.BUS_CONTACT_CODE = B.BUSINESS_CONTACT_CODE
      WHERE CLIENT_NUMBER = :clientNumber
      GROUP BY CONTACT_NAME""")
  Flux<ForestClientContactDetailsDto> findContactsByClientNumber(String clientNumber);

}
