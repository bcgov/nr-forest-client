package ca.bc.gov.app.service.client;

import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.repository.client.CountryCodeRepository;
import ca.bc.gov.app.repository.client.ProvinceCodeRepository;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientCountryProvinceService {

  private final CountryCodeRepository countryCodeRepository;
  private final ProvinceCodeRepository provinceCodeRepository;

  /**
   * <p><b>List countries</b></p>
   * <p>List countries by page with a defined size.
   * The list will be sorted by order and country name.</p> List countries by page with a defined
   * size. The list will be sorted by order and country name.
   *
   * @param page The page number, it is a 0-index base.
   * @param size The amount of entries per page.
   * @return A list of {@link CodeNameDto} entries.
   */
  public Flux<CodeNameDto> listCountries(
      int page,
      int size,
      LocalDate currentDate
  ) {
    log.info("Loading countries for page {} with size {}", page, size);
    return countryCodeRepository
        .findAllBy(PageRequest.of(page, size, Sort.by("order", "description")))
        .filter(entity -> (currentDate.isBefore(entity.getExpiredAt())
            || currentDate.isEqual(entity.getExpiredAt()))
            &&
            (currentDate.isAfter(entity.getEffectiveAt())
                || currentDate.isEqual(entity.getEffectiveAt())))
        .map(entity -> new CodeNameDto(entity.getCountryCode(), entity.getDescription()));
  }

  /**
   * <p><b>List Provinces</b></p>
   * <p>List provinces by country (which include states) by page with a defined size.
   * The list will be sorted by province name.</p>
   *
   * @param countryCode The code of the country to list provinces from.
   * @param page        The page number, it is a 0-index base.
   * @param size        The amount of entries per page.
   * @return A list of {@link CodeNameDto} entries.
   */
  public Flux<CodeNameDto> listProvinces(String countryCode, int page, int size) {
    log.info("Loading provinces for {} with page {} and size {}", countryCode, page, size);
    return provinceCodeRepository
        .findByCountryCode(countryCode, PageRequest.of(page, size, Sort.by("description")))
        .map(entity -> new CodeNameDto(entity.getProvinceCode(), entity.getDescription()));
  }

  /**
   * Retrieves country information by its country code. This method queries the
   * {@code countryCodeRepository} to find a country entity with the specified country code. If a
   * matching entity is found, it is mapped to a {@code CodeNameDto} object, which encapsulates the
   * country code and description. The resulting data is wrapped in a Mono, which represents the
   * asynchronous result of the operation.
   *
   * @param countryCode The code of the country to retrieve information for.
   * @return A Mono that emits the {@code CodeNameDto} object if a matching country is found, or an
   * empty result if no match is found.
   * @see CodeNameDto
   */
  public Mono<CodeNameDto> getCountryByCode(String countryCode) {
    log.info("Loading country for {}", countryCode);
    return countryCodeRepository
        .findByCountryCode(countryCode)
        .map(entity -> new CodeNameDto(entity.getCountryCode(),
            entity.getDescription()));
  }

  /**
 * Loads country information based on the provided country description.
 * This method queries the {@code countryCodeRepository} to find a country entity with the specified description.
 * If a matching entity is found, it is mapped to a {@code ClientValueTextDto} object, which encapsulates the country code and description.
 * If no matching entity is found, a default {@code ClientValueTextDto} object with an empty country code and the provided description is returned.
 *
 * @param countryCode The description of the country to retrieve information for.
 * @return A Mono that emits the {@code ClientValueTextDto} object if a matching country is found, or a default object if no match is found.
 * @see ClientValueTextDto
 */
public Mono<ClientValueTextDto> loadCountry(String countryCode) {
  return
      countryCodeRepository
          .findByDescription(countryCode)
          .map(
              entity -> new ClientValueTextDto(entity.getCountryCode(), entity.getDescription())
          )
          .defaultIfEmpty(new ClientValueTextDto(StringUtils.EMPTY, countryCode));
}

  /**
   * Retrieves province information by its country code and province code. This method queries the
   * {@code provinceCodeRepository} to find a province entity with the specified country code and
   * province code. If a matching entity is found, it is mapped to a {@code CodeNameDto} object,
   * which encapsulates the province code and description. The resulting data is wrapped in a Mono,
   * which represents the asynchronous result of the operation.
   *
   * @param countryCode  The code of the country to retrieve the province from.
   * @param provinceCode The code of the province to retrieve information for.
   * @return A Mono that emits the {@code CodeNameDto} object if a matching province is found, or an
   * empty result if no match is found.
   * @see CodeNameDto
   */
  public Mono<CodeNameDto> getProvinceByCountryAndProvinceCode(
      String countryCode,
      String provinceCode) {
    log.info("Loading province by country and province code {} {}", countryCode, provinceCode);
    return provinceCodeRepository
        .findByCountryCodeAndProvinceCode(countryCode, provinceCode)
        .map(entity -> new CodeNameDto(entity.getProvinceCode(),
            entity.getDescription()));
  }

  /**
 * Loads province information based on the provided country code and province code.
 * This method queries the {@code provinceCodeRepository} to find a province entity with the specified country code and province code.
 * If a matching entity is found, it is mapped to a {@code ClientValueTextDto} object, which encapsulates the province code and description.
 * If no matching entity is found, a default {@code ClientValueTextDto} object with the provided province code and description is returned.
 *
 * @param countryCode The code of the country to retrieve the province from.
 * @param provinceCode The code of the province to retrieve information for.
 * @return A Mono that emits the {@code ClientValueTextDto} object if a matching province is found, or a default object if no match is found.
 * @see ClientValueTextDto
 */
public Mono<ClientValueTextDto> loadProvince(String countryCode, String provinceCode) {
  return
      provinceCodeRepository
          .findByCountryCodeAndProvinceCode(countryCode, provinceCode)
          .map(
              entity -> new ClientValueTextDto(entity.getProvinceCode(), entity.getDescription())
          )
          .defaultIfEmpty(new ClientValueTextDto(provinceCode, provinceCode));
}


}
