package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.ForestClientLocationEntity;
import ca.bc.gov.app.entity.ForestClientLocationEntityId;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ForestClientLocationRepository
    extends ReactiveCrudRepository<ForestClientLocationEntity, ForestClientLocationEntityId> {

  @Query("""
      select *
      from the.client_location
      where
      (
          upper(address_1) like upper(concat(:address, '%'))
          or upper(address_2) like upper(concat(:address, '%'))
          or upper(address_3) like upper(concat(:address, '%'))
      )
      and upper(postal_code) = upper(replace(:postalCode, ' ', ''))""")
  Flux<ForestClientLocationEntity> matchBy(String address, String postalCode);

}
