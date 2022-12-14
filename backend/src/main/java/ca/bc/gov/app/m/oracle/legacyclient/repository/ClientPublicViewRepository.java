package ca.bc.gov.app.m.oracle.legacyclient.repository;

import ca.bc.gov.app.core.repository.CoreRepository;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ClientPublicViewEntity;
import ca.bc.gov.app.m.postgres.client.entity.ClientTypeCodeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientPublicViewRepository extends CoreRepository<ClientPublicViewEntity> {

  @Query("select x from ClientPublicViewEntity x " +
      "where x.clientNumber = :clientNumber")
  ClientPublicViewEntity findByClientNumber(@Param("clientNumber") String clientNumber);

  @Query(value = "select * from V_CLIENT_PUBLIC WHERE CLIENT_TYPE_CODE != '" +
      ClientTypeCodeEntity.INDIVIDUAL + "'",
      countQuery = "select count(*) from V_CLIENT_PUBLIC WHERE CLIENT_TYPE_CODE != '" +
          ClientTypeCodeEntity.INDIVIDUAL + "'",
      nativeQuery = true)
  Page<ClientPublicViewEntity> findAllNonIndividualClients(Pageable paging);

}
