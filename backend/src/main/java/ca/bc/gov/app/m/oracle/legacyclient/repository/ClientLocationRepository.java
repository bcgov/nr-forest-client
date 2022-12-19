package ca.bc.gov.app.m.oracle.legacyclient.repository;

import ca.bc.gov.app.repository.CoreRepository;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ClientLocationEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientLocationRepository extends CoreRepository<ClientLocationEntity> {
  @Query("select x from ClientLocationEntity x " +
      "where x.clientNumber = :clientNumber")
  ClientLocationEntity findByClientNumber(@Param("clientNumber") String clientNumber);
}