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

}
