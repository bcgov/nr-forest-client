package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.ForestClientEntity;
import java.time.LocalDate;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ForestClientRepository extends ReactiveCrudRepository<ForestClientEntity, String> {

  @Query("""
      select * from FOREST_CLIENT x
       where x.CLIENT_TYPE_CODE = 'B'
       and x.CLIENT_STATUS_CODE = 'ACT'""")
  Flux<ForestClientEntity> findAllFirstNationBandClients();

  @Query("""
      select * from FOREST_CLIENT x
       where (x.REGISTRY_COMPANY_TYPE_CODE || x.CORP_REGN_NMBR) = :incorporationNumber
       or x.CLIENT_NAME = :companyName""")
  Flux<ForestClientEntity> findClientByIncorporationOrName(
      @Param("incorporationNumber") String incorporationNumber,
      @Param("companyName") String companyName
  );

  @Query("""
      select * from FOREST_CLIENT x
       where lower(x.LEGAL_FIRST_NAME) = lower(:firstName)
       and lower(x.CLIENT_NAME) = lower(:lastName)
       and x.BIRTHDATE = :birthdate""")
  Flux<ForestClientEntity> findClientByNameAndBirthdate(
      @Param("firstName") String firstName,
      @Param("lastName") String lastName,
      @Param("birthdate") LocalDate birthdate
  );
}
