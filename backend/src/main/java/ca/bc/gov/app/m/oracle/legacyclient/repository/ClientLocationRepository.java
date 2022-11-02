package ca.bc.gov.app.m.oracle.legacyclient.repository;

import org.springframework.stereotype.Repository;

import ca.bc.gov.app.core.repository.CoreRepository;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ClientLocationEntity;

@Repository
public interface ClientLocationRepository extends CoreRepository<ClientLocationEntity> {

}
