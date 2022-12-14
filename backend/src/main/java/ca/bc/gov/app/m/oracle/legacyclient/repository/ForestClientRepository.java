package ca.bc.gov.app.m.oracle.legacyclient.repository;

import ca.bc.gov.app.core.repository.CoreRepository;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ClientStatusCodeEntity;
import ca.bc.gov.app.m.oracle.legacyclient.entity.ForestClientEntity;
import ca.bc.gov.app.m.postgres.client.entity.ClientTypeCodeEntity;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ForestClientRepository extends CoreRepository<ForestClientEntity> {

  @Query("select x from ForestClientEntity x " +
      "where x.clientTypeCode = '" + ClientTypeCodeEntity.FIRST_NATION_BAND + "' " +
      "and x.clientStatusCode = '" + ClientStatusCodeEntity.ACTIVE + "'")
  List<ForestClientEntity> findAllFirstNationBandClients();

  @Query("select x from ForestClientEntity x " +
      "where (x.registryCompanyTypeCode || x.corpRegnNmbr) = :incorporationNumber or x.clientName = :companyName")
  List<ForestClientEntity> findClientByIncorporationOrName(
      @Param("incorporationNumber") String incorporationNumber,
      @Param("companyName") String companyName);

  @Query("select x from ForestClientEntity x " +
      "where lower(x.legalFirstName) = lower(:firstName) " +
      "and lower(x.clientName) = lower(:lastName) " +
      "and x.birthdate = :birthdate")
  List<ForestClientEntity> findClientByNameAndBirthdate(@Param("firstName") String firstName,
                                                        @Param("lastName") String lastName,
                                                        @Param("birthdate") Date birthdate);


  @Query("select x from ForestClientEntity x " +
      "where x.clientTypeCode = '" + ClientTypeCodeEntity.UNREGISTERED_COMPANY + "' " +
      "and x.clientStatusCode = '" + ClientStatusCodeEntity.ACTIVE + "'")
  List<ForestClientEntity> findAllPagable(Pageable pageable);

  @Query("select count(x) from ForestClientEntity x")
  Long countAll();

}