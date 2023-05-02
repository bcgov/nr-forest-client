package ca.bc.gov.app.repository.legacy;

import ca.bc.gov.app.entity.legacy.ForestClientEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ForestClientRepository extends ReactiveCrudRepository<ForestClientEntity, String>,
    ReactiveQueryByExampleExecutor<ForestClientEntity>,
    ReactiveSortingRepository<ForestClientEntity, String> {

  @Query("""
      SELECT *
      FROM FOREST_CLIENT
      WHERE
        UTL_MATCH.EDIT_DISTANCE(UPPER(CLIENT_NAME),UPPER(:companyName)) <=2 OR
        UPPER(CLIENT_NAME) LIKE '%' || UPPER(:companyName) || '%'""")
  Flux<ForestClientEntity> matchBy(String companyName);

}
