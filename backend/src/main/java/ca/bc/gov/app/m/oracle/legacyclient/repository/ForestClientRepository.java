package ca.bc.gov.app.m.oracle.legacyclient.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ca.bc.gov.app.core.repository.CoreRepository;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ForestClientEntity;
import ca.bc.gov.app.m.postgres.client.entity.ClientStatusCodeEntity;
import ca.bc.gov.app.m.postgres.client.entity.ClientTypeCodeEntity;

@Repository
public interface ForestClientRepository extends CoreRepository<ForestClientEntity> {

    @Query("select x from ForestClientEntity x " +
           "where x.clientTypeCode = '" + ClientTypeCodeEntity.FIRST_NATION_BAND + "' "+
           "and x.clientStatusCode = '" + ClientStatusCodeEntity.ACTIVE + "'")
    List<ForestClientEntity> findAllFirstNationBandClients();

}
