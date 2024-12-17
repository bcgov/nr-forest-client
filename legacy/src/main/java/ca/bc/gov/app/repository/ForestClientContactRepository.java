package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.ForestClientContactEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;

@Repository
public interface ForestClientContactRepository
    extends ReactiveCrudRepository<ForestClientContactEntity,String> {

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

  Flux<ForestClientContactEntity> findAllByClientNumber(String clientNumber);

}
