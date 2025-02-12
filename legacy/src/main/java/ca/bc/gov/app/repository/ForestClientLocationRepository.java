package ca.bc.gov.app.repository;

import ca.bc.gov.app.entity.ForestClientLocationEntity;
import ca.bc.gov.app.entity.ForestClientLocationEntityId;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Repository for the ForestClientLocationEntity.
 */
@Repository
public interface ForestClientLocationRepository
    extends ReactiveCrudRepository<ForestClientLocationEntity, ForestClientLocationEntityId> {

  /**
   * Finds client locations by matching the address and postal code.
   *
   * @param address    the address to match
   * @param postalCode the postal code to match
   * @return a Flux containing the matching ForestClientLocationEntity objects
   */
  @Query("""
      select *
      from the.client_location
      where
      (
       upper(replace(replace(address_1,'-',' '),'   ',' '))
        like upper(replace(replace(concat(:address, '%'),'-',' '),'   ',' '))
       or upper(replace(replace(address_2,'-',' '),'   ',' '))
        like upper(replace(replace(concat(:address, '%'),'-',' '),'   ',' '))
       or upper(replace(replace(address_3,'-',' '),'   ',' '))
        like upper(replace(replace(concat(:address, '%'),'-',' '),'   ',' '))
      )
      and upper(postal_code) = upper(replace(:postalCode, ' ', ''))""")
  Flux<ForestClientLocationEntity> matchBy(String address, String postalCode);

  /**
   * Finds client locations by matching the address, city, province, country, and postal code.
   *
   * @param address    the address to match
   * @param postalCode the postal code to match
   * @param city       the city to match
   * @param province   the province to match
   * @param country    the country to match
   * @return a Flux containing the matching ForestClientLocationEntity objects
   */
  @Query("""
      select *
      from the.client_location
      where
      (
       upper(replace(replace(address_1,'-',' '),'   ',' '))
        like upper(replace(replace(concat(:address, '%'),'-',' '),'   ',' '))
       or upper(replace(replace(address_2,'-',' '),'   ',' '))
        like upper(replace(replace(concat(:address, '%'),'-',' '),'   ',' '))
       or upper(replace(replace(address_3,'-',' '),'   ',' '))
        like upper(replace(replace(concat(:address, '%'),'-',' '),'   ',' '))
      )
      and upper(city) = upper(:city)
      and upper(province) = upper(:province)
      and upper(country) = upper(:country)
      and upper(postal_code) = upper(replace(:postalCode, ' ', ''))""")
  Flux<ForestClientLocationEntity> matchaddress(
      String address,
      String postalCode,
      String city,
      String province,
      String country
  );

  /**
   * Finds all client locations by client number.
   *
   * @param clientNumber the client number to match
   * @return a Flux containing the matching ForestClientLocationEntity objects
   */
  Flux<ForestClientLocationEntity> findAllByClientNumber(String clientNumber);

  Flux<ForestClientLocationEntity> findByClientNumberAndClientLocnCode(String clientNumber, String clientLocnCode);

}
