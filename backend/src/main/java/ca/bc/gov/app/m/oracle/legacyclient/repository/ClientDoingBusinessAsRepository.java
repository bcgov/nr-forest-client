package ca.bc.gov.app.m.oracle.legacyclient.repository;

import ca.bc.gov.app.core.repository.CoreRepository;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ClientDoingBusinessAsEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientDoingBusinessAsRepository
    extends CoreRepository<ClientDoingBusinessAsEntity> {

  @Query("select x from ClientDoingBusinessAsEntity x")
  List<ClientDoingBusinessAsEntity> findAllPagable(Pageable pageable);

  @Query("select count(x) from ClientDoingBusinessAsEntity x")
  Long countAll();

}
