package ca.bc.gov.app.m.oracle.legacyclient.repository;

import org.springframework.stereotype.Repository;

import ca.bc.gov.app.core.repository.CoreRepository;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ForestClientEntity;

@Repository
public interface ForestClientRepository extends CoreRepository<ForestClientEntity> {

	//Commented out as it is not needed for now 
	/*
	 * @Query("select x from ForestClientEntity x " + "where x.clientTypeCode = '" +
	 * ClientTypeCodeEntity.FIRST_NATION_BAND + "' "+ "and x.clientStatusCode = '" +
	 * ClientStatusCodeEntity.ACTIVE + "'") List<ForestClientEntity>
	 * findAllFirstNationBandClients();
	 */

}
