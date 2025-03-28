package ca.bc.gov.app.repository;

import ca.bc.gov.app.dto.ForestClientLocationDetailsDto;
import ca.bc.gov.app.entity.ForestClientLocationEntity;
import ca.bc.gov.app.entity.ForestClientLocationEntityId;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

  Mono<ForestClientLocationEntity> findByClientNumberAndClientLocnCode(String clientNumber, String clientLocnCode);

  @Query("""
      select 
        l.client_number,
        l.client_locn_code,
        l.client_locn_name,
        l.address_1,
        l.address_2,
        l.address_3,
        l.city,
        l.province as province_code,
        p.province_state_name as province_desc,
        c.country_code,
        l.country as country_desc,
        l.postal_code,
        l.business_phone,
        l.home_phone,
        l.cell_phone,
        l.fax_number,
        l.email_address,
        l.locn_expired_ind,
        l.cli_locn_comment
      from the.client_location l left outer join the.mailing_province_state p
      on l.province = p.province_state_code
      left outer join the.mailing_country c
      on l.country = c.country_name
      where client_number = :clientNumber
      order by client_locn_code""")
  Flux<ForestClientLocationDetailsDto> findLocationsByClientNumber(String clientNumber);

}
