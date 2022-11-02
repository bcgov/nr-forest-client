package ca.bc.gov.api.m.oracle.legacyclient.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ca.bc.gov.api.core.repository.CoreRepository;
import ca.bc.gov.api.m.oracle.legacyclient.entity.ClientLocationEntity;

@Repository
public interface ClientLocationRepository extends CoreRepository<ClientLocationEntity> {
    @Query("select x from ClientLocationEntity x " +
	       "where x.clientNumber = :clientNumber")
	  ClientLocationEntity findByClientNumber(@Param("clientNumber") String clientNumber);
}
