package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.ClientUpdateReasonEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ClientUpdateReasonRepository extends
    ReactiveCrudRepository<ClientUpdateReasonEntity, Long> {

  @Query("""
      SELECT
        cur.*
      FROM CLIENT_UPDATE_REASON cur
      LEFT JOIN FOR_CLI_AUDIT fca ON fca.FOREST_CLIENT_AUDIT_ID = cur.FOREST_CLIENT_AUDIT_ID
      WHERE
        fca.CLIENT_NUMBER = :number
        AND cur.CLIENT_UPDATE_REASON_CODE = 'UND'
        AND cur.CLIENT_UPDATE_ACTION_CODE = :action"""
  )
  Mono<ClientUpdateReasonEntity> findUndefinedByNumberWithFilteredActions(String number, String action);

  @Query("""
      SELECT
        cur.*
      FROM CLIENT_UPDATE_REASON cur
      LEFT JOIN FOR_CLI_AUDIT fca ON fca.FOREST_CLIENT_AUDIT_ID = cur.FOREST_CLIENT_AUDIT_ID
      WHERE
        fca.CLIENT_NUMBER = :number
        AND cur.CLIENT_UPDATE_REASON_CODE = 'UND'
        AND cur.CLIENT_UPDATE_ACTION_CODE NOT IN ('ID', 'NAME')"""
  )
  Mono<ClientUpdateReasonEntity> findUndefinedByNumberWithFilteredActions(String number);
}
