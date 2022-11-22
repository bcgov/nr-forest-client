package ca.bc.gov.app.m.postgres.client.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ca.bc.gov.app.core.repository.CoreRepository;
import ca.bc.gov.app.m.postgres.client.entity.ClientTypeCodeEntity;

@Repository
public interface ClientTypeCodeRepository extends CoreRepository<ClientTypeCodeEntity> {

	@Query("from ClientTypeCodeEntity " +
		   "where (expiryDate is null or expiryDate > :activeDate) " +
		   "and effectiveDate <= :activeDate")
	List<ClientTypeCodeEntity> findActiveAt(@Param("activeDate") Date activeDate);
	
}
